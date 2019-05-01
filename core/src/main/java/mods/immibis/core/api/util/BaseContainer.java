package mods.immibis.core.api.util;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mods.immibis.core.SlotFake;
import mods.immibis.core.api.APILocator;
import mods.immibis.core.api.net.IPacket;
import mods.immibis.core.net.ISyncedContainer;
import mods.immibis.core.net.PacketButtonPress;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class BaseContainer<InventoryType> extends Container implements ISyncedContainer {
	
	protected EntityPlayer player;
	protected InventoryType inv;
	
	public BaseContainer(EntityPlayer player, InventoryType inv) {
		this.player = player;
		this.inv = inv;
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		if(inv instanceof TileEntity)
			if(((TileEntity)inv).isInvalid())
				return false;
		if(inv instanceof IInventory)
			return ((IInventory)inv).isUsableByPlayer(var1);
		if(inv instanceof TileEntity) {
			TileEntity te = (TileEntity)inv;
			return var1.getDistanceSq(te.x + 0.5, te.y + 0.5, te.z + 0.5) <= 64;
		}
		return true;
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer pl, int slot) {
		return transferStackInSlot(slot);
	}
	public ItemStack transferStackInSlot(int slot) {
		return null;
	}
	
	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		if(slotId >= 0 && slotId < inventorySlots.size() &&  inventorySlots.get(slotId) instanceof SlotFake) {
        	SlotFake fs = (SlotFake)inventorySlots.get(slotId);
        	ItemStack heldStack = player.inventory.getItemStack();
        	fs.onClickByItem(heldStack, dragType, clickTypeIn == 1);
        	return null;
        }
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}
	
	// Returns true if this slot can be taken from when the user double-clicks another slot with a matching item.
	// In vanilla, this is false for crafting output slots.
	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
		if(slotIn instanceof SlotFake)
			return false;
		return super.canMergeSlot(stack, slotIn);
	}

	/** Sends the packet to all players listening to this inventory.
	 * Use this to update the info displayed in the GUI, for example.
	 * They may be sent from the server to the client.
	 * 
	 * Calling this on the client has no effect in SSP, and throws
	 * an exception in SMP. It does NOT send the packet from the
	 * client to itself, on the rationale that the container should
	 * already know the information being sent.
	 */
	@SuppressWarnings("unchecked")
	public void sendUpdatePacket(IPacket packet) {
		if(player.world.isRemote)
			throw new IllegalStateException("Cannot send update packets from the client");

		//System.out.println("send update packet "+packet);
//		for(ICrafting ic : (List<ICrafting>)crafters)
//			if(ic instanceof EntityPlayer)
//				APILocator.getNetManager().sendToClient(packet, (EntityPlayer)ic);
	}
	
	/** Sends the packet to the server. Throws an exception if run on the server.
	 */
	public void sendActionPacket(IPacket packet) {

		if(!player.world.isRemote)
			throw new IllegalStateException("Cannot send action packets from the server");

		APILocator.getNetManager().sendToServer(packet);
	}
	
	/** Called when an action packet is received. */
	public void onActionPacket(IPacket p) {}
	/** Called when an update packet is received. */
	public void onUpdatePacket(IPacket p) {}
	
	/** Called when a button-press packet is received.
	 * It's like an action packet that carries a single int,
	 * for convenience (you don't have to make a packet
	 * class to wrap just one int)
	 */
	public void onButtonPressed(int id) {}
	
	/** Sends a button-press packet. */
	public void sendButtonPressed(int id) {
		sendActionPacket(new PacketButtonPress(id));
	}
	
	private void onActionPacket2(IPacket p) {
		if(p instanceof PacketButtonPress) {
			onButtonPressed(((PacketButtonPress)p).buttonID);
		} else
			onActionPacket(p);
	}
	
	@Override
	public final void onReceivePacket(IPacket p) {
		if(!player.world.isRemote)
			onActionPacket2(p);
		else
			onUpdatePacket(p);
	}
	
//	public void sendProgressBarUpdate(int index, int value) {
//		for(Object o : crafters)
//			((ICrafting)o).sendProgressBarUpdate(this, index, value);
//	}
	
	private Map<Short, Short> prevBarValues = new HashMap<Short, Short>();
	protected void setProgressBar(int _index, int _value) {
		short index = (short)_index, value = (short)_value;
		Short prev = prevBarValues.get(index);
		if(prev == null || prev != value) {
			prevBarValues.put(index, value);
//			sendProgressBarUpdate(index, value);
		}
	}
}
