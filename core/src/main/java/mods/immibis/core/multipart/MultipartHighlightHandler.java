package mods.immibis.core.multipart;

import mods.immibis.core.api.multipart.ICoverableTile;
import mods.immibis.core.api.multipart.IPartContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class MultipartHighlightHandler {

	@SubscribeEvent(receiveCanceled = true)
	public void onBlockHighlightEvent(DrawBlockHighlightEvent evt) {
		RayTraceResult pos = evt.getTarget();
		EntityPlayer ply = evt.getPlayer();
		
		if(pos.typeOfHit != RayTraceResult.Type.BLOCK)
			return;
		
		if(evt.getSubID() != 0) // render pass
			return;
		
		TileEntity te = ply.world.getTileEntity(pos.getBlockPos());
		AxisAlignedBB hitBB = null;
		IPartContainer pc = null;
		if(SubhitValues.isCoverSystem(pos.subHit)) {
			if(te instanceof ICoverableTile)
				pc = ((ICoverableTile)te).getCoverSystem();
		} else {
			if(te instanceof IPartContainer)
				pc = (IPartContainer)te;
		}
		
		if(pc != null)
			hitBB = pc.getPartAABBFromPool(SubhitValues.getPartIndex(pos.subHit));
		
		
		if(hitBB != null)
		{
			drawSelectionBox(ply, pos, 0, evt.currentItem, evt.partialTicks, hitBB.offset(pos.blockX, pos.blockY, pos.blockZ));
			evt.setCanceled(true);
		}
	}
	
	private void drawOutlinedBoundingBox(AxisAlignedBB par1AxisAlignedBB)
    {
        Tessellator var2 = Tessellator.getInstance();
        var2.startDrawing(3);
        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
        var2.draw();
        var2.startDrawing(3);
        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
        var2.draw();
        var2.startDrawing(1);
        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
        var2.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
        var2.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
        var2.draw();
    }
	
	public void drawSelectionBox(EntityPlayer par1EntityPlayer, RayTraceResult par2MovingObjectPosition, int par3, ItemStack par4ItemStack, float par5, AxisAlignedBB box)
    {
        if (par3 == 0 && par2MovingObjectPosition.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
            GL11.glLineWidth(2.0F);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDepthMask(false);
            float var6 = 0.002F;
            
            double var8 = par1EntityPlayer.lastTickPosX + (par1EntityPlayer.posX - par1EntityPlayer.lastTickPosX) * (double)par5;
            double var10 = par1EntityPlayer.lastTickPosY + (par1EntityPlayer.posY - par1EntityPlayer.lastTickPosY) * (double)par5;
            double var12 = par1EntityPlayer.lastTickPosZ + (par1EntityPlayer.posZ - par1EntityPlayer.lastTickPosZ) * (double)par5;
            drawOutlinedBoundingBox(box.expand((double)var6, (double)var6, (double)var6).getOffsetBoundingBox(-var8, -var10, -var12));

            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }
}
