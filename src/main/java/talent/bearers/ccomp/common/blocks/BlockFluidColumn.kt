package talent.bearers.ccomp.common.blocks

import com.teamwizardry.librarianlib.common.base.block.BlockModContainer
import com.teamwizardry.librarianlib.common.base.block.TileMod
import com.teamwizardry.librarianlib.common.util.autoregister.TileRegister
import com.teamwizardry.librarianlib.common.util.saving.SaveMethodGetter
import com.teamwizardry.librarianlib.common.util.saving.SaveMethodSetter
import com.teamwizardry.librarianlib.common.util.sendSpamlessMessage
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
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
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidTank
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import talent.bearers.ccomp.api.misc.IPulsarUsable
import talent.bearers.ccomp.common.items.ItemPulsar
import net.minecraft.item.ItemBlock
import net.minecraft.util.BlockRenderLayer
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fml.relauncher.FMLLaunchHandler.side
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import javax.annotation.Nonnull





/**
 * @author WireSegal
 * Created at 4:25 PM on 3/6/17.
 */
class BlockFluidColumn : BlockModContainer("fluid_column", Material.GLASS), IPulsarUsable { // todo make data node
    init {
        setHardness(1f)
    }

    companion object {
        val AABB = AxisAlignedBB(2 / 16.0, 0.0, 2 / 16.0, 14 / 16.0, 1.0, 14 / 16.0)
    }

    override fun getBoundingBox(state: IBlockState?, source: IBlockAccess?, pos: BlockPos?) = AABB

    override fun createTileEntity(world: World, state: IBlockState) = TileFluidColumn()

    override fun onPulsarUse(stack: ItemStack, playerIn: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {
        if (!worldIn.isRemote) {
            //todo localize
            val fluid = (worldIn.getTileEntity(pos) as? TileFluidColumn)?.tank?.fluid
            if (fluid == null) playerIn.sendSpamlessMessage("Empty", ItemPulsar.CHANNEL_ID)
            else playerIn.sendSpamlessMessage("${fluid.localizedName}x${fluid.amount}", ItemPulsar.CHANNEL_ID)
        }
        return EnumActionResult.SUCCESS
    }

    override fun onBlockActivated(worldIn: World, pos: BlockPos?, state: IBlockState?, playerIn: EntityPlayer?, hand: EnumHand?, heldItem: ItemStack?, side: EnumFacing?, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        val te = worldIn.getTileEntity(pos)
        if(te == null || !te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) return false

        val fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)
        return FluidUtil.interactWithFluidHandler(heldItem, fluidHandler, playerIn)
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
