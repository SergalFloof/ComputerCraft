package mods.immibis.core.api.util;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class BaseGuiContainer<ContainerType extends Container> extends GuiContainer {
	public ContainerType container;
	private ResourceLocation texPath;
	public BaseGuiContainer(ContainerType container, int xSize, int ySize, ResourceLocation texPath) {
		super(container);
		this.container = container;
		this.xSize = xSize;
		this.ySize = ySize;
		this.texPath = texPath;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.renderEngine.bindTexture(texPath);
		GL11.glColor3f(1, 1, 1);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
	
	protected void drawString(String s, int x, int y, int colour) {
		fontRenderer.drawStringWithShadow(s, x + guiLeft, y + guiTop, colour);
	}
	
	protected void drawStringWithoutShadow(String s, int x, int y, int colour) {
		fontRenderer.drawString(s, x + guiLeft, y + guiTop, colour);
	}
}
