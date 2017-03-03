package talent.bearers.ccomp.common.blocks

import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import talent.bearers.ccomp.api.packet.IPacket

/**
 * @author WireSegal
 * Created at 11:00 AM on 3/2/17.
 */
class BlockSignalNode : BlockBaseNode("signal_node") {
    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: IBlockAccess): IPacket? {
        return null //todo
    }

    override fun requestPullPacket(packetType: String, strength: Int, pos: BlockPos, world: World) = null

    override fun pushPacket(packet: IPacket, pos: BlockPos, world: World): IPacket? {
        if (packet.type != "signal") return packet
        //todo signal holding
        return null
    }
}
