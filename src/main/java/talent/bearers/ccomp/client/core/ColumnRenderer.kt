package talent.bearers.ccomp.client.core

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import talent.bearers.ccomp.common.blocks.BlockFluidColumn.TileFluidColumn


/**
 * @author WireSegal
 * Created at 3:58 PM on 3/7/17.
 */
object ColumnRenderer : TileEntitySpecialRenderer<TileFluidColumn>() {

    override fun renderTileEntityAt(te: TileFluidColumn, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int) {
        val fluid = te.tank.fluid
        if (fluid != null) {
            GlStateManager.pushMatrix()
            GlStateManager.translate(x + 0.16, y + 0.02, z + 0.16)
            GlStateManager.disableLighting()
            GlStateManager.enableBlend()
            val x1 = 0.0
            val y1 = 0.0
            val z1 = 0.0
            val x2 = 0.7
            val y2 = te.tank.fluidAmount.toDouble() / te.tank.capacity.toDouble() - 0.08
            val z2 = 0.7
            ClientUtil.renderFluidCuboid(fluid.copy(), te.pos, x1, y1, z1, x2, y2, z2)
            GlStateManager.enableLighting()
            GlStateManager.disableBlend()
            GlStateManager.popMatrix()
        }
    }
}
