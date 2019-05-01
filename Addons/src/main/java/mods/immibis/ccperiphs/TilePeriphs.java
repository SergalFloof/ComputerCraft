package mods.immibis.ccperiphs;


import java.util.ArrayList;
import java.util.List;

import mods.immibis.core.TileCombined;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public abstract class TilePeriphs extends TileCombined {
	@Override
	public List<ItemStack> getInventoryDrops() {
		if(!(this instanceof IInventory))
			return super.getInventoryDrops();
		
		IInventory inv = (IInventory)this;
		
		ArrayList<ItemStack> rv = new ArrayList<ItemStack>(inv.getSizeInventory());
		for(int k = 0; k < inv.getSizeInventory(); k++) {
			ItemStack is = inv.getStackInSlot(k);
			if(is != null)
				rv.add(is);
		}
		return rv;
	}
	
	public int getTexture(int side) {return 0;}

	public void onPlacedOnSide(int side) {}
}
