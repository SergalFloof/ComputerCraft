 package mods.immibis.core.api.net;
 
 import java.io.IOException;
 import net.minecraft.network.INetHandler;
 import net.minecraft.network.NetHandlerPlayServer;
 import net.minecraft.network.Packet;
 import net.minecraft.network.PacketBuffer;
 
 
 
 public class IPacketWrapper implements Packet
 {
   public IPacket packet;
   
   public IPacketWrapper(IPacket packet)
   {
     this.packet = packet;
   }
   
   public void func_148833_a(INetHandler var1)
   {
     if ((var1 instanceof NetHandlerPlayServer)) {
       this.packet.onReceived(((NetHandlerPlayServer)var1).player);
     } else {
       this.packet.onReceived(null);
     }
   }
   
   public void readPacketData(PacketBuffer var1) throws IOException {
     throw new IOException("not serializable");
   }
   
   public void writePacketData(PacketBuffer var1) throws IOException
   {
     throw new IOException("not serializable");
   }

   @Override
   public void processPacket(INetHandler handler) {
	   // TODO Auto-generated method stub
	
   }
 }