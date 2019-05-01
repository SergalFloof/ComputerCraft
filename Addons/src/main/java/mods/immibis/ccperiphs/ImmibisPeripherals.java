package mods.immibis.ccperiphs;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import mods.immibis.ccperiphs.coproc.TileCoprocAdvMap;
import mods.immibis.ccperiphs.coproc.TileCoprocCrypto;
import mods.immibis.ccperiphs.lan.BlockLANWire;
import mods.immibis.ccperiphs.lan.ItemLANWire;
import mods.immibis.ccperiphs.lan.TileNIC;
import mods.immibis.ccperiphs.rfid.DyeCardRecipe;
import mods.immibis.ccperiphs.rfid.ItemCardBase;
import mods.immibis.ccperiphs.rfid.TileMagStripe;
import mods.immibis.ccperiphs.rfid.TileRFIDReader;
import mods.immibis.ccperiphs.rfid.TileRFIDWriter;
import mods.immibis.ccperiphs.tape.ItemTape;
import mods.immibis.core.Config;
import mods.immibis.core.api.APILocator;
import mods.immibis.core.api.FMLModInfo;
import mods.immibis.core.api.net.IPacket;
import mods.immibis.core.api.net.IPacketMap;
import mods.immibis.core.api.porting.PortableBaseMod;
import mods.immibis.core.api.porting.SidedProxy;
import mods.immibis.core.api.util.Colour;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import dan200.computercraft.api.ComputerCraftAPI;

@Mod(version = "59.0.3", name = "Immibis's Peripherals", modid = "ImmibisPeripherals", dependencies = "required-after:ImmibisCore;required-after:ComputerCraft@[1.6,]")
@FMLModInfo(
		url="http://www.minecraftforum.net/topic/1001131-110-immibiss-mods-smp/",
		description="Adds some useful and neat peripherals",
		credits="Code by immibis; card textures and ideas by ozbar11.",
		authors="immibis"
		)
public class ImmibisPeripherals extends PortableBaseMod implements IPacketMap {

	public static BlockPeriphs block;
	public static BlockLANWire lanWire;
	public static ItemCardBase itemRFID, itemMagStripe, itemSmartCard;
	public static ItemTape itemTape;
	public static ItemComponent itemComponent;
	public static ImmibisPeripherals instance;
	public static File scBaseDir;
	
	public static boolean enableCraftingAcceleratorComponents;

	public static final int GUI_RFID_WRITER = 0;

	public static boolean enableLANRegistration = true;

	public static int maxTapeSizeKB = 65536;

	public static final int PKT_SPEAKER_STOP = 1;
	public static final int PKT_SPEAKER_START = 2;
	public static final int PKT_SPEAKER_STREAM = 3;

	public static String adminPassword;
	public static boolean enableSendFrom;
	public static boolean allowAdventureMapInterface;

	public ImmibisPeripherals() {
		instance = this;
	}

	public static final String CHANNEL = "ImmPhs"; // fairly short, since speakers can send lots of packets. TODO: use packet 131

	@EventHandler
	public void preinit(FMLPreInitializationEvent evt) {
		block = new BlockPeriphs();
		GameRegistry.registerBlock(block, ItemPeriphs.class, "peripherals");
		
		enableLANRegistration = Config.getBoolean("peripherals.enableLAN", true);
		enableSendFrom = Config.getBoolean("peripherals.enableLANSenderSpoofing", true);

		if(enableLANRegistration) {
			lanWire = new BlockLANWire();
			GameRegistry.registerBlock(lanWire, ItemLANWire.class, "lanwire");
		}

		itemRFID = new ItemCardBase("rf");
		itemMagStripe = new ItemCardBase("mc");
		itemComponent = new ItemComponent();
		GameRegistry.registerItem(itemRFID, "rfidcard");
		GameRegistry.registerItem(itemMagStripe, "magncard");
		GameRegistry.registerItem(itemComponent, "component");
		
		TileRFIDReader.TICKS_PER_SCAN = Config.getInt("peripherals.rfidTicksPerScan", 10);

		CraftingManager.getInstance().getRecipeList().add(new DyeCardRecipe(itemRFID));
		CraftingManager.getInstance().getRecipeList().add(new DyeCardRecipe(itemMagStripe));
		RecipeSorter.register(DyeCardRecipe.class.getName(), DyeCardRecipe.class, RecipeSorter.Category.SHAPELESS, "");

		if(Config.getBoolean("peripherals.enableCraftingRFIDCards", true)) {
			GameRegistry.addRecipe(new ItemStack(itemRFID, 1, 0),
					"RRR",
					"PPP",
					"RRR",
					'P', Items.paper,
					'R', Items.redstone
					);
		}
		if(Config.getBoolean("peripherals.enableCraftingMagCards", true)) {
			GameRegistry.addRecipe(new ItemStack(itemMagStripe, 1, 0),
					"PPP",
					"IRI",
					'P', Items.paper,
					'R', Items.redstone,
					'I', Items.iron_ingot
					);
		}
		if(Config.getBoolean("peripherals.enableCraftingRFIDReader", true)) {
			GameRegistry.addRecipe(new ItemStack(block, 1, EnumPeriphs.RFID_READER.ordinal()),
					" T ",
					"SIS",
					"SRS",
					'S', Blocks.stone,
					'T', Blocks.redstone_torch,
					'R', Items.redstone,
					'I', Items.iron_ingot
					);
		}
		if(Config.getBoolean("peripherals.enableCraftingRFIDWriter", true)) {
			GameRegistry.addRecipe(new ItemStack(block, 1, EnumPeriphs.RFID_WRITER.ordinal()),
					"L-L",
					"# #",
					"SRS",
					'S', Blocks.stone,
					'R', Items.repeater,
					'#', Blocks.gold_block,
					'-', Blocks.glass_pane,
					'L', Blocks.redstone_lamp
					);
		}
		if(Config.getBoolean("peripherals.enableCraftingMagCardDevice", true)) {
			GameRegistry.addRecipe(new ItemStack(block, 1, EnumPeriphs.MAG_STRIPE.ordinal()),
					"STS",
					"SRS",
					"SSS",
					'S', Blocks.stone,
					'R', Items.redstone,
					'T', Blocks.redstone_torch
					);
		}
		if(Config.getBoolean("peripherals.enableCraftingSpeaker", true)) {
			GameRegistry.addRecipe(new ItemStack(block, 1, EnumPeriphs.SPEAKER.ordinal()),
					"S#S",
					"SNS",
					"SNS",
					'S', Blocks.stone,
					'N', Blocks.noteblock,
					'#', Blocks.iron_block
					);
		}
		if(enableLANRegistration && Config.getBoolean("peripherals.enableCraftingLANModem", true)) {
			GameRegistry.addRecipe(new ItemStack(block, 1, EnumPeriphs.NIC.ordinal()),
					"SWS",
					"TWT",
					"SSS",
					'S', Blocks.stone,
					'T', Blocks.redstone_torch,
					'W', new ItemStack(lanWire, 1, 0)
					);
		}
		if(enableLANRegistration && Config.getBoolean("peripherals.enableCraftingLANWire", true)) {
			GameRegistry.addRecipe(new ItemStack(lanWire, 16, 0),
					"WWW",
					"RRR",
					"WWW",
					'W', new ItemStack(Blocks.wool, 1, Colour.BLUE.woolId()),
					'R', Items.redstone
					);
		}
				
		enableCraftingAcceleratorComponents = Config.getBoolean("peripherals.enableCraftingAcceleratorComponents", true);
		
		if(Config.getBoolean("peripherals.enableCraftingCryptoAccelerator", true)) {
			// recipe that uses 64 CPU cores
			GameRegistry.addRecipe(new CoprocRecipe(new ItemStack(block, 1, EnumPeriphs.COPROC_CRYPTO.ordinal()),
				"IDI",
				"I#I",
				"IDI",
				'I', Items.iron_ingot,
				'D', "dyeBlack",
				'#', new ItemStack(itemComponent, 1, ItemComponent.META_CPU_CORE)
			));
			FMLCommonHandler.instance().bus().register(new CraftingHandler());
			
			// fallback recipe for autocrafting
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(block, 1, EnumPeriphs.COPROC_CRYPTO.ordinal()),
				"IDI",
				"I#I",
				"IDI",
				'I', Items.iron_ingot,
				'D', "dyeBlack",
				'#', new ItemStack(itemComponent, 1, ItemComponent.META_CPU_CORE_64)
			));
			GameRegistry.addRecipe(new ItemStack(itemComponent, 1, ItemComponent.META_CPU_CORE_64),
				"###",
				"# #",
				"###",
				'#', new ItemStack(itemComponent, 1, ItemComponent.META_CPU_CORE_8)
			);
			GameRegistry.addRecipe(new ItemStack(itemComponent, 1, ItemComponent.META_CPU_CORE_8),
				"###",
				"# #",
				"###",
				'#', new ItemStack(itemComponent, 1, ItemComponent.META_CPU_CORE)
			);
		}
		
		allowAdventureMapInterface = Config.getBoolean("peripherals.enableAdventureMapInterface", !SidedProxy.instance.isDedicatedServer());

		ComputerCraftAPI.registerPeripheralProvider(new IPPeripheralProvider());
	}
	
	public static class CraftingHandler {
		@SubscribeEvent
		public void onCrafting(PlayerEvent.ItemCraftedEvent evt) {
			IInventory craftMatrix = evt.craftMatrix;
			ItemStack item = evt.crafting;
			EntityPlayer player = evt.player;
			if(item != null && item.getItem() == Item.getItemFromBlock(block) && item.getItemDamage() == EnumPeriphs.COPROC_CRYPTO.ordinal()) {
				for(int k = 0; k < craftMatrix.getSizeInventory(); k++) {
					ItemStack is = craftMatrix.getStackInSlot(k);
					if(is != null && is.getItem() == itemComponent && is.getItemDamage() == ItemComponent.META_CPU_CORE) {
						if(is.stackSize < 64 && player != null && !player.world.isRemote) {
							// just in case
							player.sendMessage(new TextComponentString("Don't cheat mkay?"));
							// explosion power 2 is 0.5x TNT
							player.world.newExplosion(player, player.posX, player.posY, player.posZ, 2.0f, true, true);
						}
						is.stackSize = 1;
					}
				}
			}
		}
	}

	@EventHandler
	public void load(FMLInitializationEvent evt) {

		BlockPeriphs.model = SidedProxy.instance.getUniqueBlockModelID("mods.immibis.ccperiphs.BlockRenderer", true);

		adminPassword = Config.getString("peripherals.adminPassword", "", Configuration.CATEGORY_GENERAL, "");

		if(adminPassword.isEmpty())
			adminPassword = null;

		//itemSmartCard = new ItemCardBase(Config.getItemID("peripherals.smartcard", 7436) - 256, 80);
		//itemTape = new ItemTape(Config.getItemID("peripherals.magtape", 7436) - 256, 128);

		//ModLoader.addName(itemSmartCard, "Smart card");
		//ModLoader.addName(itemTape, "Magnetic tape");


		//CraftingManager.getInstance().getRecipeList().add(new DyeCardRecipe(itemTape.shiftedIndex));

		SidedProxy.instance.registerTileEntity(TileRFIDWriter.class, "immibis.cc-rfid.writer", "mods.immibis.ccperiphs.rfid.RenderTileRFIDWriter");
		GameRegistry.registerTileEntity(TileRFIDReader.class, "immibis.cc-rfid.reader");
		GameRegistry.registerTileEntity(TileMagStripe.class, "immibis.cc-rfid.msreader");
		//ModLoader.registerTileEntity(TileTapeDrive.class, "immibis.cc-tapedrive");
		GameRegistry.registerTileEntity(TileNIC.class, "immibis.cc-lan.nic");
		SidedProxy.instance.registerTileEntity(TileCoprocCrypto.class, "immibis.cc.coproc.crypto", "mods.immibis.ccperiphs.coproc.RenderTileCoprocBase");
		SidedProxy.instance.registerTileEntity(TileCoprocAdvMap.class, "immibis.cc.coproc.advmap", "mods.immibis.ccperiphs.coproc.RenderTileCoprocBase");

		//ModLoader.registerTileEntity(TileSCInterface.class, "immibis.cc-sc.writer");
		//scBaseDir = new File(mod_ComputerCraft.getBaseDir(), "mod-data/immibis-peripherals/smartcard");
		//createFakeBaseDir();

		APILocator.getNetManager().listen(this);

		enableClockTicks(false);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		
		if(enableCraftingAcceleratorComponents) {
			// computer -> smelt -> processor
			Item computerItem = GameRegistry.findItem("ComputerCraft", "CC-Computer");
			if(computerItem != null)
				GameRegistry.addSmelting(computerItem, new ItemStack(itemComponent, 1, ItemComponent.META_CPU_CORE), 0.1f);
			else
				throw new RuntimeException("Couldn't find item ComputerCraft:CC-Computer");
		}
	}
	
	@EventHandler
	@SideOnly(Side.CLIENT)
	public void postInitClient(FMLPostInitializationEvent evt) {
		
		CreativeTabs cc_tab = CreativeTabs.MISC;
		for(CreativeTabs tab : CreativeTabs.CREATIVE_TAB_ARRAY)
			if(tab.getTabLabel().equals("ComputerCraft"))
				cc_tab = tab;
		
		if(cc_tab != null) {
			block.setCreativeTab(cc_tab);
			if(enableLANRegistration)
				lanWire.setCreativeTab(cc_tab);
			itemMagStripe.setCreativeTab(cc_tab);
			itemRFID.setCreativeTab(cc_tab);
			itemComponent.setCreativeTab(cc_tab);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean onTickInGame() {

		return true;
	}

	@SuppressWarnings("unused")
	private void createFakeBaseDir() {
		copyResourceToFileIfDoesntExist("/immibis/ccperiphs/smartcard/bios.lua", new File(scBaseDir, "mods/ComputerCraft/lua/bios.lua"));
	}

	private void copyResourceToFileIfDoesntExist(String respath, File file) {
		//if(file.exists())
		//	return;
		File parent = file.getParentFile();
		if(!parent.exists() && !parent.mkdirs())
			throw new RuntimeException("Failed to create "+parent);

		try {
			InputStream in = ImmibisPeripherals.class.getResourceAsStream(respath);
			if(in == null)
				throw new RuntimeException("Failed to open resource "+respath);

			try {
				FileOutputStream out = new FileOutputStream(file);
				try {
					byte[] buf = new byte[102400];
					while(true) {
						int read = in.read(buf);
						if(read < 0)
							break;
						out.write(buf, 0, read);
					}
				} finally {
					out.close();
				}
			} finally {
				in.close();
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public String getChannel() {
		return CHANNEL;
	}


	@Override
	public IPacket createC2SPacket(byte id) {
		return null;
	}
}
