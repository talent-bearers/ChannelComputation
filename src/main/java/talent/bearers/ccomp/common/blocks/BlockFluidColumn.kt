package talent.bearers.ccomp.common.blocks

import com.teamwizardry.librarianlib.client.core.ModelHandler
import com.teamwizardry.librarianlib.client.util.TooltipHelper
import com.teamwizardry.librarianlib.common.base.block.BlockModContainer
import com.teamwizardry.librarianlib.common.base.block.ItemModBlock
import com.teamwizardry.librarianlib.common.base.block.TileMod
import com.teamwizardry.librarianlib.common.base.item.ISpecialModelProvider
import com.teamwizardry.librarianlib.common.util.ItemNBTHelper
import com.teamwizardry.librarianlib.common.util.autoregister.TileRegister
import com.teamwizardry.librarianlib.common.util.saving.SaveMethodGetter
import com.teamwizardry.librarianlib.common.util.saving.SaveMethodSetter
import com.teamwizardry.librarianlib.common.util.sendSpamlessMessage
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import talent.bearers.ccomp.api.misc.IPulsarUsable
import talent.bearers.ccomp.common.items.ItemPulsar
import net.minecraft.item.ItemBlock
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.WorldServer
import net.minecraftforge.energy.EnergyStorage
import net.minecraftforge.fluids.*
import net.minecraftforge.fml.relauncher.FMLLaunchHandler.side
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import talent.bearers.ccomp.MODID
import talent.bearers.ccomp.api.packet.IPacket
import talent.bearers.ccomp.api.pathing.IDataNode
import talent.bearers.ccomp.client.core.TankModel
import talent.bearers.ccomp.common.core.ContainerBlockCC
import talent.bearers.ccomp.common.core.FluidStack
import talent.bearers.ccomp.common.packets.FluidPacket
import talent.bearers.ccomp.common.packets.SignalPacket
import javax.annotation.Nonnull





/**
 * @author WireSegal
 * Created at 4:25 PM on 3/6/17.
 */
class BlockFluidColumn : ContainerBlockCC("fluid_column", Material.GLASS), IPulsarUsable, IDataNode {
    init {
        setHardness(1f)
    }

    override fun createItemForm(): ItemBlock? {
        return object : ItemModBlock(this), ISpecialModelProvider {
            @SideOnly(Side.CLIENT)
            override fun getSpecialModel(index: Int): IBakedModel? {
                val parent = ModelHandler.resourceLocations[MODID]?.get(ModelHandler.getKey(this, 0)) ?: return null
                val model = Minecraft.getMinecraft()?.renderItem?.itemModelMesher?.modelManager?.getModel(parent as ModelResourceLocation) ?: return null
                return TankModel(model)
            }
        }
    }

    override fun connectionPoint(pos: BlockPos, world: IBlockAccess) = null

    override fun isSideAvailable(side: EnumFacing, pos: BlockPos, world: IBlockAccess): Boolean {
        return side.horizontalIndex == -1
    }

    fun getPacket(strength: Int, pos: BlockPos, world: IBlockAccess, ghost: Boolean): IPacket? {
        val tile = world.getTileEntity(pos) as? TileFluidColumn ?: return null
        val capability = tile.tank
        return FluidPacket.fromFluidHandler(strength, capability, ghost, tile)
    }

    fun getTotalStrength(pos: BlockPos, world: IBlockAccess): IPacket? {
        val capability = (world.getTileEntity(pos) as? TileFluidColumn)?.tank ?: return null
        return SignalPacket.fromFluidHandler(capability)
    }

    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer): IPacket? {
        if (packetType == "fluid") return getPacket(strength, pos, world, true)
        else if (packetType == "signal") return getTotalStrength(pos, world)
        return null
    }

    override fun requestPullPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer)
            = if (packetType == "fluid") getPacket(strength, pos, world, false) else null

    override fun pushPacket(packet: IPacket, pos: BlockPos, world: WorldServer): IPacket? {
        if (packet.type != "fluid") return packet
        val capability = (world.getTileEntity(pos) as? TileFluidColumn) ?: return packet
        return FluidPacket.transfer(packet, capability.tank, capability)
    }

    companion object {
        val AABB = AxisAlignedBB(2 / 16.0, 0.0, 2 / 16.0, 14 / 16.0, 1.0, 14 / 16.0)
    }

    override fun getBoundingBox(state: IBlockState?, source: IBlockAccess?, pos: BlockPos?) = AABB

    override fun createTileEntity(world: World, state: IBlockState) = TileFluidColumn()

    override fun performHUDOverlay(playerIn: EntityPlayer, worldIn: World, pos: BlockPos, ray: RayTraceResult, resolution: Any): String? {
        val fluid = (worldIn.getTileEntity(pos) as? TileFluidColumn)?.tank?.fluid ?: return TooltipHelper.local("$MODID.hud.nofluid")
        return TooltipHelper.local("$MODID.hud.fluid", fluid.amount, fluid.localizedName)
    }

    override fun addInformation(stack: ItemStack, player: EntityPlayer, tooltip: MutableList<String>, advanced: Boolean) {
        if (ItemNBTHelper.verifyExistence(stack, "fluid")) {
            val fluid = FluidStack(ItemNBTHelper.getCompound(stack, "fluid", false)!!)
            TooltipHelper.addToTooltip(tooltip, "$MODID.hud.fluid", fluid.amount, fluid.localizedName)
        }
        super.addInformation(stack, player, tooltip, advanced)
    }

    override fun onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack) {
        if (ItemNBTHelper.verifyExistence(stack, "fluid")) {
            val fluid = FluidStack(ItemNBTHelper.getCompound(stack, "fluid", false)!!)
            val te = worldIn.getTileEntity(pos)
            if (te is TileFluidColumn)
                te.tank.fluid = fluid
        }
    }

    override fun customDropImplementation(stack: ItemStack, playerIn: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        val drop = ItemStack(this)
        val tile = worldIn.getTileEntity(pos) as? TileFluidColumn ?: return false
        val fluid = tile.tank.fluid ?: return false
        ItemNBTHelper.setCompound(drop, "fluid", fluid.writeToNBT(NBTTagCompound()))
        spawnAsEntity(worldIn, pos, drop)
        return true
    }

    override fun onBlockActivated(worldIn: World, pos: BlockPos?, state: IBlockState?, playerIn: EntityPlayer?, hand: EnumHand?, heldItem: ItemStack?, side: EnumFacing?, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        val te = worldIn.getTileEntity(pos)
        if(te == null || !te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) return false

        val fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)
        FluidUtil.interactWithFluidHandler(heldItem, fluidHandler, playerIn)
        return FluidUtil.getFluidHandler(heldItem) != null
    }

    override fun getLightValue(@Nonnull state: IBlockState, world: IBlockAccess, @Nonnull pos: BlockPos): Int {
        val te = world.getTileEntity(pos) as? TileFluidColumn
        return te?.tank?.fluid?.fluid?.luminosity ?: 0
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    override fun getBlockLayer() = BlockRenderLayer.CUTOUT

    override fun isFullCube(state: IBlockState) = false
    override fun isOpaqueCube(state: IBlockState) = false
    override fun hasComparatorInputOverride(state: IBlockState) = true

    override fun getComparatorInputOverride(blockState: IBlockState, world: World, pos: BlockPos): Int {
        val te = world.getTileEntity(pos) as? TileFluidColumn ?: return 0

        return 15 * te.tank.fluidAmount / te.tank.capacity
    }

    @TileRegister("fluid_column")
    class TileFluidColumn : TileMod() {
        val tank = FluidTank(Fluid.BUCKET_VOLUME * 4)

        @SaveMethodGetter("tank")
        private fun tankCompound() = tank.writeToNBT(NBTTagCompound())
        @SaveMethodSetter("tank")
        private fun deserTank(nbt: NBTTagCompound) = tank.readFromNBT(nbt)

        fun shouldProvide(facing: EnumFacing?) = facing == null || facing == EnumFacing.UP || facing == EnumFacing.DOWN

        override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
            return (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && shouldProvide(facing)) || super.hasCapability(capability, facing)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && shouldProvide(facing))
                return tank as T
            return super.getCapability(capability, facing)
        }
    }
}
