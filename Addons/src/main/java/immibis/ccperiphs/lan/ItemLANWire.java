package mods.immibis.ccperiphs.lan;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemLANWire extends ItemBlock {
	public ItemLANWire(Block id_minus_256) {
		super(id_minus_256);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		return "item.immibis_peripherals.cable."+par1ItemStack.getItemDamage();
	}
}
