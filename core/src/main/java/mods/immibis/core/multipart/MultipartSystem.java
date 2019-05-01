package mods.immibis.core.multipart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import mods.immibis.core.api.APILocator;
import mods.immibis.core.api.multipart.ICoverSystem;
import mods.immibis.core.api.multipart.IMultipartSystem;
import mods.immibis.core.api.multipart.ICoverableTile;
import mods.immibis.core.api.multipart.IPartContainer;
import mods.immibis.core.api.multipart.PartBreakEvent;
import mods.immibis.core.api.multipart.PartCoordinates;
import mods.immibis.core.api.porting.SidedProxy;
import mods.immibis.core.util.SynchronizedWeakIdentityListMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEmitter;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultipartSystem extends IMultipartSystem {
	public static MultipartSystem instance;
	{
		if(instance != null)
			throw new IllegalStateException("Creating multiple MultipartSystem instances");
		instance = this;
		IMultipartSystem.instance = this;
	}
	
	public void init() {
		SidedProxy.instance.createSidedObject("mods.immibis.core.multipart.ClientProxy", null);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	// Run last, because anything else that affects harvestability won't know about multiparts,
	// so would most likely produce a wrong result.
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void canHarvest(PlayerEvent.HarvestCheck evt) {
		World w = evt.getEntityPlayer().world;
		
		updateBreakingPartIfOnClient();
		
		PartCoordinates part = getBreakingPart(evt.getEntityPlayer());
		
		if(part == null)
			return;
		
		// sanity check
		// (normally we'd check the event coordinates against the part coordinates here as a sanity check,
		// but the event doesn't include those!)
		if(w.getBlockState(part.x, part.y, part.z) != evt.block)
			return;
		
		IPartContainer ci = getPartContainer(w, part);
		if(ci != null) {
			evt.success = ci.canPlayerHarvestPart(evt.getEntityPlayer(), part.part); 
		}
	}
	
	// Run last for the same reason as canHarvest
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void getBreakSpeed(PlayerEvent.BreakSpeed evt) {
		
		World w = evt.getEntityPlayer().world;
		if(w.isRemote)
			updateBreakingPartIfOnClient();
		
		PartCoordinates part = getBreakingPart(evt.getEntityPlayer(), GBP_SANITY_LEVEL_NONE);
		
		if(part == null || part.x != evt.x || part.y != evt.y || part.z != evt.z || evt.block != w.getBlockState(part.x, part.y, part.z))
			return;
		
		IPartContainer ci = getPartContainer(w, part);
		if(ci != null) {
			evt.newSpeed = ci.getPlayerRelativePartHardness(evt.getEntityPlayer(), part.part);
		
			// undo the stuff done by ForgeHooks.blockStrength
			float hardness = evt.block.getBlockHardness(w, part.x, part.y, part.z);
		
			if(!ForgeHooks.canHarvestBlock(evt.block, evt.getEntityPlayer(), evt.metadata))
				evt.newSpeed *= hardness * 100;
			else
				evt.newSpeed *= hardness * 30;
		}
	}
	
	
	
	
	////////// PART BREAKING //////////
	
	/** Maps players to the part they are currently breaking.
	 * If the player is not currently breaking a part, their value is undefined
	 * (they may or may not have an entry in the map)
	 */
	private SynchronizedWeakIdentityListMap<EntityPlayer, PartCoordinates> breaking_part = new SynchronizedWeakIdentityListMap<EntityPlayer, PartCoordinates>();
	
	// only call on client
	private void playPartDestroyEffect(IPartContainer pc, int part) {
		pc.addPartDestroyEffects(part, Minecraft.getMinecraft().effectRenderer);
	}
	
	// run first, so we can cancel it if breaking a part (and then things expecting it to be a full block won't run)
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent evt) {
		EntityPlayer ply = evt.getPlayer();
		if(ply.world.isRemote && ply.capabilities.isCreativeMode)
			updateBreakingPartIfOnClient();
		
		// remove the part the player was breaking
		PartCoordinates coord = getBreakingPart(ply);
		breaking_part.remove(ply);
		
		if(coord == null || coord.x != evt.x || coord.y != evt.y || coord.z != evt.z)
			return;
		
		IPartContainer pc = getPartContainer(evt.world, coord);
		
		if(pc == null)
			return;
		
		PartBreakEvent partBreakEvent = new PartBreakEvent(evt.world, coord, ply);
		MinecraftForge.EVENT_BUS.post(partBreakEvent);
		if(partBreakEvent.isCanceled()) {
			evt.setCanceled(true);
			return;
		}
		
		if(ply.world.isRemote) {
			
			sendDigFinish(coord);
			
			// client-side prediction - no drops, and send a dig finish packet.
			playPartDestroyEffect(pc, coord.part);
			pc.removePartByPlayer(ply, coord.part, false);
			
		} else {
			broken_parts.put(ply, coord);
			
			boolean isHarvesting = pc.canPlayerHarvestPart(ply, coord.part) && !ply.capabilities.isCreativeMode;
			
			pc.removePartByPlayer(ply, coord.part, isHarvesting);
		}
		
		evt.setCanceled(true);
	}
	
	private static final int GBP_SANITY_LEVEL_NONE = 0;
	private static final int GBP_SANITY_LEVEL_CHECK = 1;
	public PartCoordinates getBreakingPart(EntityPlayer ply, int sanityLevel) {
		PartCoordinates pc = getBreakingPart(ply);
		if(sanityLevel >= GBP_SANITY_LEVEL_CHECK && pc != null)
		{
			if(!ply.world.blockExists(pc.x, pc.y, pc.z))
			{
				pc = null;
			}
			
			if(pc == null)
			{
				breaking_part.remove(ply);
			}
		}
		return pc;
	}
	
	public PartCoordinates getBreakingPart(EntityPlayer ply) {
		return breaking_part.get(ply);
	}
	
	@SideOnly(Side.CLIENT)
	private void sendDigStart() {
		PartCoordinates coord = getBreakingPart(Minecraft.getMinecraft().player);
		if(coord != null)
			APILocator.getNetManager().sendToServer(new PacketMultipartDigStart(coord));
	}
	
	
	
	void updateBreakingPartIfOnClient() {
		if(!FMLCommonHandler.instance().getEffectiveSide().isClient())
			return;
		
		EntityPlayer ply = SidedProxy.instance.getThePlayer();
		PartCoordinates old = getBreakingPart(ply);
		
		RayTraceResult ray = ply.rayTrace(SidedProxy.instance.getPlayerReach(ply), 0);
		PartCoordinates _new = null;
		if(ray == null || ray.typeOfHit != RayTraceResult.Type.BLOCK) {
			breaking_part.remove(ply);
			
		} else {
			breaking_part.put(ply, _new = new PartCoordinates(ray.blockX, ray.blockY, ray.blockZ, SubhitValues.getPartIndex(ray.subHit), SubhitValues.isCoverSystem(ray.subHit)));
		}
		
		boolean changed = (old == null && _new != null) || (old != null && !old.equals(_new));
		
		//if(changed) // always send update, since it's not always synced
			sendDigStart();
		if(changed)
			resetBreakProgress(ply);
	}
	
	private static void resetBreakProgress(EntityPlayer ply) {
		// Need to reset the block damage, but that doesn't seem to be possible
		// Even RP2's covers don't do that
		// TODO: this was not edited since 1.2.5, is it possible now?
		/*PlayerController pc = ModLoader.getMinecraftInstance().playerController;
		pc.resetBlockRemoving();
		pc.updateController();*/
	}
	
	void setBreakingPart(EntityPlayer source, PartCoordinates part) {
		if(part == null)
			breaking_part.remove(source);
		else
			breaking_part.put(source, part);
		
		// TODO
		//if(source.capabilities.isCreativeMode) {
		//	onRemoveBlockByPlayer(source.worldObj, source, part.x, part.y, part.z);
		//}
		
		//for(EntityPlayer pl : (List<EntityPlayer>)source.worldObj.playerEntities)
		//	APILocator.getNetManager().sendToClient(new PacketUpdateBreakingPart(part), pl);
	}

	public Iterable<Map.Entry<EntityPlayer, PartCoordinates>> getBreakingParts() {
		return breaking_part.entries();
	}
	
	
	
	
	
	
	
	
	
	////////// DIG FINISH PREDICTION //////////
	
	/** Maps players to the part they most recently broke - used for checking dig finish packets.
	 */
	private SynchronizedWeakIdentityListMap<EntityPlayer, PartCoordinates> broken_parts = new SynchronizedWeakIdentityListMap<EntityPlayer, PartCoordinates>();
	
	public boolean didClientJustBreakPart(EntityPlayer ply, PartCoordinates coord) {
		PartCoordinates broke = broken_parts.get(ply);
		broken_parts.remove(ply);
		
		return broke != null && broke.equals(coord);
	}
	
	@SideOnly(Side.CLIENT)
	private static void sendDigFinish(PartCoordinates coord) {
		APILocator.getNetManager().sendToServer(new PacketMultipartDigFinish(coord));
	}
	
	private static ICoverSystem getCoverSystem(TileEntity te) {
		if(!(te instanceof ICoverableTile))
			return null;
		return ((ICoverableTile)te).getCoverSystem();
	}
	
	private static IPartContainer getPartContainer(BlockPos pos, World world, PartCoordinates coords) {
		return getPartContainer( pos, world, coords.x, coords.y, coords.z, coords.isCoverSystemPart);
	}
	private static IPartContainer getPartContainer(BlockPos pos, World world, int x, int y, int z, boolean isCoverSystemPart) {
		TileEntity te = world.getTileEntity(pos);
		if(isCoverSystemPart) {
			if(te instanceof ICoverableTile)
				return ((ICoverableTile)te).getCoverSystem();
		} else {
			if(te instanceof IPartContainer)
				return (IPartContainer)te;
		}
		return null;
	}

	@Override
	public boolean hook_addCollisionBoxesToList(World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity entity) {
		TileEntity te = world.getTileEntity(pos);
		if(te != null) {
			if(te instanceof ICoverableTile) {
				ICoverSystem ci = ((ICoverableTile)te).getCoverSystem();
				if(ci != null)
					ci.getCollidingBoundingBoxes(mask, list, entity);
			}
			if(te instanceof IPartContainer) {
				((IPartContainer)te).getCollidingBoundingBoxes(mask, list, entity);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ItemStack hook_getPickBlock(RayTraceResult trace, World world, BlockPos pos, EntityPlayer player) {
		int blockX = pos.getX();
    	int blockY = pos.getY();
    	int blockZ = pos.getZ();
		if(trace.typeOfHit != RayTraceResult.Type.BLOCK)
			return null;
		if(trace.getBlockPos().getX() != blockX || trace.getBlockPos().getY() != blockY || trace.getBlockPos().getZ() != blockZ)
			return null;
		
		IPartContainer ci = getPartContainer(pos, world, blockX, blockY, blockZ, SubhitValues.isCoverSystem(trace.subHit));
		if(ci != null)
		{
			return ci.pickPart(trace, SubhitValues.getPartIndex(trace.subHit));
		}
		
		return null;
	}
	
	@Override
	public boolean hook_isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
		TileEntity te = world.getTileEntity(pos);
		
		if(te instanceof IPartContainer && ((IPartContainer)te).isPartContainerSideSolid(side))
			return true;
		
		if(te instanceof ICoverableTile) {
			ICoverSystem ci = ((ICoverableTile)te).getCoverSystem();
			if(ci != null && ci.isPartContainerSideSolid(side))
				return true;
		}
		
		return false;
	}
	
	@Override
	public boolean hook_addHitEffects(World world, RayTraceResult trace, BlockPos pos, ParticleManager manager) {
		PartCoordinates p = getBreakingPart(Minecraft.getMinecraft().player);
		if(p == null)
			return false;
		
		if(p.x != trace.getBlockPos().getX() || p.y != trace.getBlockPos().getY() || p.z != trace.getBlockPos().getZ() || p.part != SubhitValues.getPartIndex(trace.subHit) || p.isCoverSystemPart != SubhitValues.isCoverSystem(trace.subHit))
			return false; // something weird happened
		
		IPartContainer pc = getPartContainer(pos, world, p);
		if(pc == null)
			return false;
		
		return pc.addPartHitEffects(p.part, trace.sideHit, manager);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean hook_addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
		int x = pos.getX();
    	int y = pos.getY();
    	int z = pos.getZ();
		PartCoordinates part = getBreakingPart(Minecraft.getMinecraft().player);
		if(part == null || part.x != x || part.y != y || part.z != z)
			return false;
		
		//IPartContainer4 pc = getPartContainer(world, part);
		//if(pc != null && pc.addPartDestroyEffects(part, er)
		
		// TODO - Destroy effects are already added in the block break event.
		
		return false;
	}
	
	private static long lastSubhitCollisionWarningAt = Long.MIN_VALUE;
	
	private static RayTraceResult getCloserMOP(RayTraceResult a, RayTraceResult b, Vec3d src) {
		if(a == null) return b;
		if(b == null) return a;
		
		double ciDistSq = a.hitVec.squareDistanceTo(src);
		double normalDistSq = b.hitVec.squareDistanceTo(src);
		
		return ciDistSq < normalDistSq ? a : b;
	}
	
	@Override
	public RayTraceResult hook_collisionRayTrace(RayTraceResult result, World world, BlockPos pos, Vec3d src, Vec3d dst) {
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IPartContainer)
			result = ((IPartContainer)te).collisionRayTrace(src, dst);
		IPartContainer ci2 = (te instanceof ICoverableTile) ? ((ICoverableTile)te).getCoverSystem() : null;
		if(ci2 == null)
			return result;
		
		if(result != null && SubhitValues.isCoverSystem(result.subHit)) {
			long time = System.nanoTime();
			if(time - 10000000000L > lastSubhitCollisionWarningAt) {
				lastSubhitCollisionWarningAt = time;
				System.err.println(world.getBlockState(pos)+" collisionRayTrace used a subhit index that conflicts with the range used for microblocks!");
			}
		}
		
		result = getCloserMOP(result, ci2.collisionRayTrace(src, dst), src);
		return result;
	}
	
	/**
	 * If this is false (default), then breaking a multipart block will break all the parts.
	 * If true, it will only break the core block (if the block is not itself multipart)
	 */
	static ThreadLocal<AtomicBoolean> isDoingMultipartCompatibleBlockBreak = new ThreadLocal<AtomicBoolean>() {
		@Override
		protected AtomicBoolean initialValue() {
			return new AtomicBoolean(false);
		}
	};
	
	@Override
	public ArrayList<ItemStack> hook_getDrops(List<ItemStack> drops, IBlockAccess world, BlockPos pos, int metadata, int fortune) {
		ArrayList<ItemStack> rv = super.hook_getDrops(drops, world, pos, metadata, fortune);
		if(isDoingMultipartCompatibleBlockBreak.get().get())
			return rv;
		
		// if a block is broken in a non-multipart-compatible way, then drop all the parts
		
		TileEntity te = world.getTileEntity(pos);
		if(te != null) {
			if(te instanceof ICoverableTile) {
				ICoverSystem cs = ((ICoverableTile)te).getCoverSystem();
				if(cs != null)
					cs.getPartContainerDrops(drops, fortune);
			}
			if(te instanceof IPartContainer)
				((IPartContainer)te).getPartContainerDrops(drops, fortune);
		}
		return rv;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderMultiparts(IBlockAccess world, int x, int y, int z, RenderBlocks renderer) {
		return ClientProxy.renderMultiparts(renderer, world, x, y, z);
	}

	void clearBreakingPart(EntityPlayer ply) {
		breaking_part.remove(ply);
	}
}
