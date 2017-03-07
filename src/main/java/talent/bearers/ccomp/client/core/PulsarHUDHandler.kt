package talent.bearers.ccomp.client.core

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11
import talent.bearers.ccomp.api.misc.IPulsarUsable
import talent.bearers.ccomp.common.items.ModItems

/**
 * @author WireSegal
 * Created at 3:16 PM on 3/7/17.
 */
@SideOnly(Side.CLIENT)
object PulsarHUDHandler {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onRenderHUDPost(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
        val player = Minecraft.getMinecraft().thePlayer
        if (player.heldItemMainhand?.item == ModItems.PULSAR || player.heldItemOffhand?.item == ModItems.PULSAR)
            renderHUD(event.resolution, player, event.partialTicks)
    }

    fun renderHUD(resolution: ScaledResolution, player: EntityPlayerSP, partTicks: Float) {
        val mc = Minecraft.getMinecraft()
        val location = player.rayTrace(mc.playerController?.blockReachDistance?.toDouble() ?: 5.0, partTicks) ?: return
        if (location.typeOfHit != RayTraceResult.Type.BLOCK) return

        val state = mc.theWorld.getBlockState(location.blockPos)
        val block = state.block as? IPulsarUsable ?: return

        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        val name = block.getHUDOverlay(player, mc.theWorld, location.blockPos, location) ?: return
        val label = mc.fontRendererObj.getStringWidth(name)
        val setRecipe = resolution.scaledWidth / 2 - label / 2
        val y = resolution.scaledHeight / 2 - 65
        val color = 0x22000000
        Gui.drawRect(setRecipe - 6, y - 6, setRecipe + label + 6, y + 15, color)
        Gui.drawRect(setRecipe - 4, y - 4, setRecipe + label + 4, y + 13, color)
        mc.fontRendererObj.drawStringWithShadow(name, setRecipe.toFloat(), y.toFloat(), 0xFFFFFF)
    }
}
