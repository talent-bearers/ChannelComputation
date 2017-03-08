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
import talent.bearers.ccomp.common.blocks.ModBlocks
import talent.bearers.ccomp.common.blocks.base.BlockBaseLancer
import talent.bearers.ccomp.common.core.count
import talent.bearers.ccomp.common.core.isEmpty
import talent.bearers.ccomp.common.packets.FluidPacket
import talent.bearers.ccomp.common.packets.ItemPacket
import talent.bearers.ccomp.common.packets.SignalPacket

/**
 * @author WireSegal
 * Created at 11:00 AM on 3/2/17.
 */
class BlockItemLancer : BlockBaseLancer("item_lancer") {
    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer): IPacket? {
        if (packetType == "item") return getPacket(strength, pos, world)
        else if (packetType == "signal") return getTotalStrength(pos, world)
        return null
    }

    fun getPacket(strength: Int, pos: BlockPos, world: IBlockAccess): IPacket? {
        val target = getTarget(pos, world) { pos, tile, facing ->
            tile?.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing) ?: false
        }
        if (target.tile == null || !target.tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing)) return null
        val capability = target.tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing)
        return ItemPacket.fromItemHandler(strength, capability, true, target.tile)
    }

    fun getTotalStrength(pos: BlockPos, world: IBlockAccess): IPacket? {
        val target = getTarget(pos, world) { pos, tile, facing ->
            tile?.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing) ?: false
        }
        if (target.tile == null || !target.tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing)) return null
        val capability = target.tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing)
        return SignalPacket.fromItemHandler(capability)
    }
}
