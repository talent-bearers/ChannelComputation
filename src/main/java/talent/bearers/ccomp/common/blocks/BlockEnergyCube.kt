package talent.bearers.ccomp.common.blocks

import com.teamwizardry.librarianlib.client.util.TooltipHelper
import com.teamwizardry.librarianlib.common.base.block.BlockModContainer
import com.teamwizardry.librarianlib.common.base.block.TileMod
import com.teamwizardry.librarianlib.common.util.ItemNBTHelper
import com.teamwizardry.librarianlib.common.util.autoregister.TileRegister
import com.teamwizardry.librarianlib.common.util.saving.SaveMethodGetter
import com.teamwizardry.librarianlib.common.util.saving.SaveMethodSetter
import com.teamwizardry.librarianlib.common.util.sendSpamlessMessage
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.energy.EnergyStorage
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidTank
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import talent.bearers.ccomp.MODID
import talent.bearers.ccomp.api.misc.IPulsarUsable
import talent.bearers.ccomp.api.packet.IPacket
import talent.bearers.ccomp.api.pathing.IDataNode
import talent.bearers.ccomp.common.core.FluidStack
import talent.bearers.ccomp.common.items.ItemPulsar
import talent.bearers.ccomp.common.packets.EnergyPacket
import talent.bearers.ccomp.common.packets.SignalPacket

/**
 * @author WireSegal
 * Created at 10:19 AM on 3/7/17.
 */
class BlockEnergyCube : BlockModContainer("energy_cube", Material.IRON, "energy_cube", "creative_cube"), IPulsarUsable, IDataNode {

    companion object {
        val CREATIVE: PropertyBool = PropertyBool.create("creative")
    }

    override fun connectionPoint(pos: BlockPos, world: IBlockAccess) = null

    override fun isSideAvailable(side: EnumFacing, pos: BlockPos, world: IBlockAccess) = true

    fun getPacket(strength: Int, pos: BlockPos, world: IBlockAccess, ghost: Boolean): IPacket? {
        val tile = (world.getTileEntity(pos) as? TileEnergyCube) ?: return null
        val capability = tile.cell
        val amount = if (strength == -1) Int.MAX_VALUE else strength
        val takenAmount = capability.extractEnergy(amount, ghost)
        if (takenAmount != 0 && !ghost) tile.markDirty()
        return EnergyPacket(takenAmount, ghost)
    }

    fun getTotalStrength(pos: BlockPos, world: IBlockAccess): IPacket? {
        val capability = (world.getTileEntity(pos) as? TileEnergyCube)?.cell ?: return null
        return SignalPacket(capability.energyStored.toFloat() / capability.maxEnergyStored)
    }

    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer): IPacket? {
        if (packetType == "energy") return getPacket(strength, pos, world, true)
        else if (packetType == "signal") return getTotalStrength(pos, world)
        return null
    }

    override fun requestPullPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer)
            = if (packetType == "energy") getPacket(strength, pos, world, false) else null

    override fun pushPacket(packet: IPacket, pos: BlockPos, world: WorldServer): IPacket? {
        if (packet.type != "energy") return packet
        val cell = (world.getTileEntity(pos) as? TileEnergyCube) ?: return packet
        return EnergyPacket.transfer(packet, cell.cell, cell)
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

    override fun getHUDOverlay(playerIn: EntityPlayer, worldIn: World, pos: BlockPos, ray: RayTraceResult): String? {
        val value = if (worldIn.getBlockState(pos).getValue(CREATIVE))
            "∞"
        else {
            val cell = (worldIn.getTileEntity(pos) as? TileEnergyCube)?.cell ?: EnergyStorage(0)
            "${cell.energyStored} / ${cell.maxEnergyStored}"
        }
        return TooltipHelper.local("$MODID.hud.energy", value)
    }

    override fun addInformation(stack: ItemStack, player: EntityPlayer?, tooltip: MutableList<String>, advanced: Boolean) {
        if (ItemNBTHelper.verifyExistence(stack, "energy")) {
            val energy = ItemNBTHelper.getInt(stack, "energy", 0)
            TooltipHelper.addToTooltip(tooltip, "$MODID.hud.energy", energy)
        }
    }

    override fun onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack) {
        if (ItemNBTHelper.verifyExistence(stack, "energy")) {
            val energy = ItemNBTHelper.getInt(stack, "energy", 0)
            val te = worldIn.getTileEntity(pos)
            if (te is TileEnergyCube)
                te.cell.setEnergy(energy)
        }
    }

    override fun customDropImplementation(stack: ItemStack, playerIn: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        val drop = ItemStack(this)
        val tile = worldIn.getTileEntity(pos) as? TileEnergyCube ?: return false
        ItemNBTHelper.setInt(drop, "energy", tile.cell.energyStored)
        spawnAsEntity(worldIn, pos, drop)
        return true
    }

    @TileRegister("energy_cube")
    class TileEnergyCube(val creative: Boolean = false) : TileMod() {
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
        @SaveMethodSetter("cell")
        private fun deserCell(amount: Int) {
            if (!creative) cell.setEnergy(amount)
        }

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
