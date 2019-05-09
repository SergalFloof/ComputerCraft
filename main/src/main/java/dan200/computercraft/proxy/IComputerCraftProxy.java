/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.proxy;

import dan200.computercraft.shared.computer.blocks.TileComputer;
import dan200.computercraft.shared.network.ComputerCraftPacket;
import dan200.computercraft.shared.peripheral.diskdrive.TileDiskDrive;
import dan200.computercraft.shared.peripheral.printer.TilePrinter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.io.File;

public interface IComputerCraftProxy
{
	public void preInit();
	public void init();
	public boolean isClient();

	public boolean getGlobalCursorBlink();
    public long getRenderFrame();
    public void deleteDisplayLists( int list, int range );
	public Object getFixedWidthFontRenderer();

	public String getRecordInfo( ItemStack item, World world );
	public void playRecord( String record, String recordInfo, World world, BlockPos pos );

	public Object getDiskDriveGUI( InventoryPlayer inventory, TileDiskDrive drive );
	public Object getComputerGUI( TileComputer computer );
	public Object getPrinterGUI( InventoryPlayer inventory, TilePrinter printer );
    public abstract Object getPrintoutGUI( InventoryPlayer inventory );
    public abstract Object getPocketComputerGUI( InventoryPlayer inventory );

	public File getWorldDir( World world );
	public void handlePacket( ComputerCraftPacket packet, EntityPlayer player, World world );
}
