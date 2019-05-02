package mods.immibis.ccperiphs;


import java.util.ArrayList;
import java.util.List;

import mods.immibis.ccperiphs.coproc.TileCoprocBase;
import mods.immibis.ccperiphs.lan.TileNIC;
import mods.immibis.ccperiphs.lan.WorldNetworkData;
import mods.immibis.ccperiphs.rfid.TileRFIDWriter;
import mods.immibis.core.BlockCombined;
import mods.immibis.core.api.util.Dir;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPeriphs extends BlockCombined {
	
	public static int model;

	public BlockPeriphs() {
		super(Material.IRON);
	}
	
	@Override
	public RayTraceResult collisionRayTrace(IBlockState blockState, World world, BlockPos pos, Vec3d start,
			Vec3d end) {
		int x = pos.getX();
    	int y = pos.getY();
    	int z = pos.getZ();
		List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
		addCollisionBoxesToList(world, pos, AxisAlignedBB.getBoundingBox(pos, x+1, y+1, z+1), list, null);
		
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
	public void setBlockBoundsBasedOnState(IBlockAccess w, BlockPos pos) {
		if(w.getBlockState(pos) != this) {
			setBlockBounds(0, 0, 0, 1, 1, 1);
			return;
		}
		
		EnumPeriphs type = EnumPeriphs.VALUES[w.getBlockMetadata(x, y, z)];
		TileEntity te = w.getTileEntity(x, y, z);
		
		switch(type) {
		case RFID_WRITER:
			if(te == null) return;
			
			switch(((TileRFIDWriter)te).facing) {
			case Dir.PY: setBlockBounds(0, 0, 0, 1, 9/16.0f, 1); break;
			case Dir.PX: setBlockBounds(0, 0, 0, 9/16.0f, 1, 1); break;
			case Dir.NX: setBlockBounds(7/16.0f, 0, 0, 1, 1, 1); break;
			case Dir.PZ: setBlockBounds(0, 0, 0, 1, 1, 9/16.0f); break;
			case Dir.NZ: setBlockBounds(0, 0, 7/16.0f, 1, 1, 1); break;
			}
			break;
		case NIC:
			if(te == null) return;
			
			float ins = 2/16f;
			float thick = 2/16f;
			
			switch(((TileNIC)te).facing) {
			case Dir.PY: setBlockBounds(ins, 1-thick, ins, 1-ins, 1, 1-ins); break;
			case Dir.NY: setBlockBounds(ins, 0, ins, 1-ins, thick, 1-ins); break;
			case Dir.PX: setBlockBounds(1-thick, ins, ins, 1, 1-ins, 1-ins); break;
			case Dir.NX: setBlockBounds(0, ins, ins, thick, 1-ins, 1-ins); break;
			case Dir.PZ: setBlockBounds(ins, ins, 1-thick, 1-ins, 1-ins, 1); break;
			case Dir.NZ: setBlockBounds(ins, ins, 0, 1-ins, 1-ins, thick); break;
			}
			break;
		default:
			setBlockBounds(0, 0, 0, 1, 1, 1);
			break;
		}
	}

//	@Override
//	public IIcon getIcon(int side, int data) {return Blocks.cobblestone.getIcon(side, data);}
//	
	// @Override // client only override
	/*public int getBlockTexture(IBlockAccess w, int x, int y, int z, int side) {
		TilePeriphs t;
		try {
			t = (TilePeriphs)w.getBlockTileEntity(x, y, z);
		} catch(Exception e) {
			return 0;
		}
		
		if(t == null)
			return 0;
		
		return t.getTexture(side);
	}*/
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		// TODO Auto-generated method stub
		return super.isBlockNormalCube(state);
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		switch(EnumPeriphs.VALUES[world.getBlockMetadata(x, y, z)])
		{
		case COPROC_ADVMAP:
		case COPROC_CRYPTO:
			// all sides except front
			return side.ordinal() != ((TileCoprocBase)world.getTileEntity(pos)).facing;
		case MAG_STRIPE:
		case RFID_READER:
			return true;
		case RFID_WRITER:
			// back only
			return side.ordinal() == (1 ^ ((TileRFIDWriter)world.getTileEntity(pos)).facing);
		case NIC:
			return false;
		}
		return false;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess world, BlockPos pos) {
		int x = pos.getX();
    	int y = pos.getY();
    	int z = pos.getZ();
		setBlockBoundsBasedOnState(world, x, y, z);
		return super.getCollisionBoundingBox(blockState, world, pos);
	}
	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox,
			List<AxisAlignedBB> collidingBoxes, Entity entity, boolean isActualState) {
		int meta = world.getBlockMetadata(par2, par3, par4);
		if(meta == EnumPeriphs.NIC.ordinal() && ImmibisPeripherals.enableLANRegistration)
			ImmibisPeripherals.lanWire.addCollidingBlockToList(world, pos, entityBox, collidingBoxes, entity);
		super.addCollisionBoxesToList(world, pos, entityBox, collidingBoxes, entity);
	}
	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
		setBlockBoundsBasedOnState(world, pos);
		return super.getSelectedBoundingBoxFromPool(world, par2, par3, par4);
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return model;
	}

	@Override
	public TileEntity getBlockEntity(int data) {
		return EnumPeriphs.VALUES[data].createTile();
	}

	@Override
	public void getCreativeItems(List<ItemStack> arraylist) {
		for(EnumPeriphs e : EnumPeriphs.VALUES) {
			if(!ImmibisPeripherals.allowAdventureMapInterface && e == EnumPeriphs.COPROC_ADVMAP)
				continue;
			arraylist.add(new ItemStack(this, 1, e.ordinal()));
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		int x = pos.getX();
    	int y = pos.getY();
    	int z = pos.getZ();
		if(world.getBlockMetadata(x, y, z) == EnumPeriphs.NIC.ordinal()) {
			WorldNetworkData.getForWorld(world).removeNIC(x, y, z);
		}
	}
		
		
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		int x = pos.getX();
    	int y = pos.getY();
    	int z = pos.getZ();
		int meta = world.getBlockMetadata(par2, par3, par4); 
		
		if(meta == EnumPeriphs.NIC.ordinal()) {
			WorldNetworkData.getForWorld(world).addNIC(x, y, z, 0);
		}
		
		super.onBlockAdded(world, pos, state);
	}
	
}
