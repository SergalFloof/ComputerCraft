/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.media.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.media.IMedia;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

// An implementation of IMedia for ItemRecord's
public class RecordMedia implements IMedia
{
	
	public World world;
	public RecordMedia()
	{
	}
	 
	@Override
	public String getLabel( ItemStack stack )
	{
		return getAudioTitle( stack );
	}
	
	@Override
	public boolean setLabel( ItemStack stack, String label )
	{
		return false;
	}
	
	@Override
	public String getAudioTitle( ItemStack stack )
	{
		return ComputerCraft.getRecordInfo( stack, world );
	}
	
	@Override
	public String getAudioRecordName( ItemStack stack )
	{
		ItemRecord itemRecord = (ItemRecord)stack.getItem();
		return "records." + itemRecord.getRecordNameLocal();
	}
    
	@Override
    public IMount createDataMount( ItemStack stack, World world )
    {
    	return null;
    }
}
