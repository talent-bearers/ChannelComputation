package talent.bearers.ccomp.common.blocks

import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.items.CapabilityItemHandler
import talent.bearers.ccomp.api.packet.IPacket
import talent.bearers.ccomp.common.packets.EnergyPacket
import talent.bearers.ccomp.common.packets.FluidPacket
import talent.bearers.ccomp.common.packets.SignalPacket

/**
 * @author WireSegal
 * Created at 11:00 AM on 3/2/17.
 */
class BlockFluidLancer : BlockBaseLancer("fluid_lancer") {
    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: IBlockAccess): IPacket? {
        if (packetType == "fluid") return getPacket(strength, pos, world)
        else if (packetType == "signal") return getTotalStrength(pos, world)
        return null
    }

    fun getPacket(strength: Int, pos: BlockPos, world: IBlockAccess): IPacket? {
        val target = getTarget(pos, world) { pos, tile, facing ->
            tile?.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing) ?: false
        }
        if (target.tile == null || !target.tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.facing)) return null
        val capability = target.tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.facing)
        val fluids = mutableListOf<FluidStack>()
        var toTake = if (strength == -1) Int.MAX_VALUE else strength
        for (i in capability.tankProperties) {
            val stack = i.contents ?: continue
            val taken = capability.drain(stack, false)
            if (taken != null && taken.amount == 0) {
                fluids.add(taken)
                toTake -= taken.amount
            }
        }
        return FluidPacket(fluids, true)
    }

    fun getTotalStrength(pos: BlockPos, world: IBlockAccess): IPacket? {
        val target = getTarget(pos, world) { pos, tile, facing ->
            tile?.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing) ?: false
        }
        if (target.tile == null || !target.tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.facing)) return null
        val capability = target.tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.facing)
        var percent = 0f
        var tanks = 0
        for (i in capability.tankProperties) {
            percent += (i.contents?.amount ?: 0).toFloat() / i.capacity
            tanks++
        }
        percent /= tanks
        return SignalPacket(percent)
    }
}
