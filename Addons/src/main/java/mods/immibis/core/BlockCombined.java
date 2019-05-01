package mods.immibis.core;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import mods.immibis.core.api.util.Dir;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockCombined extends BlockContainer {
	
	private static Random random = new Random();
	
	public ItemCombined item;
	//public final String texfile;
	@Override
	public int damageDropped(IBlockState state) {
		return damageDropped(state);
	}
	
	public BlockCombined(Material m) {
		super(m);
		
		setCreativeTab(CreativeTabs.MISC);
		setHardness(2.0F);
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player,
			ItemStack stack) {
TileCombined te = (TileCombined)world.getTileEntity(pos);
        
		Vec3d look = player.getLook(1.0f);
		
        double absx = Math.abs(look.x);
        double absy = Math.abs(look.y);
        double absz = Math.abs(look.z);
        
        if(absx > absy && absx > absz) {
        	if(look.x < 0)
        		te.onPlaced(player, Dir.NX);
        	else
        		te.onPlaced(player, Dir.PX);
        } else if(absy > absz) {
        	if(look.y < 0)
        		te.onPlaced(player, Dir.NY);
        	else
        		te.onPlaced(player, Dir.PY);
        } else {
        	if(look.z < 0)
        		te.onPlaced(player, Dir.NZ);
        	else
        		te.onPlaced(player, Dir.PZ);
        }
	}
	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}
	
	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int i) {
		TileCombined te = (TileCombined)world.getTileEntity(x, y, z);
		if(te != null)
			return te.redstone_output;
		return 0;
	}
	
	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int i) {
		return isProvidingStrongPower(world, x, y, z, i);
	}
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		list.add(new ItemStack(this, 1));
		return list;
	}
	
	public List<ItemStack> getInventoryDrops(World world, BlockPos pos) {
		TileCombined te = (TileCombined)world.getTileEntity(pos);
		if(te == null)
			return Collections.emptyList();
		return te.getInventoryDrops();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return 0;
	}
	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		if(world.isRemote)
			return;
		TileCombined te = (TileCombined)world.getTileEntity(pos);
		if(te != null)
			te.onBlockNeighbourChange();
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(player.isSneaking())
			return false;
		TileCombined te = (TileCombined)world.getTileEntity(pos);
		if(te != null)
			return te.onBlockActivated(player);
		return false;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		int x = pos.getX();
    	int y = pos.getY();
    	int z = pos.getZ();
		TileCombined te = (TileCombined)world.getTileEntity(pos);
		if(te != null)
			te.onBlockRemoval();
		
		List<ItemStack> drops = getInventoryDrops(world, pos);
		for(ItemStack stack : drops) {
			float xpos = x + random.nextFloat() * 0.8f + 0.1f;
			float ypos = y + random.nextFloat() * 0.8f + 0.1f;
			float zpos = z + random.nextFloat() * 0.8f + 0.1f;
			
			//System.out.println("drop "+stack + " at "+xpos+","+ypos+","+zpos);
			
			// chests do this (multiple drops per stack, 10-30 items at a time)
			int left = stack.getMaxStackSize();
			while(left > 0) {
				int removeCount = Math.min(random.nextInt(21) + 10, left);
				left -= removeCount;
				
				EntityItem ent = new EntityItem(world, xpos, ypos, zpos, new ItemStack(stack.getItem(), removeCount, stack.getItemDamage()));
				
				ent.motionX = random.nextGaussian() * 0.05f;
				ent.motionY = random.nextGaussian() * 0.05f + 0.2f;
				ent.motionZ = random.nextGaussian() * 0.05f;
				
				if(stack.hasTagCompound())
					ent.getItem().setTagCompound(stack.getTagCompound());
				
				world.spawnEntity(ent);
	        }
		}
		super.breakBlock(world, pos, state);
	}
	
	public abstract TileEntity getBlockEntity(int data);
	
	@Override
	public final TileEntity createNewTileEntity(World world, int meta) {
		return getBlockEntity(meta);
	}
	
	public abstract void getCreativeItems(List<ItemStack> is);
	
	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		getCreativeItems(items);
	}
}
