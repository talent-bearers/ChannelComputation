package talent.bearers.ccomp.client.core

import com.teamwizardry.librarianlib.common.util.ItemNBTHelper
import com.teamwizardry.librarianlib.common.util.isEmpty
import com.teamwizardry.librarianlib.common.util.vec
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.block.model.ItemCameraTransforms
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import talent.bearers.ccomp.common.blocks.BlockCrafter.TileCrafter
import talent.bearers.ccomp.common.blocks.ModBlocks
import talent.bearers.ccomp.common.blocks.base.BlockBaseNode
import talent.bearers.ccomp.common.items.ModItems


/**
 * @author WireSegal
 * Created at 3:58 PM on 3/7/17.
 */
object BumpRenderer : TileEntitySpecialRenderer<TileCrafter>() {

    val slotPositions = arrayOf(
            vec(4 / 16.0, 4 / 16.0),
            vec(7 / 16.0, 4 / 16.0),
            vec(10 / 16.0, 4 / 16.0),
            vec(4 / 16.0, 7 / 16.0),
            vec(7 / 16.0, 7 / 16.0),
            vec(10 / 16.0, 7 / 16.0),
            vec(4 / 16.0, 10 / 16.0),
            vec(7 / 16.0, 10 / 16.0),
            vec(10 / 16.0, 10 / 16.0)
    )

     val renderStack = ItemStack(ModItems.PLACEHOLDER).apply { ItemNBTHelper.setBoolean(this, "bump", true) }

    override fun renderTileEntityAt(te: TileCrafter, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, z)

        val state = te.world.getBlockState(te.pos)
        if (state.block != ModBlocks.CRAFTER) return
        val facing = state.getValue(BlockBaseNode.FACING)
        when (facing) {
            EnumFacing.UP -> {
                GlStateManager.rotate(180f, 0f, 0f, 1f)
                GlStateManager.translate(-1f, -1f, 0f)
            }
            EnumFacing.NORTH -> {
                GlStateManager.rotate(90f, 1f, 0f, 0f)
                GlStateManager.translate(0f, 0f, -1f)
            }
            EnumFacing.SOUTH -> {
                GlStateManager.rotate(90f, 1f, 0f, 0f)
                GlStateManager.rotate(180f, 0f, 0f, 1f)
                GlStateManager.translate(-1f, -1f, -1f)
            }
            EnumFacing.WEST -> {
                GlStateManager.rotate(90f, 1f, 0f, 0f)
                GlStateManager.rotate(-90f, 0f, 0f, 1f)
                GlStateManager.translate(-1f, 0f, -1f)
            }
            EnumFacing.EAST -> {
                GlStateManager.rotate(90f, 1f, 0f, 0f)
                GlStateManager.rotate(90f, 0f, 0f, 1f)
                GlStateManager.translate(0f, -1f, -1f)
            }
            else -> { /* NO-OP */ }
        }
        GlStateManager.translate(1 / 16.0, 1.45, 1 / 16.0)

        val inventory = te.main
        for (i in 0 until 9) {
            val stack = inventory.getStackInSlot(i)
            if (stack == null || stack.isEmpty || stack.item != ModItems.PLACEHOLDER)
                continue
            GlStateManager.pushMatrix()
            val pos = slotPositions[i]
            GlStateManager.translate(pos.x, 0.0, pos.y)
            Minecraft.getMinecraft().renderItem.renderItem(renderStack, ItemCameraTransforms.TransformType.NONE)
            GlStateManager.popMatrix()
        }
        GlStateManager.popMatrix()
    }
}
