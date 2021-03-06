package mods.immibis.ccperiphs.rfid;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class DyeCardRecipe implements IRecipe {
	
	public Item cardID;
	
	public DyeCardRecipe(Item cardID) {
		this.cardID = cardID;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack card = null, dye = null;
		for(int k = 0; k < inv.getSizeInventory(); k++) {
			ItemStack s = inv.getStackInSlot(k);
			if(s == null)
				continue;
			
			if(s.getItem() == cardID) {
				if(card != null)
					return null;
				card = s;
			} else if(s.getItem() == Items.DYE) {
				if(dye != null)
					return null;
				dye = s;
			}
		}
		if(card == null || dye == null)
			return null;
		
		ItemStack result = card.copy();
		result.setItemDamage(15 - dye.getItemDamage());
		return result;
	}

	@Override
	public boolean matches(InventoryCrafting var1, World world) {
		return getCraftingResult(var1) != null;
	}

//	@Override
//	public int getRecipeSize() {
//		return 2;
//	}

	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(cardID, 1, 0);
	}

	@Override
	public IRecipe setRegistryName(ResourceLocation name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceLocation getRegistryName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<IRecipe> getRegistryType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canFit(int width, int height) {
		// TODO Auto-generated method stub
		return false;
	}

}
