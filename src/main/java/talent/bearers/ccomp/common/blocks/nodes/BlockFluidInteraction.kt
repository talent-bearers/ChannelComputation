package talent.bearers.ccomp.common.blocks.nodes

import net.minecraft.block.BlockLiquid
import net.minecraft.init.SoundEvents
import net.minecraft.util.EnumFacing
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fluids.IFluidBlock
import talent.bearers.ccomp.api.packet.IPacket
import talent.bearers.ccomp.common.blocks.base.BlockBaseInteraction
import talent.bearers.ccomp.common.packets.FluidPacket
import talent.bearers.ccomp.common.packets.SignalPacket

/**
 * @author WireSegal
 * Created at 11:00 AM on 3/2/17.
 */
class BlockFluidInteraction : BlockBaseInteraction("fluid_interaction") {
    override fun requestReadPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer): IPacket? {
        if (packetType == "fluid") return getPacket(strength, pos, world, true)
        else if (packetType == "signal") return getTotalStrength(pos, world)
        return null
    }

    fun getPacket(strength: Int, pos: BlockPos, world: World, ghost: Boolean): IPacket? {
        val target = getTarget(pos, world)
        if (target.state.block !is IFluidBlock && target.state.block !is BlockLiquid) return null
        val capability = FluidUtil.getFluidHandler(world, target.pos, target.facing) ?: return null
        val ret = FluidPacket.fromFluidHandler(strength, capability, ghost)
        if (ret.size != 0 && !ghost)
            world.playSound(null, target.pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1f, 1f)
        return ret
    }

    fun getTotalStrength(pos: BlockPos, world: World): IPacket? {
        val target = getTarget(pos, world)
        if (target.state.block !is IFluidBlock && target.state.block !is BlockLiquid) return null
        val capability = FluidUtil.getFluidHandler(world, target.pos, target.facing) ?: return null
        return SignalPacket.fromFluidHandler(capability)
    }

    override fun requestPullPacket(packetType: String, strength: Int, pos: BlockPos, world: WorldServer)
            = if (packetType == "fluid") getPacket(strength, pos, world, false) else null

    override fun pushPacket(packet: IPacket, pos: BlockPos, world: WorldServer): IPacket? {
        if (packet.type != "fluid") return packet
        val target = getTarget(pos, world)
        val fluids = FluidPacket.getFluids(packet)

        val firstOrig = fluids.firstOrNull { it.amount >= Fluid.BUCKET_VOLUME }
        val first = firstOrig?.copy() ?:
                return if (fluids.isEmpty()) null else packet

        val success = FluidUtil.tryPlaceFluid(null, world, first, target.pos)
        if (success) {
            world.playSound(null, target.pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1f, 1f)
            val newFirstSize = first.amount - Fluid.BUCKET_VOLUME
            val others = fluids.filter { it !== firstOrig }
            if (newFirstSize != 0) {
                val newFirst = first.copy()
                newFirst.amount = newFirstSize
                others.toMutableList().add(0, newFirst)
            }
            return FluidPacket(others)
        }
        return packet
    }
}
