package mods.immibis.core.impl.crossmod;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.item.ElectricItem;
import ic2.api.item.IC2Items;
import ic2.api.item.IElectricItem;
import ic2.api.reactor.IReactorChamber;
import ic2.api.tile.ExplosionWhitelist;
import java.util.List;

import mods.immibis.core.api.crossmod.ICrossModIC2;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;

public class CrossModIC2_Impl implements ICrossModIC2 {
	@Override
	public Item getWrenchItem() {
		return IC2Items.getItem("wrench").getItem();
	}
	
	private static ItemStack[] zeroStackSizes(ItemStack[] ar) {
		ItemStack[] rv = new ItemStack[ar.length];
		for(int k = 0; k < ar.length; k++) {
			if(ar[k] == null)
				rv[k] = null;
			else {
				ItemStack is = ar[k].copy();
				is.stackSize = 0;
				rv[k] = is;
			}
		}
		return rv;
	}

	@Override
	public void addEnetTile(Object tile) throws ClassCastException {
		if(((TileEntity)tile).getWorld().isRemote)
			return;
		MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent((IEnergyTile)tile));
	}

	@Override
	public void removeEnetTile(Object tile) throws ClassCastException {
		if(((TileEntity)tile).getWorld().isRemote)
			return;
		MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile)tile));
	}

	@Override
	public boolean isElectricItem(ItemStack item) {
		return item.getItem() instanceof IElectricItem;
	}

	@Override
	public int dischargeElectricItem(ItemStack stack, int amount, int tier, boolean ignoreRateLimit, boolean simulate) {
		return (int)(ElectricItem.manager.discharge(stack, amount, tier, ignoreRateLimit, true, simulate) + Math.random());
	}

	@Override
	public boolean isReactorChamber(IBlockAccess world, int i, int j, int k) {
		return world.getTileEntity(i, j, k) instanceof IReactorChamber;
	}

	@Override
	public void addExplosionWhitelist(Block block) {
		ExplosionWhitelist.addWhitelistedBlock(block);
	}

	@Override
	public ItemStack getItem(String name) {
		return IC2Items.getItem(name);
	}
}
