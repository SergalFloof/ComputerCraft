package mods.immibis.ccperiphs.coproc;

import mods.immibis.ccperiphs.EnumPeriphs;
import mods.immibis.ccperiphs.RenderUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class RenderTileCoprocBase extends TileEntitySpecialRenderer {
	
	@Override
	public void renderTileEntityFast(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage,
			float partial, BufferBuilder buffer) {
		RenderUtils.renderCoprocDynamic(x, y, z, EnumPeriphs.VALUES[te.getBlockMetadata()], ((TileCoprocBase)te).facing, (TileCoprocBase)te);
	}

}
