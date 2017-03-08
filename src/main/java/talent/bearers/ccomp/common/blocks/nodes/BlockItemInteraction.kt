package talent.bearers.ccomp.common.blocks.nodes

import com.mojang.authlib.GameProfile
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.common.util.FakePlayer
import net.minecraftforge.common.util.FakePlayerFactory
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import talent.bearers.ccomp.api.packet.IPacket
import talent.bearers.ccomp.common.blocks.base.BlockBaseInteraction
import talent.bearers.ccomp.common.blocks.base.BlockBaseNode
import talent.bearers.ccomp.common.core.EMPTY
import talent.bearers.ccomp.common.core.isEmpty
import talent.bearers.ccomp.common.packets.ItemPacket
import talent.bearers.ccomp.common.packets.SignalPacket
import java.util.*

/**
 * @author WireSegal
 * Created at 11:00 AM on 3/2/17.
 */
class BlockItemInteraction : BlockBaseInteraction("item_interaction") {
    companion object {
        val profile = GameProfile(UUID.fromString("7C1C207B-1413-4B13-813F-4464C44A509D"), "[InteractionNode]")
    }

    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer): IPacket? {
        if (packetType == "signal") return SignalPacket(if (shouldBreak(getTarget(pos, world), world)) 15 else 0)
        else if (packetType == "item") return getPacketForDrops(pos, world, true)
        return null
    }

    fun shouldBreak(target: BlockBaseNode.NodeTarget, world: World) = !target.state.block.isAir(target.state, world, target.pos) && !target.state.material.isLiquid

    fun getPacketForDrops(pos: BlockPos, world: World, ghost: Boolean): IPacket? {
        val target = getTarget(pos, world)
        if (!shouldBreak(target, world) || target.state.getBlockHardness(world, target.pos) == -1f) return null
        captureDrops(true)
        if (ghost)
            target.state.block.dropBlockAsItem(world, target.pos, target.state, 0)
        else
            world.destroyBlock(target.pos, true)
        val drops = captureDrops(false)
        return ItemPacket(drops, ghost)
    }

    override fun requestPullPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer)
            = if (packetType == "item") getPacketForDrops(pos, world, false) else null

    override fun pushPacket(packet: IPacket, pos: BlockPos, world: WorldServer): IPacket? {
        if (packet.type != "item") return packet
        val target = getTarget(pos, world)
        val items = ItemPacket.getItems(packet)
        val firstOrig = items.firstOrNull() ?: return packet
        var first = firstOrig.copy()

        val player = FakePlayer(world, profile)
        player.rotationYaw = target.facing.horizontalAngle
        player.rotationPitch = if (target.facing == EnumFacing.UP) -90f else if (target.facing == EnumFacing.DOWN) 90f else 0f
        player.posX = target.pos.x + 0.5
        player.posY = target.pos.y + 0.5 - player.eyeHeight
        player.posZ = target.pos.z + 0.5

        player.setHeldItem(EnumHand.MAIN_HAND, first)
        val result = first.onItemUse(player, world, target.pos, EnumHand.MAIN_HAND, target.facing.opposite, 0f, 0f, 0f)
        if (result == EnumActionResult.PASS) {
            first = firstOrig.copy()
            first = first.useItemRightClick(world, player, EnumHand.MAIN_HAND).result
        }
        val results = items.mapNotNull { if (it !== firstOrig) it else if (first.isEmpty) EMPTY else first }.toMutableList()
        if (results.isEmpty()) return null
        return ItemPacket(results)
    }


}
