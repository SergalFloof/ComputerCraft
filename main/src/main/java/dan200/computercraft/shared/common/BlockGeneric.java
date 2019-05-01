/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.common;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class BlockGeneric extends Block implements
    ITileEntityProvider
{
    protected BlockGeneric( Material material )
    {
        super( material );
        this.isBlockContainer = true;
    }

    protected abstract IBlockState getDefaultBlockState( int damage, EnumFacing placedSide );
    protected abstract TileGeneric createTile( IBlockState state );
    protected abstract TileGeneric createTile( int damage );

    @Override
    public final void dropBlockAsItemWithChance( World world, BlockPos pos, IBlockState state, float chance, int fortune )
    {
    }
        
    @Override
    public final List<ItemStack> getDrops( IBlockAccess world, BlockPos pos, IBlockState state, int fortune )
    {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>( 1 );
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            generic.getDroppedItems( drops, fortune, false, false );
        }
        return drops;
    }
    
    @Override
    public final IBlockState onBlockPlaced( World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int damage, EntityLivingBase placer )
    {
        return getDefaultBlockState( damage, side );
    }
    
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
    		boolean willHarvest) {
    	if( !world.isRemote )
    	{
            // Drop items
            int fortune = EnchantmentHelper.getLootingModifier(player);
            boolean creative = player.capabilities.isCreativeMode;
//            boolean silkTouch = EnchantmentHelper.getSilkTouchModifier( player );
            dropAllItems( world, pos, fortune, creative /*, silkTouch */, true);
        }

        // Remove block
        return super.removedByPlayer( state, world, pos, player, willHarvest );
    }


    public final void dropAllItems( World world, BlockPos pos, int fortune, boolean creative, boolean silkTouch )
    {
        // Get items to drop
        List<ItemStack> drops = new ArrayList<ItemStack>( 1 );
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            generic.getDroppedItems( drops, fortune, creative, silkTouch );
        }

        // Drop items
        if( drops.size() > 0 )
        {
            Iterator<ItemStack> it = drops.iterator();
            while( it.hasNext() )
            {
                ItemStack item = it.next();
                dropItem( world, pos, item );
            }
        }
    }

    public final void dropItem( World world, BlockPos pos, ItemStack stack )
    {
        Block.spawnAsEntity( world, pos, stack );
    }
    
    @Override
    public final void breakBlock( World world, BlockPos pos, IBlockState newState )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            generic.destroy();
        }
    	super.breakBlock( world, pos, newState );
        world.removeTileEntity( pos );
    }
    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
    		EntityPlayer player) {
    	TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            return generic.getPickedItem();
        }
        return null;
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
    		EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    	TileEntity tile = worldIn.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            return generic.onActivate( playerIn, facing, hitX, hitY, hitZ );
        }
        return false;
    }


    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
    	TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            generic.onNeighbourChange();
        }
    }
    
   @Override
public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
	   TileEntity tile = world.getTileEntity( pos );
       if( tile != null && tile instanceof TileGeneric )
       {
           TileGeneric generic = (TileGeneric)tile;
           return generic.isSolidOnSide( side.ordinal() );
       }
       return false;
}
   
   @Override
public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
	return false;
}

    @Override
    public float getExplosionResistance( World world, BlockPos pos, Entity exploder, Explosion explosion )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric && tile.hasWorld() )
        {
            TileGeneric generic = (TileGeneric)tile;
            if( generic.isImmuneToExplosion( exploder ) )
            {
                return 2000.0f;
            }
        }
        return super.getExplosionResistance( exploder );
    }

    private void setBlockBounds( AxisAlignedBB bounds )
    {
        setBlockBounds(
            (float)bounds.minX, (float)bounds.minY, (float)bounds.minZ,
            (float)bounds.maxX, (float)bounds.maxY, (float)bounds.maxZ
        );
    }
    

    @Override
    public final void setBlockBoundsBasedOnState( IBlockAccess world, BlockPos pos )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric && tile.hasWorld() )
        {
            TileGeneric generic = (TileGeneric)tile;
            setBlockBounds( generic.getBounds() );
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
    	setBlockBoundsBasedOnState( worldIn, pos );
        return super.getCollisionBoundingBox( worldIn, pos, blockState );
    }
    
    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB bigBox,
    		List<AxisAlignedBB> list, Entity entityIn, boolean isActualState) {
    	TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;

            // Get collision bounds
            List<AxisAlignedBB> collision = new ArrayList<AxisAlignedBB>( 1 );
            generic.getCollisionBounds( collision );

            // Add collision bounds to list
            if( collision.size() > 0 )
            {
                Iterator<AxisAlignedBB> it = collision.iterator();
                while( it.hasNext() )
                {
                    AxisAlignedBB localBounds = it.next();
                    setBlockBounds( localBounds );

                    AxisAlignedBB bounds = super.getCollisionBoundingBox( state, world, pos);
                    if( bounds != null && bigBox.intersects(bounds) )
                    {
                        list.add( bounds );
                    }
                }
            }
        }
    }
    
    @Override
    public boolean canProvidePower(IBlockState state) {
    	return true;
    }
    
    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
    	TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            return generic.getRedstoneConnectivity( side );
        }
        return false;
    }
    
    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
    	TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric && tile.hasWorld() )
        {
            TileGeneric generic = (TileGeneric)tile;
            return generic.getRedstoneOutput( side.getOpposite() );
        }
        return 0;
    }
    
    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
    	return getStrongPower( state, world, pos, side );
    }

    public boolean getBundledRedstoneConnectivity( World world, BlockPos pos, EnumFacing side )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            return generic.getBundledRedstoneConnectivity( side );
        }
        return false;
    }

    public int getBundledRedstoneOutput( World world, BlockPos pos, EnumFacing side )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric && tile.hasWorld() )
        {
            TileGeneric generic = (TileGeneric)tile;
            return generic.getBundledRedstoneOutput( side );
        }
        return 0;
    }

    @Override
    public boolean onBlockEventReceived( World world, BlockPos pos, IBlockState state, int eventID, int eventParameter )
    {
        if( world.isRemote )
        {
            TileEntity tile = world.getTileEntity( pos );
            if( tile != null && tile instanceof TileGeneric )
            {
                TileGeneric generic = (TileGeneric)tile;
                generic.onBlockEvent( eventID, eventParameter );
            }
        }
        return true;
    }

    @Override
    public final TileEntity createTileEntity( World world, IBlockState state )
    {
        return createTile( state );
    }

    @Override
    public final TileEntity createNewTileEntity( World world, int damage )
    {
        return createTile( damage );
    }
}
