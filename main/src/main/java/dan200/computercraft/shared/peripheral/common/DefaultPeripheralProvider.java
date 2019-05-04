/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.common;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.computer.blocks.ComputerPeripheral;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class DefaultPeripheralProvider implements IPeripheralProvider
{
    public DefaultPeripheralProvider()
    {
    }

    @Override
    public IPeripheral getPeripheral( World world, BlockPos pos, EnumFacing side )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null )
        {
            // Handle our peripherals
            if( tile instanceof IPeripheralTile )
            {
                IPeripheralTile peripheralTile = (IPeripheralTile)tile;
                return peripheralTile.getPeripheral( side );
            }

            
        }
        return null;
    }
}
