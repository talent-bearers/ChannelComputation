package talent.bearers.ccomp.common.packets

import com.teamwizardry.librarianlib.common.util.NBTTypes
import com.teamwizardry.librarianlib.common.util.builders.JSON.obj
import com.teamwizardry.librarianlib.common.util.builders.nbt
import com.teamwizardry.librarianlib.common.util.forEach
import com.teamwizardry.librarianlib.common.util.get
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.energy.IEnergyStorage
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.ItemHandlerHelper
import sun.audio.AudioPlayer.player
import talent.bearers.ccomp.api.packet.IPacket
import talent.bearers.ccomp.common.core.FluidStack
import talent.bearers.ccomp.common.core.ItemStack
import talent.bearers.ccomp.common.core.count
import talent.bearers.ccomp.common.packets.ItemPacket.Companion.getItems

/**
 * @author WireSegal
 * Created at 9:03 AM on 3/3/17.
 */
class SignalPacket(val strength: Int) : IPacket {
    constructor(strengthPercentage: Float) : this((strengthPercentage * 15).toInt() + if(strengthPercentage > 0.0) 1 else 0)

    companion object {
        fun getStrength(packet: IPacket) = packet.data.getInteger("strength")
    }

    val nbt = nbt {
        comp(
                "strength" to strength
        )
    } as NBTTagCompound

    override fun getType() = "signal"
    override fun isGhost() = true

    override fun getData() = nbt

    override fun getSize() = strength

    override fun dumpData(fluids: IFluidHandler, items: IItemHandler, energy: IEnergyStorage, general: TileEntity) {
        // NO-OP
    }
}

class ItemPacket(vararg val items: ItemStack, val ghost: Boolean = false) : IPacket {
    constructor(items: List<ItemStack>, ghost: Boolean = false) : this(*items.toTypedArray(), ghost = ghost)

    companion object {
        fun getItems(packet: IPacket): List<ItemStack> {
            val nbt = packet.data
            val list = nbt.getTagList("items", NBTTypes.COMPOUND)
            val ret = mutableListOf<ItemStack>()
            list.forEach<NBTTagCompound> { ret.add(ItemStack(it)) }
            return ret
        }

        fun transfer(packet: IPacket, items: IItemHandler, tileEntity: TileEntity): IPacket? {
            if (packet.isGhost) return null
            val newItems = getItems(packet).mapNotNull { ItemHandlerHelper.insertItem(items, it, false) }
            tileEntity.markDirty()
            if (newItems.isEmpty()) return null

            return ItemPacket(newItems)
        }
    }

    override fun getType() = "item"
    override fun isGhost() = ghost

    val nbt = NBTTagCompound()
    init {
        val list = NBTTagList()
        for (i in items) list.appendTag(i.writeToNBT(NBTTagCompound()))
        nbt.setTag("items", list)
    }
    private val size = items.sumBy { it.count }

    override fun getData() = nbt

    override fun getSize() = size

    override fun dumpData(fluids: IFluidHandler, items: IItemHandler, energy: IEnergyStorage, general: TileEntity) {
        val world = general.world
        val pos = general.pos

        this.items
                .mapNotNull { ItemHandlerHelper.insertItem(items, it, false) }
                .forEach {
                    val entityitem = EntityItem(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, it)
                    entityitem.setPickupDelay(40)
                    entityitem.motionX = 0.0
                    entityitem.motionZ = 0.0

                    world.spawnEntityInWorld(entityitem)
                }
    }
}

class FluidPacket(vararg val fluids: FluidStack, val ghost: Boolean = false): IPacket {
    constructor(fluids: List<FluidStack>, ghost: Boolean = false) : this(*fluids.toTypedArray(), ghost = ghost)

    companion object {
        fun getFluids(packet: IPacket): List<FluidStack> {
            val nbt = packet.data
            val list = nbt.getTagList("fluids", NBTTypes.COMPOUND)
            val ret = mutableListOf<FluidStack>()
            list.forEach<NBTTagCompound> { ret.add(FluidStack(it)) }
            return ret
        }

        fun transfer(packet: IPacket, fluids: IFluidHandler, tileEntity: TileEntity): IPacket? {
            if (packet.isGhost) return null
            val newFluids = getFluids(packet).mapNotNull {
                val stack = it.copy()
                val amountTaken = fluids.fill(it, true)
                stack.amount -= amountTaken

                if (stack.amount <= 0) null else stack
            }
            tileEntity.markDirty()
            if (newFluids.isEmpty()) return null

            return FluidPacket(newFluids)
        }
    }

    override fun getType() = "fluid"

    override fun isGhost() = ghost

    val nbt = NBTTagCompound()
    init {
        val list = NBTTagList()
        for (i in fluids) list.appendTag(i.writeToNBT(NBTTagCompound()))
        nbt.setTag("fluids", list)
    }
    private val size = fluids.sumBy { it.amount }

    override fun getData() = nbt

    override fun getSize() = size

    override fun dumpData(fluids: IFluidHandler, items: IItemHandler, energy: IEnergyStorage, general: TileEntity) {
        for (i in this.fluids) fluids.fill(i, true) // Remainder is dumped, unlike items
    }
}

class EnergyPacket(val energy: Int, val ghost: Boolean = false): IPacket {
    companion object {
        fun getEnergy(packet: IPacket) = packet.data.getInteger("energy")

        fun transfer(packet: IPacket, energy: IEnergyStorage, tileEntity: TileEntity): IPacket? {
            if (packet.isGhost) return null
            val stored = getEnergy(packet)
            val amountTaken = energy.receiveEnergy(stored, false)
            tileEntity.markDirty()
            if (amountTaken == stored) return null
            return EnergyPacket(stored - amountTaken)
        }
    }

    override fun getType() = "energy"

    override fun isGhost() = ghost

    val nbt = nbt {
        comp(
                "energy" to energy
        )
    } as NBTTagCompound

    override fun getData() = nbt

    override fun getSize() = energy

    override fun dumpData(fluids: IFluidHandler, items: IItemHandler, energy: IEnergyStorage, general: TileEntity) {
        energy.receiveEnergy(this.energy, false)
    }
}
