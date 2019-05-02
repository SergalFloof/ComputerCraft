package mods.immibis.core.api.multipart.util;


import java.util.ArrayList;
import java.util.List;

import mods.immibis.core.api.multipart.IMultipartRenderingBlockMarker;
import mods.immibis.core.api.multipart.IMultipartSystem;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public abstract class BlockMultipartBase extends BlockContainer implements IMultipartRenderingBlockMarker {
	
	protected BlockMultipartBase(Material mat) {
		super(mat);
		
		setHardness(1); // the block hardness must not be zero.
		// if using IPartContainer tiles, the block hardness should be irrelevant, but still can't be zero.
	}
	
	
	
	@Override public boolean isOpaqueCube(IBlockState state) {return false;}

//	@Override public final boolean renderAsNormalBlock() {return false;}
	@Override public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {return true;}
	
	
	
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
			EntityPlayer player) {
		int x = pos.getX();
    	int y = pos.getY();
    	int z = pos.getZ();

		ItemStack coverPick = IMultipartSystem.instance.hook_getPickBlock(target, world, pos, player);
		if(coverPick != null)
			return coverPick;
		
		return super.getPickBlock(state, target, world, pos, player);
	}
	
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {

		if(IMultipartSystem.instance.hook_isSideSolid(world, pos, side))
			return true;
		
		return super.isSideSolid(state, world, pos, side);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
		int x = pos.getX();
    	int y = pos.getY();
    	int z = pos.getZ();

		if(IMultipartSystem.instance.hook_addDestroyEffects(world, pos, manager))
			return true;
		
		return super.addDestroyEffects(world, pos, manager);
	}
	
	@SideOnly(Side.CLIENT)
	
	@Override
	public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager) {
		if(IMultipartSystem.instance.hook_addHitEffects(world, target, manager))
			return true;
		
		return super.addHitEffects(state, world, target, manager);
	}
	
	@Override
	public RayTraceResult collisionRayTrace(IBlockState blockState, World world, BlockPos pos, Vec3d start,
			Vec3d end) {
		int x = pos.getX();
    	int y = pos.getY();
    	int z = pos.getZ();
		return IMultipartSystem.instance.hook_collisionRayTrace(super.collisionRayTrace(blockState, world, pos, start, end), world, pos, start, end);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask,
			List<AxisAlignedBB> list, Entity entity, boolean isActualState) {
		// Note: if you're implementing this yourself (instead of extending this class) you don't need to check the
				// return value of hook_addCollisionBlocksToList.
				// The check is to allow BlockMultipartBase to work with both multipart and normal blocks.
				if(!IMultipartSystem.instance.hook_addCollisionBoxesToList(world, pos, mask, list, entity))
					super.addCollisionBoxToList(state, world, pos, mask, list, entity, isActualState);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return IMultipartSystem.instance.hook_getDrops(super.getDrops(world, pos, state, fortune), world, pos, state, fortune);
	}
}
