package talent.bearers.ccomp.common.blocks.nodes

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import talent.bearers.ccomp.api.packet.IPacket
import talent.bearers.ccomp.common.blocks.base.BlockBaseNode
import talent.bearers.ccomp.common.core.count
import talent.bearers.ccomp.common.core.isEmpty
import talent.bearers.ccomp.common.packets.FluidPacket
import talent.bearers.ccomp.common.packets.ItemPacket
import talent.bearers.ccomp.common.packets.SignalPacket

/**
 * @author WireSegal
 * Created at 11:00 AM on 3/2/17.
 */
class BlockItemNode : BlockBaseNode("item_node") {
    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer): IPacket? {
        if (packetType == "item") return getPacket(strength, pos, world, true)
        else if (packetType == "signal") return getTotalStrength(pos, world)
        return null
    }

    fun getPacket(strength: Int, pos: BlockPos, world: IBlockAccess, ghost: Boolean): IPacket? {
        val target = getTarget(pos, world)
        if (target.tile == null || !target.tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing)) return null
        val capability = target.tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing)
        return ItemPacket.fromItemHandler(strength, capability, ghost, target.tile)
    }

    fun getTotalStrength(pos: BlockPos, world: IBlockAccess): IPacket? {
        val target = getTarget(pos, world)
        if (target.tile == null || !target.tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing)) return null
        val capability = target.tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing)
        return SignalPacket.fromItemHandler(capability)
    }

    fun getMaxStackSize(slot: Int, handler: IItemHandler, inSlot: ItemStack?): Int {
        if (inSlot == null || inSlot.isEmpty) return 64
        val stack = inSlot.copy()
        stack.count = inSlot.maxStackSize - inSlot.count
        val result = handler.insertItem(slot, stack, true)
        return inSlot.maxStackSize - result.count
    }

    override fun requestPullPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer)
            = if (packetType == "item") getPacket(strength, pos, world, false) else null

    override fun pushPacket(packet: IPacket, pos: BlockPos, world: WorldServer): IPacket? {
        if (packet.type != "item") return packet
        val target = getTarget(pos, world)
        if (target.tile == null || !target.tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing)) return packet
        return ItemPacket.transfer(packet, target.tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing), target.tile)
    }
}
