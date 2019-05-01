package mods.immibis.core.api.porting;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class PortableTileEntity extends TileEntity {
	
	public void onDataPacket(SPacketUpdateTileEntity packet) {}
	
	@Override public final void onDataPacket(NetworkManager net, SPacketUpdateTileEntity p) {
		onDataPacket(p);
	}
}
