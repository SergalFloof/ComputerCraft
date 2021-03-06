package mods.immibis.core.api.porting;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public abstract class PortableBaseMod {
	public class TickEventServerHandler {
		@SubscribeEvent
		public void tickEventServer(TickEvent.ServerTickEvent event) {
			if(event.phase != Phase.START) return;
			onTickInGame();
		}
	}
	
	public class TickEventClientHandler {
		@SubscribeEvent
		public void tickEventClient(TickEvent.ClientTickEvent event) {
			if(event.phase != Phase.START) return;
			onTickInGame();
		}
	}
	
    public boolean onTickInGame() {return false;}

    public void enableClockTicks(final boolean server) {
    	FMLCommonHandler.instance().bus().register(server
    			? new TickEventServerHandler()
    			: new TickEventClientHandler());
    }
}
