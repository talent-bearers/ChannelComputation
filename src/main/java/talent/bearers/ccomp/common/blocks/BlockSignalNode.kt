package talent.bearers.ccomp.common.blocks

import com.teamwizardry.librarianlib.common.base.block.TileMod
import com.teamwizardry.librarianlib.common.util.autoregister.TileRegister
import com.teamwizardry.librarianlib.common.util.saving.Save
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import talent.bearers.ccomp.api.packet.IPacket
import talent.bearers.ccomp.common.packets.SignalPacket

/**
 * @author WireSegal
 * Created at 11:00 AM on 3/2/17.
 */
class BlockSignalNode : BlockBaseNode("signal_node") {
    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: IBlockAccess): IPacket? {
        if (packetType != "signal") return null
        return SignalPacket(world.getStrongPower(pos, connectionPoint(pos, world)))
    }

    override fun requestPullPacket(packetType: String, strength: Int, pos: BlockPos, world: World) = null

    override fun pushPacket(packet: IPacket, pos: BlockPos, world: World): IPacket? {
        if (packet.type != "signal") return packet
        val tile = world.getTileEntity(pos) as? TileSignalNode
        tile?.signal = SignalPacket.getStrength(packet).toByte()
        world.notifyNeighborsOfStateChange(pos, this)
        tile?.markDirty()
        return null
    }

    override fun getStrongPower(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing?)
            = getWeakPower(blockState, blockAccess, pos, side)
    override fun getWeakPower(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing?)
            = if (side == connectionPoint(pos, blockAccess))
                (blockAccess.getTileEntity(pos) as? TileSignalNode)?.signal?.toInt() ?: 0
            else 0

    override fun hasTileEntity(state: IBlockState?) = true

    override fun createTileEntity(world: World, state: IBlockState) = TileSignalNode()

    @TileRegister("signal_node") class TileSignalNode(@Save var signal: Byte = 0) : TileMod()
}
