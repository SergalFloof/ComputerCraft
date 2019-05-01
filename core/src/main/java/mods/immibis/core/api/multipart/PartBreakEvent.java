package mods.immibis.core.api.multipart;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired on the Forge event bus when a part finishes being broken.
 * 
 * This is fired on both the client and server.
 */
@Cancelable
public class PartBreakEvent extends Event {
	public final World world;
	public final PartCoordinates coords;
	public final EntityLivingBase player;
	
	public PartBreakEvent(World world, PartCoordinates coords, EntityLivingBase player) {
		this.world = world;
		this.coords = coords;
		this.player = player;
	}
}
