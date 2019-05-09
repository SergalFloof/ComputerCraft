/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
 
package dan200.computercraft;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.api.media.IMediaProvider;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.api.redstone.IBundledRedstoneProvider;
import dan200.computercraft.core.filesystem.ComboMount;
import dan200.computercraft.core.filesystem.FileMount;
import dan200.computercraft.core.filesystem.JarMount;
import dan200.computercraft.proxy.IComputerCraftProxy;
import dan200.computercraft.shared.common.DefaultBundledRedstoneProvider;
import dan200.computercraft.shared.computer.blocks.BlockCommandComputer;
import dan200.computercraft.shared.computer.blocks.BlockComputer;
import dan200.computercraft.shared.computer.blocks.TileComputer;
import dan200.computercraft.shared.computer.core.ClientComputerRegistry;
import dan200.computercraft.shared.computer.core.ServerComputerRegistry;
import dan200.computercraft.shared.media.items.ItemDiskExpanded;
import dan200.computercraft.shared.media.items.ItemDiskLegacy;
import dan200.computercraft.shared.media.items.ItemPrintout;
import dan200.computercraft.shared.media.items.ItemTreasureDisk;
import dan200.computercraft.shared.network.ComputerCraftPacket;
import dan200.computercraft.shared.network.PacketHandler;
import dan200.computercraft.shared.peripheral.common.BlockCable;
import dan200.computercraft.shared.peripheral.common.BlockPeripheral;
import dan200.computercraft.shared.peripheral.diskdrive.TileDiskDrive;
import dan200.computercraft.shared.peripheral.modem.BlockAdvancedModem;
import dan200.computercraft.shared.peripheral.modem.WirelessNetwork;
import dan200.computercraft.shared.peripheral.printer.TilePrinter;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.util.CreativeTabMain;
import dan200.computercraft.shared.util.IDAssigner;
import dan200.computercraft.shared.util.IEntityDropConsumer;
import dan200.computercraft.shared.util.WorldUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

///////////////
// UNIVERSAL //
///////////////

@Mod( modid = "ComputerCraft", name = "ComputerCraft", version = "${version}" )
public class ComputerCraft
{
	// GUI IDs
	public static final int diskDriveGUIID = 100;
	public static final int computerGUIID = 101;
	public static final int printerGUIID = 102;
	// ComputerCraftEdu uses ID 104
    public static final int printoutGUIID = 105;
    public static final int pocketComputerGUIID = 106;

	// Configuration options
	public static boolean http_enable = true;
    public static String http_whitelist = "*";
    public static boolean disable_lua51_features = false;
    public static String default_computer_settings = "";

	public static boolean enableCommandBlock = false;


    public static final int terminalWidth_computer = 51;
	public static final int terminalHeight_computer = 19;

    public static final int terminalWidth_pocketComputer = 26;
    public static final int terminalHeight_pocketComputer = 20;

    public static int modem_range = 64;
	public static int modem_highAltitudeRange = 384;
	public static int modem_rangeDuringStorm = 64;
	public static int modem_highAltitudeRangeDuringStorm = 384;

	public static int computerSpaceLimit = 1000 * 1000;
	public static int floppySpaceLimit = 125 * 1000;

	public static int treasureDiskLootFrequency = 1;

    // Blocks and Items
	public static class Blocks
	{
		public static BlockComputer computer;
		public static BlockPeripheral peripheral;
		public static BlockCable cable;
        public static BlockCommandComputer commandComputer;
        public static BlockAdvancedModem advancedModem;
    }

	public static class Items
	{
		public static ItemDiskLegacy disk;
		public static ItemDiskExpanded diskExpanded;
		public static ItemPrintout printout;
		public static Item treasureDisk;
        public static ItemPocketComputer pocketComputer;
	}


    // Registries
    public static ClientComputerRegistry clientComputerRegistry = new ClientComputerRegistry();
    public static ServerComputerRegistry serverComputerRegistry = new ServerComputerRegistry();

    // Networking
    public static FMLEventChannel networkEventChannel;

	// Creative
	public static CreativeTabMain mainCreativeTab;

    // API users
	private static List<IPeripheralProvider> peripheralProviders = new ArrayList<IPeripheralProvider>();
    private static List<IBundledRedstoneProvider> bundledRedstoneProviders = new ArrayList<IBundledRedstoneProvider>();
    private static List<IMediaProvider> mediaProviders = new ArrayList<IMediaProvider>();

    // Implementation
	@Mod.Instance( value = "ComputerCraft" )
	public static ComputerCraft instance;

	@SidedProxy( clientSide = "dan200.computercraft.client.proxy.ComputerCraftProxyClient", serverSide = "dan200.computercraft.server.proxy.ComputerCraftProxyServer" )
	public static IComputerCraftProxy proxy;


	public ComputerCraft()
	{
	}

	@Mod.EventHandler
	public void preInit( FMLPreInitializationEvent event )
	{
        // Load config
		Configuration config = new Configuration( event.getSuggestedConfigurationFile() );
		config.load();

        // Setup general

		Property prop = config.get(Configuration.CATEGORY_GENERAL, "http_enable", http_enable);
		prop.comment = "Enable the \"http\" API on Computers (see \"http_whitelist\" for more fine grained control than this)";
        http_enable = prop.getBoolean(http_enable);

        prop = config.get(Configuration.CATEGORY_GENERAL, "http_whitelist", http_whitelist );
        prop.comment = "A semicolon limited list of wildcards for domains that can be accessed through the \"http\" API on Computers. Set this to \"*\" to access to the entire internet. Example: \"*.pastebin.com;*.github.com;*.computercraft.info\" will restrict access to just those 3 domains.";
        http_whitelist = prop.getString();

        prop = config.get(Configuration.CATEGORY_GENERAL, "disable_lua51_features", disable_lua51_features );
        prop.comment = "Set this to true to disable Lua 5.1 functions that will be removed in a future update. Useful for ensuring forward compatibility of your programs now.";
        disable_lua51_features = prop.getBoolean( disable_lua51_features );

        prop = config.get( Configuration.CATEGORY_GENERAL, "default_computer_settings", default_computer_settings );
        prop.comment = "A comma seperated list of default system settings to set on new computers. Example: \"shell.autocomplete=false,lua.autocomplete=false,edit.autocomplete=false\" will disable all autocompletion";
        default_computer_settings = prop.getString();

        prop = config.get(Configuration.CATEGORY_GENERAL, "enableCommandBlock", enableCommandBlock);
		prop.comment = "Enable Command Block peripheral support";
		enableCommandBlock = prop.getBoolean(enableCommandBlock);

		prop = config.get(Configuration.CATEGORY_GENERAL, "modem_range", modem_range);
		prop.comment = "The range of Wireless Modems at low altitude in clear weather, in meters";
		modem_range = Math.min( prop.getInt(), 100000 );

		prop = config.get(Configuration.CATEGORY_GENERAL, "modem_highAltitudeRange", modem_highAltitudeRange);
		prop.comment = "The range of Wireless Modems at maximum altitude in clear weather, in meters";
		modem_highAltitudeRange = Math.min( prop.getInt(), 100000 );

		prop = config.get(Configuration.CATEGORY_GENERAL, "modem_rangeDuringStorm", modem_rangeDuringStorm);
		prop.comment = "The range of Wireless Modems at low altitude in stormy weather, in meters";
		modem_rangeDuringStorm = Math.min( prop.getInt(), 100000 );

		prop = config.get(Configuration.CATEGORY_GENERAL, "modem_highAltitudeRangeDuringStorm", modem_highAltitudeRangeDuringStorm);
		prop.comment = "The range of Wireless Modems at maximum altitude in stormy weather, in meters";
		modem_highAltitudeRangeDuringStorm = Math.min( prop.getInt(), 100000 );

		prop = config.get(Configuration.CATEGORY_GENERAL, "computerSpaceLimit", computerSpaceLimit);
		prop.comment = "The disk space limit for computers and turtles, in bytes";
		computerSpaceLimit = prop.getInt();

		prop = config.get(Configuration.CATEGORY_GENERAL, "floppySpaceLimit", floppySpaceLimit);
		prop.comment = "The disk space limit for floppy disks, in bytes";
		floppySpaceLimit = prop.getInt();

		prop = config.get(Configuration.CATEGORY_GENERAL, "treasureDiskLootFrequency", treasureDiskLootFrequency);
		prop.comment = "The frequency that treasure disks will be found in dungeon chests, from 0 to 100. Increase this value if running a modpack with lots of mods that add dungeon loot, or you just want more treasure disks. Set to 0 to disable treasure disks.";
		treasureDiskLootFrequency = prop.getInt();

       

        config.save();

        // Setup network
        networkEventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel( "CC" );
        networkEventChannel.register( new PacketHandler() );

		proxy.preInit();
	}

	@Mod.EventHandler
	public void init( FMLInitializationEvent event )
	{
		proxy.init();
	}

//    @Mod.EventHandler
//    public void onServerStarting( FMLServerStartingEvent event )
//    {
//        ItemTreasureDisk.registerDungeonLoot();
//    }

    @Mod.EventHandler
    public void onServerStart( FMLServerStartedEvent event )
    {
        if( FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER )
        {
            ComputerCraft.serverComputerRegistry.reset();
            WirelessNetwork.resetNetworks();
        }
    }

    @Mod.EventHandler
    public void onServerStopped( FMLServerStoppedEvent event )
    {
        if( FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER )
        {
            ComputerCraft.serverComputerRegistry.reset();
            WirelessNetwork.resetNetworks();
        }
    }

    public static String getVersion()
    {
        return "${version}";
    }

    public static boolean isClient()
	{
		return proxy.isClient();
	}

	public static boolean getGlobalCursorBlink()
	{
		return proxy.getGlobalCursorBlink();
	}

    public static long getRenderFrame()
    {
        return proxy.getRenderFrame();
    }

    public static void deleteDisplayLists( int list, int range )
    {
        proxy.deleteDisplayLists( list, range );
    }

    public static Object getFixedWidthFontRenderer()
	{
		return proxy.getFixedWidthFontRenderer();
	}

	public static void playRecord( String record, String recordInfo, World world, BlockPos pos )
	{
		proxy.playRecord( record, recordInfo, world, pos );
	}

	public static String getRecordInfo( ItemStack recordStack, World world )
	{
		return proxy.getRecordInfo( recordStack, world );
	}

	public static void openDiskDriveGUI( EntityPlayer player, TileDiskDrive drive )
	{
        BlockPos pos = drive.getPos();
		player.openGui( ComputerCraft.instance, ComputerCraft.diskDriveGUIID, player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ() );
	}

	public static void openComputerGUI( EntityPlayer player, TileComputer computer )
	{
        BlockPos pos = computer.getPos();
		player.openGui( ComputerCraft.instance, ComputerCraft.computerGUIID, player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ() );
	}

	public static void openPrinterGUI( EntityPlayer player, TilePrinter printer )
	{
        BlockPos pos = printer.getPos();
		player.openGui( ComputerCraft.instance, ComputerCraft.printerGUIID, player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ() );
	}

	public static void openPrintoutGUI( EntityPlayer player )
	{
        player.openGui( ComputerCraft.instance, ComputerCraft.printoutGUIID, player.getEntityWorld(), 0, 0, 0 );
    }

    public static void openPocketComputerGUI( EntityPlayer player )
    {
        player.openGui( ComputerCraft.instance, ComputerCraft.pocketComputerGUIID, player.getEntityWorld(), 0, 0, 0 );
    }

    public static File getBaseDir()
	{
		return FMLCommonHandler.instance().getMinecraftServerInstance().getFile(".");
	}

	public static File getResourcePackDir()
	{
		return new File( getBaseDir(), "resourcepacks" );
	}

	public static File getWorldDir( World world )
	{
		return proxy.getWorldDir( world );
	}

    private static FMLProxyPacket encode( ComputerCraftPacket packet )
    {
        PacketBuffer buffer = new PacketBuffer( Unpooled.buffer() );
        packet.toBytes( buffer );
        return new FMLProxyPacket( buffer, "CC" );
    }

	public static void sendToPlayer( EntityPlayer player, ComputerCraftPacket packet )
	{
        networkEventChannel.sendTo( encode( packet ), (EntityPlayerMP)player );
	}

	public static void sendToAllPlayers( ComputerCraftPacket packet )
	{
        networkEventChannel.sendToAll( encode( packet ) );
	}

	public static void sendToServer( ComputerCraftPacket packet )
	{
        networkEventChannel.sendToServer( encode( packet ) );
	}

	public static void handlePacket( ComputerCraftPacket packet, EntityPlayer player, World world )
	{
		proxy.handlePacket( packet, player, world );
	}

    public static boolean canPlayerUseCommands( EntityPlayer player, World world )
    {
        MinecraftServer server = world.getMinecraftServer();
        if( server != null )
        {
            return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().canSendCommands( player.getGameProfile() );
        }
        return false;
    }

    public static boolean isPlayerOpped( EntityPlayer player, World world )
    {
    	MinecraftServer server = world.getMinecraftServer();
        if( server != null )
        {
            return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getOppedPlayers().getEntry( player.getGameProfile() ) != null;
        }
        return false;
    }

    public static void registerPeripheralProvider( IPeripheralProvider provider )
	{
        if( provider != null && !peripheralProviders.contains( provider ) )
        {
            peripheralProviders.add( provider );
        }
	}

    public static void registerBundledRedstoneProvider( IBundledRedstoneProvider provider )
    {
        if( provider != null && !bundledRedstoneProviders.contains( provider ) )
        {
            bundledRedstoneProviders.add( provider );
        }
    }

    public static void registerMediaProvider( IMediaProvider provider )
    {
        if( provider != null && !mediaProviders.contains( provider ) )
        {
            mediaProviders.add( provider );
        }
    }

    public static IPeripheral getPeripheralAt( World world, BlockPos pos, EnumFacing side )
	{
		// Try the handlers in order:
	    Iterator<IPeripheralProvider> it = peripheralProviders.iterator();
	    while( it.hasNext() )
	    {
            try
            {
                IPeripheralProvider handler = it.next();
                IPeripheral peripheral = handler.getPeripheral( world, pos, side );
                if( peripheral != null )
                {
                    return peripheral;
                }
            }
            catch( Exception e )
            {
                // mod misbehaved, ignore it
            }
	    }
	    return null;
	}

    public static int getDefaultBundledRedstoneOutput( World world, BlockPos pos, EnumFacing side )
    {
        if( WorldUtil.isBlockInWorld( world, pos ) )
        {
            return DefaultBundledRedstoneProvider.getDefaultBundledRedstoneOutput( world, pos, side );
        }
        return -1;
    }

    public static int getBundledRedstoneOutput( World world, BlockPos pos, EnumFacing side )
    {
        int y = pos.getY();
        if( y < 0 || y >= world.getHeight() )
        {
            return -1;
        }

        // Try the handlers in order:
        int combinedSignal = -1;
        Iterator<IBundledRedstoneProvider> it = bundledRedstoneProviders.iterator();
        while( it.hasNext() )
        {
            try
            {
                IBundledRedstoneProvider handler = it.next();
                int signal = handler.getBundledRedstoneOutput( world, pos, side );
                if( signal >= 0 )
                {
                    if( combinedSignal < 0 )
                    {
                        combinedSignal = (signal & 0xffff);
                    }
                    else
                    {
                        combinedSignal = combinedSignal | (signal & 0xffff);
                    }
                }
            }
            catch( Exception e )
            {
                // mod misbehaved, ignore it
            }
        }
        return combinedSignal;
    }

    public static IMedia getMedia( ItemStack stack )
    {
        if( stack != null )
        {
            // Try the handlers in order:
            Iterator<IMediaProvider> it = mediaProviders.iterator();
            while( it.hasNext() )
            {
                try
                {
                    IMediaProvider handler = it.next();
                    IMedia media = handler.getMedia( stack );
                    if( media != null )
                    {
                        return media;
                    }
                }
                catch( Exception e )
                {
                    // mod misbehaved, ignore it
                }
            }
            return null;
        }
        return null;
    }

	public static int createUniqueNumberedSaveDir( World world, String parentSubPath )
	{
		return IDAssigner.getNextIDFromDirectory(new File(getWorldDir(world), parentSubPath));
	}

	public static IWritableMount createSaveDirMount( World world, String subPath, long capacity )
	{
		try
		{
			return new FileMount( new File( getWorldDir( world ), subPath ), capacity );
		}
		catch( Exception e )
		{
			return null;
		}
	}

	public static IMount createResourceMount( Class modClass, String domain, String subPath )
	{
        // Start building list of mounts
        List<IMount> mounts = new ArrayList<IMount>();
        subPath = "assets/" + domain + "/" + subPath;

        // Mount from debug dir
        File codeDir = getDebugCodeDir( modClass );
        if( codeDir != null )
        {
            File subResource = new File( codeDir, subPath );
            if( subResource.exists() )
            {
                IMount resourcePackMount = new FileMount( subResource, 0 );
                mounts.add( resourcePackMount );
            }
        }

        // Mount from mod jar
        File modJar = getContainingJar( modClass );
        if( modJar != null )
        {
            try
            {
                IMount jarMount = new JarMount( modJar, subPath );
                mounts.add( jarMount );
            }
            catch( IOException e )
            {
                // Ignore
            }
        }

        // Mount from resource packs
        File resourcePackDir = getResourcePackDir();
        if( resourcePackDir.exists() && resourcePackDir.isDirectory() )
        {
            String[] resourcePacks = resourcePackDir.list();
            for( int i=0; i<resourcePacks.length; ++i )
            {
                try
                {
                    File resourcePack = new File( resourcePackDir, resourcePacks[i] );
                    if( !resourcePack.isDirectory() )
                    {
                        // Mount a resource pack from a jar
                        IMount resourcePackMount = new JarMount( resourcePack, subPath );
                        mounts.add( resourcePackMount );
                    }
                    else
                    {
                        // Mount a resource pack from a folder
                        File subResource = new File( resourcePack, subPath );
                        if( subResource.exists() )
                        {
                            IMount resourcePackMount = new FileMount( subResource, 0 );
                            mounts.add( resourcePackMount );
                        }
                    }
                }
                catch( IOException e )
                {
                    // Ignore
                }
            }
        }

        // Return the combination of all the mounts found
        if( mounts.size() >= 2 )
        {
            IMount[] mountArray = new IMount[ mounts.size() ];
            mounts.toArray( mountArray );
            return new ComboMount( mountArray );
        }
        else if( mounts.size() == 1 )
        {
            return mounts.get( 0 );
        }
        else
        {
            return null;
        }
	}

	private static File getContainingJar( Class modClass )
	{
		String path = modClass.getProtectionDomain().getCodeSource().getLocation().getPath();
		int bangIndex = path.indexOf( "!" );
		if( bangIndex >= 0 )
		{
			path = path.substring( 0, bangIndex );
		}

		URL url;
		try {
			url = new URL( path );
		} catch (MalformedURLException e1) {
			return null;
		}

		File file;
		try {
			file = new File( url.toURI() );
		} catch(URISyntaxException e) {
			file = new File( url.getPath() );
		}
		return file;
	}

    private static File getDebugCodeDir( Class modClass )
    {
        String path = modClass.getProtectionDomain().getCodeSource().getLocation().getPath();
        int bangIndex = path.indexOf("!");
        if( bangIndex >= 0 )
        {
            return null;
        }
        return new File( new File( path ).getParentFile(), "../.." );
    }
}
