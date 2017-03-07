package talent.bearers.ccomp.common.blocks

import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.energy.CapabilityEnergy
import talent.bearers.ccomp.api.packet.IPacket
import talent.bearers.ccomp.common.packets.EnergyPacket
import talent.bearers.ccomp.common.packets.SignalPacket

/**
 * @author WireSegal
 * Created at 11:00 AM on 3/2/17.
 */
class BlockEnergyNode : BlockBaseNode("energy_node") {
    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer): IPacket? {
        if (packetType == "energy") return getPacket(strength, pos, world, true)
        else if (packetType == "signal") return getTotalStrength(pos, world)
        return null
    }

    fun getPacket(strength: Int, pos: BlockPos, world: IBlockAccess, ghost: Boolean): IPacket? {
        val target = getTarget(pos, world)
        if (target.tile == null || !target.tile.hasCapability(CapabilityEnergy.ENERGY, target.facing)) return null
        val amount = if (strength == -1) Int.MAX_VALUE else strength
        val capability = target.tile.getCapability(CapabilityEnergy.ENERGY, target.facing)
        val amountTaken = capability.extractEnergy(amount, ghost)
        if (amountTaken != 0 && !ghost) target.tile.markDirty()
        return EnergyPacket(amountTaken, ghost)
    }

    fun getTotalStrength(pos: BlockPos, world: IBlockAccess): IPacket? {
        val target = getTarget(pos, world)
        if (target.tile == null || !target.tile.hasCapability(CapabilityEnergy.ENERGY, target.facing)) return null
        val capability = target.tile.getCapability(CapabilityEnergy.ENERGY, target.facing)
        return SignalPacket(capability.energyStored.toFloat() / capability.maxEnergyStored)
    }

    override fun requestPullPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer)
            = if (packetType == "energy") getPacket(strength, pos, world, false) else null

    override fun pushPacket(packet: IPacket, pos: BlockPos, world: WorldServer): IPacket? {
        if (packet.type != "energy") return packet
        val target = getTarget(pos, world)
        if (target.tile == null || !target.tile.hasCapability(CapabilityEnergy.ENERGY, target.facing)) return packet
        return EnergyPacket.transfer(packet, target.tile.getCapability(CapabilityEnergy.ENERGY, target.facing), target.tile)
    }
}
