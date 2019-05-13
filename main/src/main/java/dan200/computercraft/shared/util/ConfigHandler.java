package dan200.computercraft.shared.util;

import java.io.File;

import dan200.computercraft.ComputerCraft;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ConfigHandler {
	
	public static Configuration config;
	//Genrle
	public static boolean http_enable = true;
	public static String http_whitelist = "*";
	public static boolean disable_lua51_features = false;
	public static String default_computer_settings = "";
	public static boolean enableCommandBlock = false;
	public static int treasureDiskLootFrequency = 1;
	
	// GUI IDs
	public static int diskDriveGUIID = 100;
	public static int computerGUIID = 101;
	public static int printerGUIID = 102;
	
	// ComputerCraftEdu uses ID 104
	public static int printoutGUIID = 105;
	public static int pocketComputerGUIID = 106;

	//Modem Ranges
	public static int modem_range = 64;
	public static int modem_highAltitudeRange = 384;
	public static int modem_rangeDuringStorm = 64;
	public static int modem_highAltitudeRangeDuringStorm = 384;
	//Space Limit
	public static int computerSpaceLimit = 1000000;
	public static int floppySpaceLimit = 125000;
	
	public static void init(File file) {
		
		config = new Configuration(file);
		
		String category;
		
		category = "IDs";
		config.addCustomCategoryComment(category, "Set IDs for each entity and gui");
		
		// Setup general
		category = "General";
		config.addCustomCategoryComment(category, "Settings for the HTTP, Lua51 Computer settings and Enabling the Command Computer");
		http_enable = config.getBoolean("http_enable", category, true, "Enable the \\\"http\\\" API on Computers (see \\\"http_whitelist\\\" for more fine grained control than this)");
		http_whitelist = config.getString("http_whitelist", category, "*", "A semicolon limited list of wildcards for domains that can be accessed through the \"http\" API on Computers. Set this to \"*\" to access to the entire internet. Example: \"*.pastebin.com;*.github.com;*.computercraft.info\" will restrict access to just those 3 domains.");
		disable_lua51_features = config.getBoolean("disable_lua51_features", category, false, "Set this to true to disable Lua 5.1 functions that will be removed in a future update. Useful for ensuring forward compatibility of your programs now.");
		default_computer_settings = config.getString("default_computer_settings", category, "", "A comma seperated list of default system settings to set on new computers. Example: \"shell.autocomplete=false,lua.autocomplete=false,edit.autocomplete=false\" will disable all autocompletion");
		enableCommandBlock = config.getBoolean("enableCommandBlock", category, false, "Enable Command Block peripheral support");
		treasureDiskLootFrequency = config.getInt("treasureDiskLootFrequency", category, 1, 0, 100, "The frequency that treasure disks will be found in dungeon chests, from 0 to 100. Increase this value if running a modpack with lots of mods that add dungeon loot, or you just want more treasure disks. Set to 0 to disable treasure disks.");
		
		category = "Modem Rang";
		config.addCustomCategoryComment(category, "Settings for the Difrent Ranges of the Modem");
		modem_range = config.getInt("modem_range", category, 64, 64, 100000, "The range of Wireless Modems at low altitude in clear weather, in meters");
		modem_highAltitudeRange = config.getInt("modem_highAltitudeRange", category, 384, 384, 100000, "The range of Wireless Modems at maximum altitude in clear weather, in meters");
		modem_rangeDuringStorm = config.getInt("modem_rangeDuringStorm", category, 64, 64, 100000, "The range of Wireless Modems at low altitude in stormy weather, in meters");
		modem_highAltitudeRangeDuringStorm = config.getInt("modem_highAltitudeRangeDuringStorm", category, 384, 384, 100000, "The range of Wireless Modems at maximum altitude in stormy weather, in meters");
		
		category = "Space Size";
		computerSpaceLimit = config.getInt("computerSpaceLimit", category, 1000000, 1000, 1999999999, "The disk space limit for computers in bytes");
		floppySpaceLimit = config.getInt("floppySpaceLimit", category, 125000, 1000, 1999999999, "The disk space limit for floppy disks, in bytes");

		config.save();
	}
	
	public static void registerConfig(FMLPreInitializationEvent event) {
		
		ComputerCraft.config = new File(event.getModConfigurationDirectory() + "/computercraft");
		ComputerCraft.config.mkdir();
		init(new File(ComputerCraft.config.getPath(), "computercraft.cfg"));
	}

}
