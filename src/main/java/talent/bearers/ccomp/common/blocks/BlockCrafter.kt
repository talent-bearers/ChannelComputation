package talent.bearers.ccomp.common.blocks

import com.teamwizardry.librarianlib.client.util.TooltipHelper
import com.teamwizardry.librarianlib.common.base.block.TileMod
import com.teamwizardry.librarianlib.common.util.ItemNBTHelper
import com.teamwizardry.librarianlib.common.util.autoregister.TileRegister
import com.teamwizardry.librarianlib.common.util.saving.SaveMethodGetter
import com.teamwizardry.librarianlib.common.util.saving.SaveMethodSetter
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory.height
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory.width
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.inventory.InventoryHelper.spawnItemStack
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.CraftingManager
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraftforge.items.*
import net.minecraftforge.items.wrapper.RangedWrapper
import talent.bearers.ccomp.MODID
import talent.bearers.ccomp.api.misc.IPulsarUsable
import talent.bearers.ccomp.api.packet.IPacket
import talent.bearers.ccomp.api.pathing.IDataNode
import talent.bearers.ccomp.common.blocks.base.BlockBaseNode
import talent.bearers.ccomp.common.core.ContainerBlockCC
import talent.bearers.ccomp.common.core.EMPTY
import talent.bearers.ccomp.common.core.isEmpty
import talent.bearers.ccomp.common.items.ModItems
import talent.bearers.ccomp.common.packets.ItemPacket

/**
 * @author WireSegal
 * Created at 7:45 PM on 3/7/17.
 */
class BlockCrafter : ContainerBlockCC("crafter", Material.IRON), IDataNode, IPulsarUsable {
    //todo allow placing of placeholders on the block itself, and keeping them when broken

    override fun createTileEntity(world: World, state: IBlockState) = TileCrafter()

    override fun createBlockState() = BlockStateContainer(this, BlockBaseNode.FACING)
    override fun connectionPoint(pos: BlockPos, world: IBlockAccess): EnumFacing = world.getBlockState(pos).getValue(BlockBaseNode.FACING)

    override fun getMetaFromState(state: IBlockState) = state.getValue(BlockBaseNode.FACING).index
    override fun getStateFromMeta(meta: Int) = defaultState.withProperty(BlockBaseNode.FACING, EnumFacing.getFront(meta))

    @SideOnly(Side.CLIENT)
    override fun performHUDOverlay(playerIn: EntityPlayer, worldIn: World, pos: BlockPos, ray: RayTraceResult, resolution: Any): String? {
        val tile = worldIn.getTileEntity(pos)
        if (tile is TileCrafter) {
            val width = 52
            val height = 52
            val xc = (resolution as ScaledResolution).scaledWidth / 2 + 20
            val yc = resolution.scaledHeight / 2 - height / 2

            Gui.drawRect(xc - 6, yc - 6, xc + width + 6, yc + height + 6, 0x44000000)
            Gui.drawRect(xc - 4, yc - 4, xc + width + 4, yc + height + 4, 0x44000000)

            for (i in 0 until 3) for (j in 0 until 3) {
                val index = i * 3 + j
                val xp = xc + j * 18
                val yp = yc + i * 18

                val item = tile.handler.getStackInSlot(index)

                val enabled = item?.item != ModItems.PLACEHOLDER

                Gui.drawRect(xp, yp, xp + 16, yp + 16, if (enabled) 0x44FFFFFF else 0x44FF0000)

                if (enabled) {
                    RenderHelper.enableGUIStandardItemLighting()
                    GlStateManager.enableRescaleNormal()
                    Minecraft.getMinecraft().renderItem.renderItemAndEffectIntoGUI(item, xp, yp)
                    RenderHelper.disableStandardItemLighting()
                }
            }
            return TooltipHelper.local(if ((0..tile.output.slots).any { tile.output.getStackInSlot(it) != null })
                        "$MODID.hud.output" else "$MODID.hud.nooutput")
        }
        return null
    }

    override fun isFullCube(state: IBlockState) = false
    override fun isOpaqueCube(blockState: IBlockState) = false

    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer): IPacket? {
        val tile = (world.getTileEntity(pos) as? TileCrafter) ?: return null
        return if (packetType == "item") ItemPacket.fromItemHandler(strength, tile.output, true) else null
    }

    override fun requestPullPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer): IPacket? {
        val tile = (world.getTileEntity(pos) as? TileCrafter) ?: return null
        return if (packetType == "item") ItemPacket.fromItemHandler(strength, tile.output, false, tile) else null
    }

    override fun pushPacket(packet: IPacket, pos: BlockPos, world: WorldServer) = packet

    override fun onBlockPlaced(worldIn: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase): IBlockState
            = defaultState.withProperty(BlockBaseNode.FACING, facing.opposite)

    override fun breakBlock(worldIn: World, pos: BlockPos, state: IBlockState) {
        val tile = worldIn.getTileEntity(pos)
        if (tile is TileCrafter)
            (0..tile.handler.slots - 1)
                    .map { tile.handler.getStackInSlot(it) }
                    .filterNot { it.isEmpty }
                    .forEach { spawnItemStack(worldIn, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), it) }

        super.breakBlock(worldIn, pos, state)
    }

    @TileRegister("crafter")
    class TileCrafter : TileMod() {

        val handler: ItemStackHandler = Handler(this)

        val main = RangedWrapper(handler, 0, 10)
        val output = RangedWrapper(handler, 9, 19)

        @SaveMethodGetter("items")
        private fun itemsCompound() = handler.serializeNBT()
        @SaveMethodSetter("items")
        private fun deserItems(nbt: NBTTagCompound) = handler.deserializeNBT(nbt)

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
            val internalFacing = world.getBlockState(pos).getValue(BlockBaseNode.FACING)
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                if (facing == internalFacing) return output as T
                else if (facing == internalFacing.opposite) return main as T
                else if (facing == null) return handler as T
            }
            return super.getCapability(capability, facing)
        }

        override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
            val internalFacing = world.getBlockState(pos).getValue(BlockBaseNode.FACING)
            return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY &&
                        facing == internalFacing || facing == internalFacing.opposite || facing == null)
                    || super.hasCapability(capability, facing)
        }
    }

    private class Handler(val tile: TileCrafter) : ItemStackHandler(19) {

        override fun getStackLimit(slot: Int, stack: ItemStack?): Int {
            return if (slot < 9) 1 else super.getStackLimit(slot, stack)
        }

        override fun insertItem(slot: Int, stack: ItemStack?, simulate: Boolean): ItemStack? {
            if (!openThyDoorsToMe && slot >= 9) return stack

            return super.insertItem(slot, stack, simulate)
        }

        private var no = false

        private var openThyDoorsToMe = false

        override fun onContentsChanged(slot: Int) {
            if (no) return
            no = true
            val empty = (0 until 9).any { getStackInSlot(it).isEmpty }
            if (!empty) {
                val fakeInv = FakeCraftingInventory(this)
                val recipe = CraftingManager.getInstance().recipeList
                        .filter { it.matches(fakeInv, tile.world) }.firstOrNull()
                if (recipe != null) {
                    val result = recipe.getCraftingResult(fakeInv)
                    if (result != null) {
                        for (i in 0 until 9) {
                            val item = getStackInSlot(i)
                            if (item == null || item.isEmpty) continue
                            if (item.item == ModItems.PLACEHOLDER)
                                continue
                            setStackInSlot(i, EMPTY)
                        }

                        val remaining = recipe.getRemainingItems(fakeInv)

                        openThyDoorsToMe = true
                        remaining
                                .filterNot { it.isEmpty }
                                .map { ItemHandlerHelper.insertItem(tile.output, it, false) }
                                .filterNot { it.isEmpty }
                                .forEach { spawnAsEntity(tile.world, tile.pos, it) }

                        val tossOut: ItemStack? = ItemHandlerHelper.insertItem(tile.output, result, false)
                        if (!tossOut.isEmpty) spawnAsEntity(tile.world, tile.pos, tossOut)
                        openThyDoorsToMe = false
                    }
                }
            }
            tile.markDirty()
            no = false
        }
    }

    private object FakeContainer : Container() {
        override fun canInteractWith(playerIn: EntityPlayer?) = true
    }

    private class FakeCraftingInventory(handler: IItemHandler) : InventoryCrafting(FakeContainer, 3, 3) {
        init {
            for (i in 0 until 9) {
                val stack = handler.getStackInSlot(i)
                if (stack.isEmpty)
                    continue
                if (stack.item != ModItems.PLACEHOLDER)
                    setInventorySlotContents(i, stack.copy())
            }
        }
    }
}
