package talent.bearers.ccomp.common.blocks

import com.teamwizardry.librarianlib.client.core.JsonGenerationUtils
import com.teamwizardry.librarianlib.client.core.ModelHandler
import com.teamwizardry.librarianlib.client.util.TooltipHelper
import com.teamwizardry.librarianlib.common.base.IModelGenerator
import com.teamwizardry.librarianlib.common.base.block.BlockMod
import com.teamwizardry.librarianlib.common.util.builders.json
import net.minecraft.block.Block
import net.minecraft.block.BlockDirectional
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.*
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import talent.bearers.ccomp.api.pathing.IDataNode
import talent.bearers.ccomp.api.packet.IPacket

/**
 * @author WireSegal
 * Created at 11:00 AM on 3/2/17.
 */
abstract class BlockBaseNode(name: String) : BlockMod(name, Material.IRON), IDataNode, IModelGenerator {
    companion object {
        val FACING: PropertyDirection = PropertyDirection.create("attachment")
        val UP_AABB    = AxisAlignedBB(6 / 16.0, 13 / 16.0, 6 / 16.0, 10 / 16.0,      1.0, 10 / 16.0)
        val DOWN_AABB  = AxisAlignedBB(6 / 16.0,       0.0, 6 / 16.0, 10 / 16.0, 3 / 16.0, 10 / 16.0)

        val SOUTH_AABB = AxisAlignedBB(6 / 16.0, 6 / 16.0, 13 / 16.0, 10 / 16.0, 10 / 16.0,      1.0)
        val NORTH_AABB = AxisAlignedBB(6 / 16.0, 6 / 16.0,       0.0, 10 / 16.0, 10 / 16.0, 3 / 16.0)

        val EAST_AABB  = AxisAlignedBB(13 / 16.0, 6 / 16.0, 6 / 16.0,      1.0, 10 / 16.0, 10 / 16.0)
        val WEST_AABB  = AxisAlignedBB(      0.0, 6 / 16.0, 6 / 16.0, 3 / 16.0, 10 / 16.0, 10 / 16.0)

        val AABBS = mapOf(
                UP to UP_AABB,
                DOWN to DOWN_AABB,
                WEST to WEST_AABB,
                EAST to EAST_AABB,
                NORTH to NORTH_AABB,
                SOUTH to SOUTH_AABB
        )


        fun getTarget(pos: BlockPos, worldIn: IBlockAccess): NodeTarget {
            val thisState = worldIn.getBlockState(pos)
            val thisFacing = thisState.getValue(FACING).opposite
            val shift = pos.offset(thisFacing)
            return NodeTarget(shift, thisFacing, worldIn.getBlockState(shift), worldIn.getTileEntity(shift))
        }
    }

    data class NodeTarget(val pos: BlockPos, val facing: EnumFacing, val state: IBlockState, val tile: TileEntity?)

    init {
        blockHardness = 1f
    }

    override fun addInformation(stack: ItemStack, player: EntityPlayer, tooltip: MutableList<String>, advanced: Boolean) {
        TooltipHelper.tooltipIfShift(tooltip) {
            TooltipHelper.addToTooltip(tooltip, stack.unlocalizedName + ".desc")
        }
    }

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess?, pos: BlockPos?) = AABBS[state.getValue(FACING)]

    override fun createBlockState() = BlockStateContainer(this, FACING)

    override fun getMetaFromState(state: IBlockState) = state.getValue(FACING).index
    override fun getStateFromMeta(meta: Int) = defaultState.withProperty(FACING, VALUES[meta % 6])

    override fun isFullCube(state: IBlockState) = false
    override fun isOpaqueCube(blockState: IBlockState) = false

    override fun connectionPoint(pos: BlockPos, world: IBlockAccess): EnumFacing = world.getBlockState(pos).getValue(FACING)
    override fun onBlockPlaced(worldIn: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase): IBlockState
            = defaultState.withProperty(FACING, facing.opposite)

    override fun generateMissingBlockstate(mapper: ((Block) -> Map<IBlockState, ModelResourceLocation>)?): Boolean {
        val name = ResourceLocation(registryName.resourceDomain, "blocks/${registryName.resourcePath}").toString()

        ModelHandler.generateBlockJson(this, {
                JsonGenerationUtils.generateBlockStates(this, mapper) {
                    val facing = "${FACING.name}=(\\w+)".toRegex().find(it)?.groupValues?.get(1)?.toUpperCase()
                    val dir = byName(facing)
                    val x = if (dir == DOWN) 180 else if (dir == SOUTH) 270 else if (dir == UP) 0 else 90
                    val y = if (dir == EAST) 90 else if (dir == WEST) 270 else 0
                    json {
                        obj(
                                "model" to registryName.toString(),
                                *if (x != 0) arrayOf("x" to x) else arrayOf(),
                                *if (y != 0) arrayOf("y" to y) else arrayOf()
                        )
                    }
                }},
                {
                    mapOf(JsonGenerationUtils.getPathForBlockModel(this)
                            to json {
                        obj(
                                "parent" to "ccomp:block/node",
                                "textures" to obj(
                                        "texture" to name
                                )
                        )
                    })
                })
        return true
    }
}
