/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.server.proxy;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.TileComputer;
import dan200.computercraft.shared.peripheral.diskdrive.TileDiskDrive;
import dan200.computercraft.shared.peripheral.printer.TilePrinter;
import dan200.computercraft.shared.proxy.ComputerCraftProxyCommon;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.io.File;

public class ComputerCraftProxyServer extends ComputerCraftProxyCommon
{
	
	public void registerItemRenderer(Item item, int meta, String id) {};
	public void registerVariantRenderer(Item item, int meta, String filename, String id) {};
	public ComputerCraftProxyServer()
	{
	}
	
	// IComputerCraftProxy implementation
	
	@Override
	public void init()
	{
		super.init();
	}

    @Override
	public boolean isClient()
	{
		return false;
	}

	@Override
	public boolean getGlobalCursorBlink()
	{
		return false;
	}

    @Override
    public long getRenderFrame()
    {
        return 0;
    }

    @Override
	public Object getFixedWidthFontRenderer()
	{
		return null;
	}
	
	@Override
	public void playRecord( String record, String recordInfo, World world, BlockPos pos )
	{
	}

	@Override
	public Object getDiskDriveGUI( InventoryPlayer inventory, TileDiskDrive drive )
	{
		return null;
	}
	
	@Override
	public Object getComputerGUI( TileComputer computer )
	{
		return null;
	}

	@Override
	public Object getPrinterGUI( InventoryPlayer inventory, TilePrinter printer )
	{
		return null;
	}

    @Override
    public Object getPrintoutGUI( InventoryPlayer inventory )
    {
        return null;
    }

    @Override
    public Object getPocketComputerGUI( InventoryPlayer inventory )
    {
        return null;
    }

	@Override
	public File getWorldDir( World world )
	{
		return new File( ComputerCraft.getBaseDir(), DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory() );
	}
}
