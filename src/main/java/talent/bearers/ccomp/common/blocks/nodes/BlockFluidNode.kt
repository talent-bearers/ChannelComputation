package talent.bearers.ccomp.common.blocks.nodes

import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import talent.bearers.ccomp.api.packet.IPacket
import talent.bearers.ccomp.common.blocks.base.BlockBaseNode
import talent.bearers.ccomp.common.packets.EnergyPacket
import talent.bearers.ccomp.common.packets.FluidPacket
import talent.bearers.ccomp.common.packets.SignalPacket

/**
 * @author WireSegal
 * Created at 11:00 AM on 3/2/17.
 */
class BlockFluidNode : BlockBaseNode("fluid_node") {
    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer): IPacket? {
        if (packetType == "fluid") return getPacket(strength, pos, world, true)
        else if (packetType == "signal") return getTotalStrength(pos, world)
        return null
    }

    fun getPacket(strength: Int, pos: BlockPos, world: IBlockAccess, ghost: Boolean): IPacket? {
        val target = getTarget(pos, world)
        if (target.tile == null || !target.tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.facing)) return null
        val capability = target.tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.facing)
        return FluidPacket.fromFluidHandler(strength, capability, ghost, target.tile)
    }

    fun getTotalStrength(pos: BlockPos, world: IBlockAccess): IPacket? {
        val target = getTarget(pos, world)
        if (target.tile == null || !target.tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.facing)) return null
        val capability = target.tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.facing)
        return SignalPacket.fromFluidHandler(capability)
    }


    override fun requestPullPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer)
            = if (packetType == "fluid") getPacket(strength, pos, world, false) else null

    override fun pushPacket(packet: IPacket, pos: BlockPos, world: WorldServer): IPacket? {
        if (packet.type != "fluid") return packet
        val target = getTarget(pos, world)
        if (target.tile == null || !target.tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.facing)) return packet
        return FluidPacket.transfer(packet, target.tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.facing), target.tile)
    }
}
