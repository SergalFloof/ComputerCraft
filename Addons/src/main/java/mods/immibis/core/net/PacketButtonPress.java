 package mods.immibis.core.net;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 
 public class PacketButtonPress  extends AbstractContainerSyncPacket
 {
   public int buttonID;
   
   public PacketButtonPress(int button)
   {
     this.buttonID = button;
   }
   
   public byte getID()
   {
     return 6;
   }
   
   public void read(DataInputStream in) throws IOException
   {
     this.buttonID = in.readInt();
   }
   
   public void write(DataOutputStream out) throws IOException
   {
     out.writeInt(this.buttonID);
   }
   
   public String getChannel()
   {
     return "ImmibisCore";
   }
 }