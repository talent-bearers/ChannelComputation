package talent.bearers.ccomp.common.blocks.nodes

import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.items.CapabilityItemHandler
import talent.bearers.ccomp.api.packet.IPacket
import talent.bearers.ccomp.common.blocks.base.BlockBaseLancer
import talent.bearers.ccomp.common.packets.EnergyPacket
import talent.bearers.ccomp.common.packets.FluidPacket
import talent.bearers.ccomp.common.packets.SignalPacket

/**
 * @author WireSegal
 * Created at 11:00 AM on 3/2/17.
 */
class BlockFluidLancer : BlockBaseLancer("fluid_lancer") {
    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer): IPacket? {
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
        return FluidPacket.fromFluidHandler(strength, capability, true)
    }

    fun getTotalStrength(pos: BlockPos, world: IBlockAccess): IPacket? {
        val target = getTarget(pos, world) { pos, tile, facing ->
            tile?.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing) ?: false
        }
        if (target.tile == null || !target.tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.facing)) return null
        val capability = target.tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.facing)
        return SignalPacket.fromFluidHandler(capability)
    }
}
