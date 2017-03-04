package talent.bearers.ccomp.common.blocks

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import talent.bearers.ccomp.api.packet.IPacket
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
    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: World): IPacket? {
        if (packetType == "item") return getPacket(strength, pos, world, true)
        else if (packetType == "signal") return getTotalStrength(pos, world)
        return null
    }

    fun getPacket(strength: Int, pos: BlockPos, world: IBlockAccess, ghost: Boolean): IPacket? {
        val target = getTarget(pos, world)
        if (target.tile == null || !target.tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing)) return null
        val capability = target.tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing)
        val stacks = mutableListOf<ItemStack>()
        var toTake = if (strength == -1) Int.MAX_VALUE else strength
        for (i in 0 until capability.slots) {
            val taken = capability.extractItem(i, toTake, ghost)
            if (!taken.isEmpty) {
                stacks.add(taken)
                toTake -= taken.count
            }
        }
        return ItemPacket(stacks, ghost)
    }

    fun getTotalStrength(pos: BlockPos, world: IBlockAccess): IPacket? {
        val target = getTarget(pos, world)
        if (target.tile == null || !target.tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing)) return null
        val capability = target.tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing)
        var percent = 0f
        for (i in 0 until capability.slots) {
            val inSlot = capability.getStackInSlot(i)
            percent += inSlot.count.toFloat() / getMaxStackSize(i, capability, inSlot)
        }
        percent /= capability.slots
        return SignalPacket(percent)
    }

    fun getMaxStackSize(slot: Int, handler: IItemHandler, inSlot: ItemStack?): Int {
        if (inSlot == null || inSlot.isEmpty) return 64
        val stack = inSlot.copy()
        stack.count = inSlot.maxStackSize - inSlot.count
        val result = handler.insertItem(slot, stack, true)
        return inSlot.maxStackSize - result.count
    }

    override fun requestPullPacket(packetType: String, strength: Int, pos: BlockPos, world: World)
            = if (packetType == "item") getPacket(strength, pos, world, false) else null

    override fun pushPacket(packet: IPacket, pos: BlockPos, world: World): IPacket? {
        if (packet.type != "item") return packet
        val target = getTarget(pos, world)
        if (target.tile == null || !target.tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing)) return packet
        return ItemPacket.transfer(packet, target.tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing))
    }
}
