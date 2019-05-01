package mods.immibis.ccperiphs.tape;


import java.util.HashSet;
import java.util.Set;

import mods.immibis.ccperiphs.IIPPeripheralTile;
import mods.immibis.ccperiphs.ImmibisPeripherals;
import mods.immibis.ccperiphs.TilePeriphs;
import mods.immibis.core.api.util.Dir;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

public class TileTapeDrive extends TilePeriphs implements IPeripheral, IInventory, IIPPeripheralTile {
	public static final int MAX_LABEL_LENGTH = 20;
	public static final int BYTES_PER_TICK = 5;
	public static final int BUFFER_SIZE = 100;
	
	private volatile Set<IComputerAccess> computers = new HashSet<IComputerAccess>();
	
	public byte facing;
	
	public volatile ItemStack contents;
	
	// Visual effects
	public static final int STATE_DIR_MASK = 3;
	public static final int STATE_STILL = 0;
	public static final int STATE_FORWARD = 1;
	public static final int STATE_BACKWARD = 2;
	public static final int STATE_FLAG_ON = 4;
	public int state; // sum of direction and flags
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setByte("facing", facing);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		facing = tag.getByte("facing");
	}
	
	@Override
	public IPeripheral getPeripheral(int side) {
		return this;
	}
	
	private void updateState() {
		int newState;
		if(computers.size() == 0)
			newState = 0;
		else {
			newState = STATE_FLAG_ON;
		}
		
		if(state != newState) {
			state = newState;
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}
	
	@Override
	public Packet getDescriptionPacket() {
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, ((facing - 2) & 3) | (state << 2), null);
	}
	
	@Override
	public void onDataPacket(S35PacketUpdateTileEntity p) {
		facing = (byte)((p.func_148853_f() & 3) + 2);
		state = (p.func_148853_f() >> 2) & 63;
	}

	@Override
	public String getType() {
		return "mag card reader";
	}
	
	private static String[] methodNames = {
		"isPresent",
		"setLabel",
		"getCode",
		"setCode",
	};

	@Override
	public String[] getMethodNames() {
		return methodNames;
	}
	
	// should be called while holding the monitor to prevent race conditions
	// (where the tape is removed after isTapeInserted returns true)
	private boolean isTapeInserted() {
		return contents != null && contents.getItem() == ImmibisPeripherals.itemTape;
	}

	@Override
	public synchronized Object[] callMethod(IComputerAccess computer, ILuaContext ctx, int method, Object[] arguments) throws LuaException {
		switch(method) {
		case 0: // isPresent
			return new Object[] {isTapeInserted()};
		case 1: // setLabel
			if(arguments.length < 1 || !(arguments[0] instanceof String))
				throw new LuaException("argument must be a string");
			if(!isTapeInserted())
				throw new LuaException("no tape");
			{
				String label = (String)arguments[0];
				if(label.length() > MAX_LABEL_LENGTH)
					throw new LuaException("label too long (max "+MAX_LABEL_LENGTH+" chars)");
				if(contents.stackTagCompound == null)
					contents.stackTagCompound = new NBTTagCompound();
				contents.stackTagCompound.setString("line1", label);
				
				worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
			}
			worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
			return new Object[] {true};
		case 2: // getCode
			if(!isTapeInserted() || contents.stackTagCompound == null)
				break;
			if(!contents.stackTagCompound.hasKey("code"))
				break;
			return new Object[] {contents.stackTagCompound.getInteger("code")};
		case 3: // setCode
			if(arguments.length < 1 || !(arguments[0] instanceof Number))
				throw new LuaException("argument must be a number");
			if(!isTapeInserted())
				throw new LuaException("no tape");
			{
				int code = ((Number)arguments[0]).intValue();
				if(contents.stackTagCompound == null)
					contents.stackTagCompound = new NBTTagCompound();
				contents.stackTagCompound.setInteger("code", code);
				
				worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
			}
			return new Object[] {true};
		}
		return new Object[0];
	}

	@Override
	public boolean equals(IPeripheral other) {
		int x = 1; // compiler warning
		return other == this;
	}

	@Override
	public synchronized void attach(IComputerAccess computer) {
		computers.add(computer);
		updateState();
	}

	@Override
	public synchronized void detach(IComputerAccess computer) {
		computers.remove(computer);
		
		if(computers.size() == 0) {
			
		}

		updateState();
	}
	
	@Override
	public synchronized boolean onBlockActivated(EntityPlayer ply) {
		ItemStack h = ply.getCurrentEquippedItem();
		if(h == null) {
			if(contents == null)
				return false;
			
			ply.inventory.setInventorySlotContents(ply.inventory.currentItem, contents);
			contents = null;
		} else {
			if(contents != null)
				return false;
			
			contents = h;
			ply.inventory.setInventorySlotContents(ply.inventory.currentItem, null);
		}
		
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
		
		return true;
	}
	
	@Override
	public void onPlaced(EntityLivingBase player, int look) {
		if(look == Dir.PY || look == Dir.NY)
			facing = (byte)2;
		else
			facing = (byte)(look ^ 1);
	}

	
	
	
	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public synchronized ItemStack getStackInSlot(int var1) {
		return var1 == 0 ? contents : null;
	}

	@Override
	public synchronized ItemStack decrStackSize(int var1, int var2) {
		if(var1 != 0 || contents == null)
			return null;
		ItemStack rv;
		if(var2 >= contents.stackSize) {
			rv = contents;
			contents = null;
		} else {
			rv = contents.copy();
			rv.stackSize = var2;
			contents.stackSize -= var2;
		}
		return rv;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return null;
	}

	@Override
	public synchronized void setInventorySlotContents(int var1, ItemStack var2) {
		if(var1 == 0)
			contents = var2;
	}

	@Override
	public String getInventoryName() {
		return "Tape drive";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return false;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

}
