package talent.bearers.ccomp.common.items

import com.teamwizardry.librarianlib.client.util.pulseColor
import com.teamwizardry.librarianlib.common.base.item.IItemColorProvider
import com.teamwizardry.librarianlib.common.base.item.ItemMod
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary
import talent.bearers.ccomp.common.core.ItemCC
import java.awt.Color

/**
 * @author WireSegal
 * Created at 2:32 PM on 3/5/17.
 */
class ItemEncodedDisk : ItemCC("disk", *VARIANTS), IItemColorProvider {
    companion object {
        val VARIANTS = arrayOf(
                "basic_disk",
                "signal_disk",
                "energy_disk",
                "fluid_disk",
                "item_disk",
                "signal_input_disk",
                "energy_input_disk",
                "fluid_input_disk",
                "item_input_disk",
                "signal_output_disk",
                "energy_output_disk",
                "fluid_output_disk",
                "item_output_disk"
        )

        fun getDiskType(stack: ItemStack) = getDiskType(stack.itemDamage)

        fun getDiskType(meta: Int): Type {
            return when ((meta - 1) % 4) {
                0 -> Type.SIGNAL
                1 -> Type.ENERGY
                2 -> Type.FLUID
                3 -> Type.ITEM
                else -> Type.BASIC
            }
        }

        fun getDiskDirection(meta: Int): Direction {
            return when ((meta - 1) / 4) {
                1 -> Direction.INPUT
                2 -> Direction.OUTPUT
                else -> Direction.BASIC
            }
        }
    }

    init {
        for (diskType in VARIANTS.indices) {
            OreDictionary.registerOre("disk"
                    + getDiskType(diskType).name.toLowerCase().capitalize()
                    + getDiskDirection(diskType).oreDictPostFix,
                    ItemStack(this, 1, diskType))
        }
    }

    enum class Type(val color: Int, val material: Any) {
        BASIC(0x19FF97, "ingotIron"),
        SIGNAL(0xFE3E62, "dustRedstone"),
        ENERGY(0xD8BD4C, "nuggetGold"),
        FLUID(0x66CBFF, Items.WATER_BUCKET),
        ITEM(0xCCEEFF, Items.CLAY_BALL)
    }

    enum class Direction(val oreDictPostFix: String) {
        BASIC(""), INPUT("In"), OUTPUT("Out")
    }

    override val itemColorFunction: ((ItemStack, Int) -> Int)?
        get() = { stack, tintIndex -> if (tintIndex == 2) Color(getDiskType(stack).color).aLittleDarker().pulseColor().rgb else -1 }

    fun Color.aLittleDarker(amount: Int = 12)
            = Color(Math.max(red - amount, 0),
                Math.max(green - amount, 0),
                Math.max(blue - amount, 0),
                alpha)
}
