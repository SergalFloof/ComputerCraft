package mods.immibis.ccperiphs.tape;


import mods.immibis.ccperiphs.rfid.ItemCardBase;

public class ItemTape extends ItemCardBase {
	public ItemTape(String itemName) {
		super(itemName);
	}
	
	/*@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addInformation(ItemStack stack, List list) {
		if(stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("data"))
			return;
		
		String line1 = stack.stackTagCompound.getString("line1");
		
		if(!line1.equals("")) list.add(line1);
	}*/
}
