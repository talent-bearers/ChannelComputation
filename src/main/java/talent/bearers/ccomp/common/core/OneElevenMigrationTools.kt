package talent.bearers.ccomp.common.core

import com.teamwizardry.librarianlib.common.util.isEmpty
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids.FluidStack

/**
 * @author WireSegal
 * Created at 8:06 PM on 3/1/17.
 */
val EMPTY: ItemStack? = null

val ItemStack?.isEmpty: Boolean get() = this == null || stackSize == 0

var ItemStack?.count: Int
    get() = if (this == null || isEmpty) 0 else stackSize
    set(value) { if (this != null && !isEmpty) stackSize = value }

fun ItemStack(compound: NBTTagCompound): ItemStack = ItemStack.loadItemStackFromNBT(compound)
fun FluidStack(compound: NBTTagCompound): FluidStack = FluidStack.loadFluidStackFromNBT(compound)
