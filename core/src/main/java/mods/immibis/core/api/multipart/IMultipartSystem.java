package mods.immibis.core.api.multipart;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Provides methods allowing blocks to implement microblock ("covering") support.
 * All of the methods in this class do nothing, but are overridden if Immibis Core is installed.
 */
public class IMultipartSystem {
	
	/** Immibis Core sets this to an actual implementation during load time */
	public static @Nonnull IMultipartSystem instance = new IMultipartSystem();

	/** Call this from addCollisionBoxesToList for cover-supporting blocks.
	 * Returns true if the tile entity implements IPartContainer.
	 */
	public boolean hook_addCollisionBoxesToList(World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity entity) {return false;}

	/** Call this from getPickBlock for cover-supporting blocks. If it returns a non-null value, then return that. */
	public ItemStack hook_getPickBlock(RayTraceResult trace, World world, BlockPos pos, EntityPlayer player) {return null;}
	
	/** Call this from isSideSolid for cover-supporting blocks. If it returns true, then return true. */
	public boolean hook_isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {return false;}
	
	/** Call this from addHitEffects for cover-supporting blocks. If it returns true, then return true without adding any custom effects. */
	@SideOnly(Side.CLIENT)
	public boolean hook_addHitEffects(World world, RayTraceResult trace, ParticleManager manager) {return false;}
	
	/** Call this from addDestroyEffects for cover-supporting blocks. If it returns true, then return true without adding any custom effects. */
	@SideOnly(Side.CLIENT)
	public boolean hook_addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {return false;}
	
	/** Call this from collisionRayTrace for cover-supporting blocks, before returning. Pass it the result you were going to return (even if that was null). */
	public RayTraceResult hook_collisionRayTrace(RayTraceResult normalResult, World world, BlockPos pos, Vec3d src, Vec3d dst) {return normalResult;}

	/** Call this from getDrops for cover-supporting blocks, before returning. Pass it the result you were going to return. */
	public ArrayList<ItemStack> hook_getDrops(List<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {return drops instanceof ArrayList<?> ? (ArrayList<ItemStack>)drops : new ArrayList<ItemStack>(drops);}

	/** Call this from your renderer's renderWorldBlock for cover-supporting blocks, before returning.
	 * If it returns true (indicating it rendered something), then return true.
	 * (Render your custom stuff regardless of what this returns)
	 * 
	 * If there is a tile entity implementing ICoverableTile with a non-null cover system, it will call renderPartContainer on that.
	 * If there is a tile entity implementing IPartContainer, it will call renderPartContainer on that.
	 */
	@SideOnly(Side.CLIENT)
	public boolean renderMultiparts(IBlockAccess world, int x, int y, int z, Render renderer) {return false;}
}
