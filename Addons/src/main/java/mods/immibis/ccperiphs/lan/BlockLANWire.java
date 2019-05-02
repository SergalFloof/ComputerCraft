package mods.immibis.ccperiphs.lan;


import java.util.ArrayList;
import java.util.List;

import mods.immibis.ccperiphs.BlockPeriphs;
import mods.immibis.ccperiphs.EnumPeriphs;
import mods.immibis.ccperiphs.ImmibisPeripherals;
import mods.immibis.core.api.util.Dir;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLANWire extends Block {
	public static int renderType = -1;

	public BlockLANWire() {
		super(Material.CIRCUITS);
	}
	
//	@Override
//	@SideOnly(Side.CLIENT)
//	public void registerBlockIcons(IIconRegister reg) {
//		blockIcon = RenderUtilsIC.loadIcon(reg, "immibis_peripherals:lanwire");
//	}
	
	public static boolean connects(IBlockAccess w, int x, int y, int z, int fromMeta) {
		Block id = w.getBlock(x, y, z);
		int bMeta = w.getBlockState(x, y, z);
		if(id == ImmibisPeripherals.lanWire)
			return fromMeta == bMeta;
		if(id != ImmibisPeripherals.block)
			return false;
		if(fromMeta == 0)
			return bMeta == EnumPeriphs.NIC.ordinal();
		return false;
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox,
			List<AxisAlignedBB> collidingBoxes, Entity entity, boolean isActualState) {
		addCollidingBlockToList((IBlockAccess)world, pos, entityBox, collidingBoxes, entity);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void addCollisionBoxesToList(IBlockAccess blockAccess, BlockPos pos, AxisAlignedBB par5AxisAlignedBB, List par6List, Entity par7Entity) {
		addCollidingBlockToList((IBlockAccess)blockAccess, pos, par5AxisAlignedBB, par6List, par7Entity);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addBB(AxisAlignedBB mask, List list, int x, int y, int z, AxisAlignedBB bb) {
		bb = bb.offset(x, y, z);
		if(mask == null || bb.intersectsWith(mask))
			list.add(bb);
	}
	
	@SuppressWarnings("rawtypes")
	public void addCollidingBlockToList(IBlockAccess blockAccess, BlockPos pos, AxisAlignedBB mask, List list, Entity par7Entity) {
		int x = pos.getX();
    	int y = pos.getY();
    	int z = pos.getZ();
		Block id = blockAccess.getBlock(x, y, z);
		int meta = blockAccess.getBlockState(x, y, z);
		int forceDir = -1;
		
		if(id != this) {
			if(meta == EnumPeriphs.NIC.ordinal()) {
				meta = 0;
				forceDir = ((TileNIC)blockAccess.getTileEntity(pos)).facing;
			}
		}
		
		boolean nx = BlockLANWire.connects(blockAccess, x-1,y,z,meta) || forceDir == Dir.NX;
		boolean px = BlockLANWire.connects(blockAccess, x+1,y,z,meta) || forceDir == Dir.PX;
		boolean ny = BlockLANWire.connects(blockAccess, x,y-1,z,meta) || forceDir == Dir.NY;
		boolean py = BlockLANWire.connects(blockAccess, x,y+1,z,meta) || forceDir == Dir.PY;
		boolean nz = BlockLANWire.connects(blockAccess, x,y,z-1,meta) || forceDir == Dir.NZ;
		boolean pz = BlockLANWire.connects(blockAccess, x,y,z+1,meta) || forceDir == Dir.PZ;
		
		final double min = 6/16f, max=10/16f;
		
		if(!nx && !ny && !nz && !px && !py && !pz) {
			addBB(mask,list,x,y,z, AxisAlignedBB.getBoundingBox(min, min, min, max, max, max));
			return;
		}
		
		if(nx || px) {
			addBB(mask,list,x,y,z, AxisAlignedBB.getBoundingBox(nx?0:min, min, min, px?1:max, max, max));
		}
		
		if(ny || py) {
			addBB(mask,list,x,y,z, AxisAlignedBB.getBoundingBox(min, ny?0:min, min, max, py?1:max, max));
		}
		
		if(nz || pz) {
			addBB(mask,list,x,y,z, AxisAlignedBB.getBoundingBox(min, min, nz?0:min, max, max, pz?1:max));
		}
	}
	
	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
		int x = pos.getX();
    	int y = pos.getY();
    	int z = pos.getZ();
		final double min = 6/16f, max=10/16f;
		
		int meta = world.getBlockState(pos);
		
		double x1 = BlockLANWire.connects(world, x-1,y,z,meta) ? 0 : min;
		double x2 = BlockLANWire.connects(world, x+1,y,z,meta) ? 1 : max;
		double y1 = BlockLANWire.connects(world, x,y-1,z,meta) ? 0 : min;
		double y2 = BlockLANWire.connects(world, x,y+1,z,meta) ? 1 : max;
		double z1 = BlockLANWire.connects(world, x,y,z-1,meta) ? 0 : min;
		double z2 = BlockLANWire.connects(world, x,y,z+1,meta) ? 1 : max;
		
		return AxisAlignedBB.getBoundingBox(x+x1, y+y1, z+z1, x+x2, y+y2, z+z2);
		//return AxisAlignedBB.getBoundingBox(x1, y1, z1, x2, y2, z2);
	}
	
	@Override
	public RayTraceResult collisionRayTrace(IBlockState blockState, World world, BlockPos pos, Vec3d start,
			Vec3d end) {
		List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
		addCollidingBlockToList(world, pos, null, list, null);
		
		RayTraceResult best = null;
		double best_dist = 0;
		for(AxisAlignedBB bb : list) {
			RayTraceResult rt = bb.calculateIntercept(start, end);
			if(rt == null)
				continue;
			double dist = rt.hitVec.distanceTo(start);
			if(best == null || dist < best_dist) {
				best = rt;
				best_dist = dist;
			}
		}
		
		return best == null ? null : new RayTraceResult(best.hitVec, best.sideHit, pos);
	}
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public int getRenderType() {
		return renderType == -1 ? BlockPeriphs.model : renderType;
	}
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		WorldNetworkData.getForWorld(world).removeCable(pos);
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		WorldNetworkData.getForWorld(world).addCable(pos, world.getBlockState(pos));
		super.onBlockAdded(world, pos, state);
	}
}
