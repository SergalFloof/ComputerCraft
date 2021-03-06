package mods.immibis.ccperiphs.rfid;

import mods.immibis.ccperiphs.RenderUtils;
import mods.immibis.core.RenderUtilsIC;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderTileRFIDWriter extends TileEntitySpecialRenderer {

	@Override
	public void render(TileEntity teRaw, double x, double y, double z, float partialTicks, int destroyStage, float partialTick) {
		RenderUtilsIC.setBrightness(teRaw.getWorld(), teRaw.getPos().getX(), teRaw.getPos().getY(), teRaw.getPos().getZ());
		
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		
		TileRFIDWriter te = (TileRFIDWriter)teRaw;
		
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		
		float interpLid = te.lidClosedAmt + TileRFIDWriter.LID_RATE * partialTick * (te.state == TileRFIDWriter.STATE_RUN ? 1 : -1);
		interpLid = Math.min(Math.max(interpLid, 0), 1);
		
		RenderUtils.renderRFIDWriterDynamic(te.facing, interpLid, te.visualProgress, te.state != TileRFIDWriter.STATE_EMPTY, te.state == TileRFIDWriter.STATE_RUN, te.heldCardColour);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		GL11.glPopMatrix();
	}

}