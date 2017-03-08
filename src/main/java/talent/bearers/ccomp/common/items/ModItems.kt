package talent.bearers.ccomp.common.items

import com.teamwizardry.librarianlib.common.base.item.ItemMod
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary
import talent.bearers.ccomp.common.core.ItemCC

/**
 * @author WireSegal
 * Created at 10:47 AM on 3/2/17.
 */
object ModItems {
    val PULSAR = ItemPulsar()
    val DISK = ItemEncodedDisk()
    val METAL = ItemCC("metal", "metal", "metal_blend")
    val PLACEHOLDER = ItemCC("placeholder").apply { containerItem = this }

    init {
        OreDictionary.registerOre("ingotComputationAlloy", ItemStack(METAL, 1, 0))
        OreDictionary.registerOre("dustComputationAlloy", ItemStack(METAL, 1, 1))
    }
}
