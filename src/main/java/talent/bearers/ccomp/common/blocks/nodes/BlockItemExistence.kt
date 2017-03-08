package talent.bearers.ccomp.common.blocks.nodes

import com.mojang.authlib.GameProfile
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.common.util.FakePlayer
import net.minecraftforge.common.util.FakePlayerFactory
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import talent.bearers.ccomp.api.packet.IPacket
import talent.bearers.ccomp.common.blocks.nodes.BlockItemInteraction.Companion.profile
import talent.bearers.ccomp.common.blocks.base.BlockBaseExistence
import talent.bearers.ccomp.common.blocks.base.BlockBaseInteraction
import talent.bearers.ccomp.common.blocks.base.BlockBaseNode
import talent.bearers.ccomp.common.core.EMPTY
import talent.bearers.ccomp.common.core.count
import talent.bearers.ccomp.common.core.isEmpty
import talent.bearers.ccomp.common.packets.ItemPacket
import talent.bearers.ccomp.common.packets.SignalPacket
import java.util.*
import java.util.stream.Collectors

/**
 * @author WireSegal
 * Created at 11:00 AM on 3/2/17.
 */
class BlockItemExistence : BlockBaseExistence("item_existence") {

    fun itemsAroundBlock(world: World, pos: BlockPos, range: Double = 7.0)
            = world.getEntitiesWithinAABB(EntityItem::class.java, AxisAlignedBB(pos).expandXyz(range)).filter {
                Vec3d(pos).addVector(0.5, 0.5, 0.5).squareDistanceTo(it.positionVector) < range * range
            }

    fun packetForItems(pos: BlockPos, world: World, strength: Int, ghost: Boolean): IPacket? {
        val items = itemsAroundBlock(world, pos)
        val toTake = if (strength == -1) Int.MAX_VALUE else strength
        val ret = mutableListOf<ItemStack>()
        for (i in items) {
            val item = i.entityItem
            if (item.count < toTake) {
                ret.add(i.entityItem.copy())
                if (!ghost) i.setDead()
            } else {
                val stack = i.entityItem.copy()
                if (!ghost) i.entityItem.count -= toTake
                stack.count = toTake
                ret.add(stack)
                break
            }
        }
        return ItemPacket(ret, ghost)
    }

    fun ejectItem(stack: ItemStack, pos: BlockPos, world: World) {
        val item = EntityItem(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, stack)
		item.motionX = 0.0
		item.motionY = 0.0
		item.motionZ = 0.0
        world.spawnEntityInWorld(item)
    }

    override fun requestPullPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer)
            = if (packetType == "item") packetForItems(pos, world, strength, false) else null


    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer)
            = if (packetType == "item") packetForItems(pos, world, strength, true) else null


    override fun pushPacket(packet: IPacket, pos: BlockPos, world: WorldServer): IPacket? {
        if (packet.type != "item") return packet

        val state = world.getBlockState(pos)
        val facing = state.getValue(BlockBaseNode.FACING).opposite
        val offset = pos.offset(facing)
        if (!world.isAirBlock(offset)) return packet
        for (item in ItemPacket.getItems(packet))
            ejectItem(item, offset, world)
        return null
    }


}
