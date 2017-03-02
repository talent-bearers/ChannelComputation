package talent.bearers.ccomp.common.blocks

import com.teamwizardry.librarianlib.common.base.block.BlockMod
import net.minecraft.block.BlockDirectional
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import talent.bearers.ccomp.api.IDataNode
import talent.bearers.ccomp.api.IPacket

/**
 * @author WireSegal
 * Created at 11:00 AM on 3/2/17.
 */
class BlockSignalNode : BlockMod("signal_node", Material.IRON), IDataNode {
    companion object {
        val FACING: PropertyDirection = PropertyDirection.create("facing")
        val UP_AABB    = AxisAlignedBB(6 / 16.0, 13 / 16.0, 6 / 16.0, 10 / 16.0,      1.0, 10 / 16.0)
        val DOWN_AABB  = AxisAlignedBB(6 / 16.0,       0.0, 6 / 16.0, 10 / 16.0, 3 / 16.0, 10 / 16.0)

        val SOUTH_AABB = AxisAlignedBB(6 / 16.0, 6 / 16.0, 13 / 16.0, 10 / 16.0, 10 / 16.0,      1.0)
        val NORTH_AABB = AxisAlignedBB(6 / 16.0, 6 / 16.0,       0.0, 10 / 16.0, 10 / 16.0, 3 / 16.0)

        val EAST_AABB  = AxisAlignedBB(13 / 16.0, 6 / 16.0, 6 / 16.0,      1.0, 10 / 16.0, 10 / 16.0)
        val WEST_AABB  = AxisAlignedBB(      0.0, 6 / 16.0, 6 / 16.0, 3 / 16.0, 10 / 16.0, 10 / 16.0)

        val AABBS = mapOf(
                EnumFacing.UP to UP_AABB,
                EnumFacing.DOWN to DOWN_AABB,
                EnumFacing.WEST to WEST_AABB,
                EnumFacing.EAST to EAST_AABB,
                EnumFacing.NORTH to NORTH_AABB,
                EnumFacing.SOUTH to SOUTH_AABB
        )
    }

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess?, pos: BlockPos?) = AABBS[state.getValue(FACING)]

    override fun createBlockState() = BlockStateContainer(this, FACING)

    override fun getMetaFromState(state: IBlockState) = state.getValue(FACING).index
    override fun getStateFromMeta(meta: Int) = defaultState.withProperty(FACING, EnumFacing.VALUES[meta % 6])

    override fun isFullCube(state: IBlockState) = false
    override fun isOpaqueCube(blockState: IBlockState) = false

    override fun connectionPoint(pos: BlockPos, world: IBlockAccess): EnumFacing = world.getBlockState(pos).getValue(FACING)
    override fun onBlockPlaced(worldIn: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase): IBlockState
            = this.defaultState.withProperty(FACING, facing.opposite)

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
