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
class BlockItemInteraction : BlockBaseInteraction("item_interaction") {
    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: World): IPacket? {
        return null //todo signal or parse placed block
    }

    override fun requestPullPacket(packetType: String, strength: Int, pos: BlockPos, world: World): IPacket? {
        return null // todo taking placed block
    }

    override fun pushPacket(packet: IPacket, pos: BlockPos, world: World): IPacket? {
        return null // todo placing block
    }
}
