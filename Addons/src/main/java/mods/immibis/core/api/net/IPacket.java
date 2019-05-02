package mods.immibis.core.api.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;

public abstract interface IPacket
{
  public abstract byte getID();
  
  public abstract String getChannel();
  
  public abstract void read(DataInputStream paramDataInputStream)
    throws IOException;
  
  public abstract void write(DataOutputStream paramDataOutputStream)
    throws IOException;
  
  public abstract void onReceived(EntityPlayer paramEntityPlayer);
  
  @Deprecated
  public static abstract interface Asynchronous
    extends IPacket
  {}
}


/* Location:              C:\Games\Minecraft Moding\Dragon World\immibis-core-59.1.4.jar!\mods\immibis\core\api\net\IPacket.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */