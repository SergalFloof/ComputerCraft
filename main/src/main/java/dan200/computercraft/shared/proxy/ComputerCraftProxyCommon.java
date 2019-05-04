/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.proxy;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.core.computer.MainThread;
import dan200.computercraft.shared.common.DefaultBundledRedstoneProvider;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.computer.blocks.BlockCommandComputer;
import dan200.computercraft.shared.computer.blocks.BlockComputer;
import dan200.computercraft.shared.computer.blocks.TileCommandComputer;
import dan200.computercraft.shared.computer.blocks.TileComputer;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.inventory.ContainerComputer;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.computer.items.ItemCommandComputer;
import dan200.computercraft.shared.computer.items.ItemComputer;
import dan200.computercraft.shared.media.common.DefaultMediaProvider;
import dan200.computercraft.shared.media.inventory.ContainerHeldItem;
import dan200.computercraft.shared.media.items.ItemDiskExpanded;
import dan200.computercraft.shared.media.items.ItemDiskLegacy;
import dan200.computercraft.shared.media.items.ItemPrintout;
import dan200.computercraft.shared.media.items.ItemTreasureDisk;
import dan200.computercraft.shared.media.recipes.DiskRecipe;
import dan200.computercraft.shared.media.recipes.PrintoutRecipe;
import dan200.computercraft.shared.network.ComputerCraftPacket;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.commandblock.CommandBlockPeripheralProvider;
import dan200.computercraft.shared.peripheral.common.*;
import dan200.computercraft.shared.peripheral.diskdrive.ContainerDiskDrive;
import dan200.computercraft.shared.peripheral.diskdrive.TileDiskDrive;
import dan200.computercraft.shared.peripheral.modem.BlockAdvancedModem;
import dan200.computercraft.shared.peripheral.modem.TileAdvancedModem;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import dan200.computercraft.shared.peripheral.modem.TileWirelessModem;
import dan200.computercraft.shared.peripheral.monitor.TileMonitor;
import dan200.computercraft.shared.peripheral.printer.ContainerPrinter;
import dan200.computercraft.shared.peripheral.printer.TilePrinter;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import dan200.computercraft.shared.pocket.recipes.PocketComputerUpgradeRecipe;
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.CreativeTabMain;
import dan200.computercraft.shared.util.ImpostorRecipe;
import dan200.computercraft.shared.util.ImpostorShapelessRecipe;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;

import java.io.File;

public abstract class ComputerCraftProxyCommon implements IComputerCraftProxy
{
	public ComputerCraftProxyCommon()
	{
	}
	
	// IComputerCraftProxy implementation

	@Override
	public void preInit()
	{
		registerItems();
	}
	
	@Override		
	public void init()
	{
		registerTileEntities();
		registerForgeHandlers();
	}
	
	@Override
	public abstract boolean isClient();
	
	@Override
	public abstract boolean getGlobalCursorBlink();

    @Override
    public abstract long getRenderFrame();

    @Override
    public void deleteDisplayLists( int list, int range )
    {
    }

    @Override
	public abstract Object getFixedWidthFontRenderer();
	
	@Override
	public String getRecordInfo( ItemStack recordStack, World world )
	{
		Item item = recordStack.getItem();
		if( item instanceof ItemRecord )
		{
			ItemRecord record = (ItemRecord)item;
            if( (( ResourceLocation)Item.REGISTRY.getNameForObject( record )).getResourceDomain().equals( "minecraft" ) )
			{
				return "C418 - " + record.getRecordNameLocal();
			}
			// TODO: determine descriptions for mod items (ie: Portal gun mod)
			return record.getRecordNameLocal();
		}
		return null;
	}
		
	@Override
	public abstract void playRecord( String record, String recordInfo, World world, BlockPos pos );
	
	@Override
	public abstract Object getDiskDriveGUI( InventoryPlayer inventory, TileDiskDrive drive );
	
	@Override
	public abstract Object getComputerGUI( TileComputer computer );

	@Override
	public abstract Object getPrinterGUI( InventoryPlayer inventory, TilePrinter printer );

	@Override
	public abstract Object getPrintoutGUI( InventoryPlayer inventory );

    @Override
    public abstract Object getPocketComputerGUI( InventoryPlayer inventory );

	public abstract File getWorldDir( World world );
	
	@Override 
	public void handlePacket( final ComputerCraftPacket packet, final EntityPlayer player, World world)
    {
        IThreadListener listener = world.getMinecraftServer();
        if( listener != null )
        {
            if( listener.isCallingFromMinecraftThread())
            {
                processPacket( packet, player );
            }
            else
            {
                listener.addScheduledTask( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        processPacket( packet, player );
                    }
                } );
            }
        }
    }

    private void processPacket( ComputerCraftPacket packet, EntityPlayer player )
    {
		switch( packet.m_packetType )
		{
			///////////////////////////////////
			// Packets from Client to Server //
			///////////////////////////////////
            case ComputerCraftPacket.TurnOn:
            case ComputerCraftPacket.Shutdown:
            case ComputerCraftPacket.Reboot:
            case ComputerCraftPacket.QueueEvent:
            case ComputerCraftPacket.RequestComputerUpdate:
            case ComputerCraftPacket.SetLabel:
            {
                int instance = packet.m_dataInt[0];
                ServerComputer computer = ComputerCraft.serverComputerRegistry.get( instance );
                if( computer != null )
                {
                    computer.handlePacket( packet, player );
                }
                break;
            }
            case ComputerCraftPacket.RequestTileEntityUpdate:
            {
                int x = packet.m_dataInt[0];
                int y = packet.m_dataInt[1];
                int z = packet.m_dataInt[2];
                BlockPos pos = new BlockPos( x, y, z );
                World world = player.getEntityWorld();
                TileEntity tileEntity = world.getTileEntity( pos );
                if( tileEntity != null && tileEntity instanceof TileGeneric )
                {
                    TileGeneric generic = (TileGeneric)tileEntity;
                    Packet description = generic.getDescriptionPacket();
                    if( description != null )
                    {
                        ((EntityPlayerMP)player).playerNetServerHandler.sendPacket( description );
                    }
                }
                break;
            }
		}
	}

	private void registerItems()
	{
		// Creative tab
		ComputerCraft.mainCreativeTab = new CreativeTabMain( CreativeTabs.getNextID() );

        // Blocks
		// Computer
		ComputerCraft.Blocks.computer = new BlockComputer();
		GameRegistry.registerBlock( ComputerCraft.Blocks.computer, ItemComputer.class, "CC-Computer" );

        // Peripheral
        ComputerCraft.Blocks.peripheral = new BlockPeripheral();
        GameRegistry.registerBlock( ComputerCraft.Blocks.peripheral, ItemPeripheral.class, "CC-Peripheral" );

        // Cable
        ComputerCraft.Blocks.cable = new BlockCable();
        GameRegistry.registerBlock( ComputerCraft.Blocks.cable, ItemCable.class, "CC-Cable" );

        // Command Computer
        ComputerCraft.Blocks.commandComputer = new BlockCommandComputer();
        GameRegistry.registerBlock( ComputerCraft.Blocks.commandComputer, ItemCommandComputer.class, "command_computer" );

        // Command Computer
        ComputerCraft.Blocks.advancedModem = new BlockAdvancedModem();
        GameRegistry.registerBlock( ComputerCraft.Blocks.advancedModem, ItemAdvancedModem.class, "advanced_modem" );

        // Items
        // Floppy Disk
        ComputerCraft.Items.disk = new ItemDiskLegacy();
        GameRegistry.registerItem( ComputerCraft.Items.disk, "disk" );

        ComputerCraft.Items.diskExpanded = new ItemDiskExpanded();
        GameRegistry.registerItem( ComputerCraft.Items.diskExpanded, "diskExpanded" );

        // Treasure Disk
        ComputerCraft.Items.treasureDisk = new ItemTreasureDisk();
        GameRegistry.registerItem( ComputerCraft.Items.treasureDisk, "treasureDisk" );

        // Printout
        ComputerCraft.Items.printout = new ItemPrintout();
        GameRegistry.registerItem( ComputerCraft.Items.printout, "printout" );

        // Pocket computer
        ComputerCraft.Items.pocketComputer = new ItemPocketComputer();
        GameRegistry.registerItem( ComputerCraft.Items.pocketComputer, "pocketComputer" );

        // Recipe types
//        RecipeSorter.register( "computercraft:impostor", ImpostorRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shapeless" );
//        RecipeSorter.register( "computercraft:impostor_shapeless", ImpostorShapelessRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless" );
//        RecipeSorter.register( "computercraft:disk", DiskRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless" );
//        RecipeSorter.register( "computercraft:printout", PrintoutRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless" );
//        RecipeSorter.register( "computercraft:pocket_computer_upgrade", PocketComputerUpgradeRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless" );

       

        // Disk
		GameRegistry.addRecipe( new DiskRecipe() );

        // Impostor Disk recipes (to fool NEI)
		ItemStack paper = new ItemStack( Items.paper, 1 );
		ItemStack redstone = new ItemStack( Items.redstone, 1 );
		ItemStack basicDisk = ItemDiskLegacy.createFromIDAndColour( -1, null, Colour.Blue.getHex() );
		GameRegistry.addRecipe( new ImpostorShapelessRecipe( basicDisk, new Object[] { redstone, paper } ) );

		for( int colour=0; colour<16; ++colour )
		{
			ItemStack disk = ItemDiskLegacy.createFromIDAndColour( -1, null, Colour.values()[ colour ].getHex() );
			ItemStack dye = new ItemStack( Items.dye, 1, colour );
			for( int otherColour=0; otherColour<16; ++otherColour )
			{
				if( colour != otherColour )
				{
					ItemStack otherDisk = ItemDiskLegacy.createFromIDAndColour( -1, null, Colour.values()[ colour ].getHex() );
					GameRegistry.addRecipe( new ImpostorShapelessRecipe( disk, new Object[] {
                        otherDisk, dye
                    } ) );
				}
			}
			GameRegistry.addRecipe( new ImpostorShapelessRecipe( disk, new Object[] {
                redstone, paper, dye
            } ) );
		}

		// Printout
        GameRegistry.addRecipe( new PrintoutRecipe() );

		ItemStack singlePrintout = ItemPrintout.createSingleFromTitleAndText( null, null, null );
		ItemStack multiplePrintout = ItemPrintout.createMultipleFromTitleAndText( null, null, null );
		ItemStack bookPrintout = ItemPrintout.createBookFromTitleAndText( null, null, null );

		// Impostor Printout recipes (to fool NEI)
		ItemStack string = new ItemStack( Items.string, 1, 0 );
		GameRegistry.addRecipe( new ImpostorShapelessRecipe( multiplePrintout, new Object[] { singlePrintout, singlePrintout, string } ) );

		ItemStack leather = new ItemStack( Items.leather, 1, 0 );
		GameRegistry.addRecipe( new ImpostorShapelessRecipe( bookPrintout, new Object[] { leather, singlePrintout, string } ) );

        

        // Wireless Pocket Computer
        ItemStack wirelessPocketComputer = PocketComputerItemFactory.create( -1, null, ComputerFamily.Normal, true );
        GameRegistry.addRecipe( new PocketComputerUpgradeRecipe() );

        // Advanced Wireless Pocket Computer
        ItemStack advancedWirelessPocketComputer = PocketComputerItemFactory.create( -1, null, ComputerFamily.Advanced, true );

        // Impostor Pocket Computer recipes (to fool NEI)
        GameRegistry.addRecipe( new ImpostorRecipe( 1, 2, new ItemStack[] { wirelessModem, pocketComputer }, wirelessPocketComputer ) );
        GameRegistry.addRecipe( new ImpostorRecipe( 1, 2, new ItemStack[] { wirelessModem, advancedPocketComputer }, advancedWirelessPocketComputer ) );

       
	}
	
	private void registerTileEntities()
	{
		// Tile Entities
		GameRegistry.registerTileEntity( TileComputer.class, "computer" );
		GameRegistry.registerTileEntity( TileDiskDrive.class, "diskdrive" );
		GameRegistry.registerTileEntity( TileWirelessModem.class, "wirelessmodem" );
		GameRegistry.registerTileEntity( TileMonitor.class, "monitor" );
		GameRegistry.registerTileEntity( TilePrinter.class, "ccprinter" );
		GameRegistry.registerTileEntity( TileCable.class, "wiredmodem" );
        GameRegistry.registerTileEntity( TileCommandComputer.class, "command_computer" );
        GameRegistry.registerTileEntity( TileAdvancedModem.class, "advanced_modem" );

		// Register peripheral providers
        ComputerCraftAPI.registerPeripheralProvider( new DefaultPeripheralProvider() );
		if( ComputerCraft.enableCommandBlock )
		{
			ComputerCraftAPI.registerPeripheralProvider( new CommandBlockPeripheralProvider() );
		}

        // Register bundled power providers
        ComputerCraftAPI.registerBundledRedstoneProvider( new DefaultBundledRedstoneProvider() );

        // Register media providers
        ComputerCraftAPI.registerMediaProvider( new DefaultMediaProvider() );
	}
		
	private void registerForgeHandlers()
	{
		ForgeHandlers handlers = new ForgeHandlers();
        MinecraftForge.EVENT_BUS.register( handlers );
		NetworkRegistry.INSTANCE.registerGuiHandler( ComputerCraft.instance, handlers );
	}
			
	public class ForgeHandlers implements
		IGuiHandler
	{
		private ForgeHandlers()
		{
		}

		// IGuiHandler implementation
		
		@Override
		public Object getServerGuiElement( int id, EntityPlayer player, World world, int x, int y, int z )
		{
            BlockPos pos = new BlockPos( x, y, z );
			switch( id )
			{
				case ComputerCraft.diskDriveGUIID:
				{
                    TileEntity tile = world.getTileEntity( pos );
					if( tile != null && tile instanceof TileDiskDrive )
					{
						TileDiskDrive drive = (TileDiskDrive)tile;
						return new ContainerDiskDrive( player.inventory, drive );
					}
					break;
				}
				case ComputerCraft.computerGUIID:
				{
                    TileEntity tile = world.getTileEntity( pos );
					if( tile != null && tile instanceof TileComputer )
					{
						TileComputer computer = (TileComputer)tile;
						return new ContainerComputer( computer );
					}
					break;
				}
				case ComputerCraft.printerGUIID:
				{
                    TileEntity tile = world.getTileEntity( pos );
					if( tile != null && tile instanceof TilePrinter )
					{
						TilePrinter printer = (TilePrinter)tile;
						return new ContainerPrinter( player.inventory, printer );
					}
					break;
				}
                case ComputerCraft.printoutGUIID:
                {
                    return new ContainerHeldItem( player.inventory );
                }
                case ComputerCraft.pocketComputerGUIID:
                {
                    return new ContainerHeldItem( player.inventory );
                }
            }
			return null;
		}

		@Override
		public Object getClientGuiElement( int id, EntityPlayer player, World world, int x, int y, int z )
		{
            BlockPos pos = new BlockPos( x, y, z );
			switch( id )
			{
				case ComputerCraft.diskDriveGUIID:
				{
                    TileEntity tile = world.getTileEntity( pos );
					if( tile != null && tile instanceof TileDiskDrive )
					{
						TileDiskDrive drive = (TileDiskDrive)tile;
						return getDiskDriveGUI( player.inventory, drive );
					}
					break;
				}
				case ComputerCraft.computerGUIID:
				{
                    TileEntity tile = world.getTileEntity( pos );
					if( tile != null && tile instanceof TileComputer )
					{
						TileComputer computer = (TileComputer)tile;
						return getComputerGUI( computer );
					}
					break;
				}
				case ComputerCraft.printerGUIID:
				{
                    TileEntity tile = world.getTileEntity( pos );
					if( tile != null && tile instanceof TilePrinter )
					{
						TilePrinter printer = (TilePrinter)tile;
						return getPrinterGUI( player.inventory, printer );
					}
					break;
				}
                case ComputerCraft.printoutGUIID:
                {
                    return getPrintoutGUI( player.inventory );
                }
                case ComputerCraft.pocketComputerGUIID:
                {
                    return getPocketComputerGUI( player.inventory );
                }
            }
            return null;
        }

        // Event handlers

        @SubscribeEvent
        public void onConnectionOpened( FMLNetworkEvent.ClientConnectedToServerEvent event )
        {
            ComputerCraft.clientComputerRegistry.reset();
        }

        @SubscribeEvent
        public void onConnectionClosed( FMLNetworkEvent.ClientDisconnectionFromServerEvent event )
        {
            ComputerCraft.clientComputerRegistry.reset();
        }

        @SubscribeEvent
        public void onClientTick( TickEvent.ClientTickEvent event )
        {
            if( event.phase == TickEvent.Phase.START )
            {
                ComputerCraft.clientComputerRegistry.update();
            }
        }

        @SubscribeEvent
        public void onServerTick( TickEvent.ServerTickEvent event )
        {
            if( event.phase == TickEvent.Phase.START )
            {
                MainThread.executePendingTasks();
                ComputerCraft.serverComputerRegistry.update();
            }
        }

        @SubscribeEvent
        public void onWorldLoad( WorldEvent.Load event )
        {
        }

        @SubscribeEvent
        public void onWorldUnload( WorldEvent.Unload event )
        {
        }
    }
}
