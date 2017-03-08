package talent.bearers.ccomp.common.blocks.nodes

import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import talent.bearers.ccomp.api.packet.IPacket
import talent.bearers.ccomp.common.blocks.base.BlockBaseLancer
import talent.bearers.ccomp.common.packets.EnergyPacket
import talent.bearers.ccomp.common.packets.SignalPacket

/**
 * @author WireSegal
 * Created at 11:00 AM on 3/2/17.
 */
class BlockEnergyLancer : BlockBaseLancer("energy_lancer") {
    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer): IPacket? {
        if (packetType == "energy") return getPacket(strength, pos, world)
        else if (packetType == "signal") return getTotalStrength(pos, world)
        return null
    }

    fun getPacket(strength: Int, pos: BlockPos, world: IBlockAccess): IPacket? {
        val target = getTarget(pos, world) { pos, tile, facing ->
            tile?.hasCapability(CapabilityEnergy.ENERGY, facing) ?: false
        }
        if (target.tile == null || !target.tile.hasCapability(CapabilityEnergy.ENERGY, target.facing)) return null
        val capability = target.tile.getCapability(CapabilityEnergy.ENERGY, target.facing)
        return EnergyPacket.fromEnergyStorage(strength, capability, true)
    }

    fun getTotalStrength(pos: BlockPos, world: IBlockAccess): IPacket? {
        val target = getTarget(pos, world) { pos, tile, facing ->
            tile?.hasCapability(CapabilityEnergy.ENERGY, facing) ?: false
        }
        if (target.tile == null || !target.tile.hasCapability(CapabilityEnergy.ENERGY, target.facing)) return null
        val capability = target.tile.getCapability(CapabilityEnergy.ENERGY, target.facing)
        return SignalPacket.fromEnergyStorage(capability)
    }
}
