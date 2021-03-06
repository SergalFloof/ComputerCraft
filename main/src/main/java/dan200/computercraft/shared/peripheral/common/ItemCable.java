/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.common;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemCable extends ItemPeripheralBase
{
	public ItemCable( Block block )
    {
        super( block );
        setUnlocalizedName( "computercraft:cable" );
        setCreativeTab( ComputerCraft.mainCreativeTab );
    }

    public ItemStack create( PeripheralType type, String label, int quantity )
    {
        ItemStack stack;
        switch( type )
        {
            case Cable:
            {
                stack = new ItemStack( this, quantity, 0 );
                break;
            }
            case WiredModem:
            {
                stack = new ItemStack( this, quantity, 1 );
                break;
            }
            default:
            {
                return null;
            }
        }
        if( label != null )
        {
            stack.setStackDisplayName( label );
        }
        return stack;
    }
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
    	list.add( PeripheralItemFactory.create( PeripheralType.WiredModem, null, 1 ) );
        list.add( PeripheralItemFactory.create( PeripheralType.Cable, null, 1 ) );
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    	
    	IBlockState cState = world.getBlockState(pos);
		ItemStack stack = player.getHeldItem(hand);
    	
    	if( !canPlaceBlockOnSide( world, pos, side, player, stack ) )
    	{
    		return EnumActionResult.FAIL;
    	}

        // Try to add a cable to a modem
    	PeripheralType type = getPeripheralType( stack );
    	Block existing = world.getBlockState( pos ).getBlock();
        IBlockState existingState = world.getBlockState( pos );
    	if( existing == ComputerCraft.Blocks.cable )
    	{
    		PeripheralType existingType = ComputerCraft.Blocks.cable.getPeripheralType( world, pos );
    		if( existingType == PeripheralType.WiredModem && type == PeripheralType.Cable )
    		{
				if( stack.getCount() > 0 )
				{
					world.setBlockState( pos, existingState.withProperty( BlockCable.Properties.CABLE, true ), 3 );
	                world.playSound(player, pos, ComputerCraft.Blocks.cable.getSoundType().getBreakSound(), SoundCategory.BLOCKS, (ComputerCraft.Blocks.cable.getSoundType().getVolume() + 1.0F) / 2.0F, ComputerCraft.Blocks.cable.getSoundType().getPitch() * 0.8F);
	    			stack.shrink(-1);;
	    			
					TileEntity tile = world.getTileEntity( pos );
					if( tile != null && tile instanceof TileCable )
					{
						TileCable cable = (TileCable)tile;
						cable.networkChanged();
					}
	    			return EnumActionResult.SUCCESS;
	    		}
	    		return EnumActionResult.FAIL;
    		}
    	}

        // Try to add on the side of something
    	if( existing != Blocks.AIR && (type == PeripheralType.Cable || existing.isSideSolid(cState, world, pos, side )) )
    	{
            BlockPos offset = pos.offset( side );
			Block offsetExisting = world.getBlockState( offset ).getBlock();
            IBlockState offsetExistingState = world.getBlockState( offset );
			if( offsetExisting == ComputerCraft.Blocks.cable )
			{
                // Try to add a modem to a cable
                PeripheralType offsetExistingType = ComputerCraft.Blocks.cable.getPeripheralType( world, offset );
				if( offsetExistingType == PeripheralType.Cable && type == PeripheralType.WiredModem )
				{
					if( stack.getCount() > 0 )
					{
                        world.setBlockState( offset, offsetExistingState.withProperty( BlockCable.Properties.MODEM, BlockCableModemVariant.fromFacing( side.getOpposite() ) ), 3 );
                        world.playSound(player, pos, ComputerCraft.Blocks.cable.getSoundType().getBreakSound(), SoundCategory.BLOCKS, (ComputerCraft.Blocks.cable.getSoundType().getVolume() + 1.0F) / 2.0F, ComputerCraft.Blocks.cable.getSoundType().getPitch() * 0.8F);						stack.shrink(-1);;

						TileEntity tile = world.getTileEntity( offset );
						if( tile != null && tile instanceof TileCable )
						{
							TileCable cable = (TileCable)tile;
							cable.networkChanged();
						}
						return EnumActionResult.SUCCESS;
					}
					return EnumActionResult.FAIL;
				}

                // Try to add a cable to a modem
                if( offsetExistingType == PeripheralType.WiredModem && type == PeripheralType.Cable )
				{
					if( stack.getCount() > 0 )
					{
                        world.setBlockState( offset, offsetExistingState.withProperty( BlockCable.Properties.CABLE, true ), 3 );
                        SoundType sound = block.getSoundType(cState, world, pos, player);
                        world.playSound(player, pos, ComputerCraft.Blocks.cable.getSoundType().getBreakSound(), SoundCategory.BLOCKS, (ComputerCraft.Blocks.cable.getSoundType().getVolume() + 1.0F) / 2.0F, ComputerCraft.Blocks.cable.getSoundType().getPitch() * 0.8F);
						stack.shrink(1);;

						TileEntity tile = world.getTileEntity( offset );
						if( tile != null && tile instanceof TileCable )
						{
							TileCable cable = (TileCable)tile;
							cable.networkChanged();
						}
						return EnumActionResult.SUCCESS;
					}
					return EnumActionResult.FAIL;
				}
			}
		}
    	
    	return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public PeripheralType getPeripheralType( int damage )
    {
        switch( damage )
        {
            case 0:
            default:
            {
                return PeripheralType.Cable;
            }
            case 1:
            {
                return PeripheralType.WiredModem;
            }
        }
    }
}
