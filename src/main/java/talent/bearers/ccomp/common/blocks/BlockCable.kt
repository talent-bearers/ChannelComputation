package talent.bearers.ccomp.common.blocks

import com.google.common.collect.Lists
import com.teamwizardry.librarianlib.client.util.TooltipHelper
import com.teamwizardry.librarianlib.common.base.block.BlockMod
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import talent.bearers.ccomp.api.pathing.ICrawlableCable
import talent.bearers.ccomp.common.core.BlockCC


/**
 * @author WireSegal
 * Created at 9:05 PM on 3/1/17.
 */
class BlockCable : BlockCC("cable", Material.IRON), ICrawlableCable {

    companion object {
        val UP: PropertyBool = PropertyBool.create("up")
        val DOWN: PropertyBool = PropertyBool.create("down")
        val WEST: PropertyBool = PropertyBool.create("west")
        val EAST: PropertyBool = PropertyBool.create("east")
        val NORTH: PropertyBool = PropertyBool.create("north")
        val SOUTH: PropertyBool = PropertyBool.create("south")

        private val CENTER_AABB = AxisAlignedBB(6 / 16.0, 6 / 16.0, 6 / 16.0, 10 / 16.0, 10 / 16.0, 10 / 16.0)

        private val UP_AABB = AxisAlignedBB(6 / 16.0, 10 / 16.0, 6 / 16.0, 10 / 16.0, 1.0, 10 / 16.0)
        private val DOWN_AABB = AxisAlignedBB(6 / 16.0, 0.0, 6 / 16.0, 10 / 16.0, 6 / 16.0, 10 / 16.0)
        private val NORTH_AABB = AxisAlignedBB(6 / 16.0, 6 / 16.0, 0.0, 10 / 16.0, 10 / 16.0, 6 / 16.0)
        private val SOUTH_AABB = AxisAlignedBB(6 / 16.0, 6 / 16.0, 10 / 16.0, 10 / 16.0, 10 / 16.0, 1.0)
        private val WEST_AABB = AxisAlignedBB(0.0, 6 / 16.0, 6 / 16.0, 6 / 16.0, 10 / 16.0, 10 / 16.0)
        private val EAST_AABB = AxisAlignedBB(10 / 16.0, 6 / 16.0, 6 / 16.0, 1.0, 10 / 16.0, 10 / 16.0)

        val PROPERTIES = mapOf(
                EnumFacing.UP to UP,
                EnumFacing.DOWN to DOWN,
                EnumFacing.WEST to WEST,
                EnumFacing.EAST to EAST,
                EnumFacing.NORTH to NORTH,
                EnumFacing.SOUTH to SOUTH
        )

        val PROP_TO_AABB = mapOf(
                UP to UP_AABB,
                DOWN to DOWN_AABB,
                WEST to WEST_AABB,
                EAST to EAST_AABB,
                NORTH to NORTH_AABB,
                SOUTH to SOUTH_AABB
        )

        private val AABBS = mutableMapOf<IBlockState, AxisAlignedBB>()
    }

    init {
        setHardness(1f)
        setLightLevel(0.5f)
        for (state in blockState.validStates) {
            var aabb = CENTER_AABB
            for ((prop, bound) in PROP_TO_AABB.entries) if (state.getValue(prop)) aabb = aabb.union(bound)
            AABBS.put(state, aabb)
        }
    }

    fun getBoxes(state: IBlockState, worldIn: World, pos: BlockPos): List<AxisAlignedBB> {
        val ret = mutableListOf(CENTER_AABB)
        val actualState = getActualState(state, worldIn, pos)
        for ((prop, bound) in PROP_TO_AABB.entries) if (actualState.getValue(prop))
            ret.add(bound)
        return ret
    }

    override fun collisionRayTrace(blockState: IBlockState, worldIn: World, pos: BlockPos, start: Vec3d, end: Vec3d): RayTraceResult? {
        return getBoxes(blockState, worldIn, pos).mapNotNull { rayTrace(pos, start, end, it) }.maxBy { it.hitVec.squareDistanceTo(end) }
    }

    override fun isFullCube(state: IBlockState) = false
    override fun isOpaqueCube(blockState: IBlockState) = false

    override fun getActualState(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): IBlockState {
        var returnState = state
        for ((facing, prop) in PROPERTIES.entries) returnState = returnState.withProperty(prop, connectedOnSide(facing, pos, worldIn))
        return returnState
    }

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos)
            = AABBS[getActualState(state, source, pos)] ?: CENTER_AABB

    override fun addCollisionBoxToList(state: IBlockState, worldIn: World, pos: BlockPos, entityBox: AxisAlignedBB, collidingBoxes: MutableList<AxisAlignedBB>, entityIn: Entity?) {
        for (box in getBoxes(state, worldIn, pos)) addCollisionBoxToList(pos, entityBox, collidingBoxes, box)
    }

    override fun createBlockState() = BlockStateContainer(this, UP, DOWN, WEST, EAST, NORTH, SOUTH)
    override fun getMetaFromState(state: IBlockState?) = 0
    override fun canHarvestBlock(world: IBlockAccess, pos: BlockPos, player: EntityPlayer) = true

    override fun isSideAvailable(side: EnumFacing, pos: BlockPos, world: IBlockAccess) = true
}
