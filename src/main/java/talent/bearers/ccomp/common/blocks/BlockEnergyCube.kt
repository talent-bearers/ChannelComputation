package talent.bearers.ccomp.common.blocks

import com.teamwizardry.librarianlib.common.base.block.BlockModContainer
import com.teamwizardry.librarianlib.common.base.block.TileMod
import com.teamwizardry.librarianlib.common.util.autoregister.TileRegister
import com.teamwizardry.librarianlib.common.util.saving.SaveMethodGetter
import com.teamwizardry.librarianlib.common.util.saving.SaveMethodSetter
import com.teamwizardry.librarianlib.common.util.sendSpamlessMessage
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.energy.EnergyStorage
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidTank
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import talent.bearers.ccomp.api.misc.IPulsarUsable
import talent.bearers.ccomp.common.items.ItemPulsar

/**
 * @author WireSegal
 * Created at 10:19 AM on 3/7/17.
 */
class BlockEnergyCube : BlockModContainer("energy_cube", Material.IRON, "energy_cube", "creative_cube"), IPulsarUsable {

    companion object {
        val CREATIVE: PropertyBool = PropertyBool.create("creative")
    }

    override fun createBlockState() = BlockStateContainer(this, CREATIVE)

    override fun createTileEntity(world: World, state: IBlockState) = TileEnergyCube(state.getValue(CREATIVE))

    override fun isFullCube(state: IBlockState) = false
    override fun isOpaqueCube(blockState: IBlockState) = false

    override fun hasComparatorInputOverride(state: IBlockState) = true

    override fun getComparatorInputOverride(blockState: IBlockState, world: World, pos: BlockPos): Int {
        val te = world.getTileEntity(pos) as? TileEnergyCube ?: return 0
        return 15 * te.cell.energyStored / te.cell.maxEnergyStored
    }

    override fun damageDropped(state: IBlockState): Int {
        return getMetaFromState(state)
    }

    override fun getMetaFromState(state: IBlockState): Int {
        return if (state.getValue(CREATIVE)) 1 else 0
    }

    override fun getStateFromMeta(meta: Int): IBlockState {
        return defaultState.withProperty(CREATIVE, meta != 0)
    }

    override fun getBlockHardness(blockState: IBlockState, worldIn: World?, pos: BlockPos?): Float {
        return if (blockState.getValue(CREATIVE)) -1f else 1f
    }

    override fun shouldBreak(stack: ItemStack, playerIn: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        return !worldIn.getBlockState(pos).getValue(CREATIVE)
    }

    override fun onPulsarUse(stack: ItemStack, playerIn: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {
        if (!worldIn.isRemote) {
            //todo localize
            if (worldIn.getBlockState(pos).getValue(CREATIVE))
                playerIn.sendSpamlessMessage("∞", ItemPulsar.CHANNEL_ID)
            else {
                val cell = (worldIn.getTileEntity(pos) as? TileEnergyCube)?.cell ?: EnergyStorage(0)
                playerIn.sendSpamlessMessage("${cell.energyStored}/${cell.maxEnergyStored}", ItemPulsar.CHANNEL_ID)
            }
        }
        return EnumActionResult.SUCCESS
    }

    @TileRegister("energy_cube")
    class TileEnergyCube(creative: Boolean = false) : TileMod() {
        val cell = if (creative) InfiniteWrapper(1000000) else EnergyStorageWrapper(100000, 2500)

        open class EnergyStorageWrapper(capacity: Int, maxIn: Int = capacity, maxOut: Int = maxIn): EnergyStorage(capacity, maxIn, maxOut) {
            fun setEnergy(energy: Int) {
                this.energy = energy
            }
        }

        class InfiniteWrapper(capacity: Int) : EnergyStorageWrapper(capacity) {
            override fun getEnergyStored() = capacity

            override fun receiveEnergy(maxReceive: Int, simulate: Boolean) = maxReceive

            override fun extractEnergy(maxExtract: Int, simulate: Boolean)
                    = Math.min(energyStored, Math.min(this.maxExtract, maxExtract))
        }

        @SaveMethodGetter("cell")
        private fun cellStorage() = cell.energyStored
        @SaveMethodSetter("tank")
        private fun deserCell(amount: Int) = cell.setEnergy(amount)

        override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
            return capability == CapabilityEnergy.ENERGY  || super.hasCapability(capability, facing)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
            if (capability == CapabilityEnergy.ENERGY)
                return cell as T
            return super.getCapability(capability, facing)
        }
    }
}
