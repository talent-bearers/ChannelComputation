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
class BlockFluidInteraction : BlockBaseInteraction("fluid_interaction") {
    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: World): IPacket? {
        return null //todo signal or parse pump
    }

    override fun requestPullPacket(packetType: String, strength: Int, pos: BlockPos, world: World): IPacket? {
        return null // todo taking fluid
    }

    override fun pushPacket(packet: IPacket, pos: BlockPos, world: World): IPacket? {
       return null // todo pushing fluid out
    }
}
