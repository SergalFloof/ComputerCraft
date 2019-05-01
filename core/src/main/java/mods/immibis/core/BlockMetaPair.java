package mods.immibis.core;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class BlockMetaPair {
	public ResourceLocation block;
	public int data;
	
	public BlockMetaPair(ResourceLocation resourceLocation, int k) {
		if(resourceLocation == null) throw new NullPointerException("block name is null");
		this.block = resourceLocation;
		this.data = k;
	}

	public BlockMetaPair(Block block, int k) {
		this(Block.REGISTRY.getNameForObject(block), k);
	}

	public BlockMetaPair(ItemStack stack, int k) {
		this(Item.REGISTRY.getNameForObject(stack.getItem()), k);
	}
	
	@Override
	public int hashCode()
	{
		return (data << 16) + block.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		try
		{
			BlockMetaPair bmp = (BlockMetaPair)o;
			return bmp.block.equals(this.block) && bmp.data == data;
		}
		catch(ClassCastException e)
		{
			return false;
		}
	}
	
	public static BlockMetaPair parse(String s)
	{
		String[] a = s.split(":");
		if(a.length != 2)
			throw new NumberFormatException("Not a valid block ID/data value: " + s);
		return new BlockMetaPair((Block)Block.REGISTRY.getObject(a[0]), Integer.parseInt(a[1]));
	}
}
