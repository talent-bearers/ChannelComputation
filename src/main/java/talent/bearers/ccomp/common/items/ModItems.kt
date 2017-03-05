package talent.bearers.ccomp.common.items

import com.teamwizardry.librarianlib.common.base.item.ItemMod
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary

/**
 * @author WireSegal
 * Created at 10:47 AM on 3/2/17.
 */
object ModItems {
    val PULSAR = ItemPulsar()
    val DISK = ItemEncodedDisk()
    val METAL = ItemMod("metal", "metal", "metal_blend")

    init {
        OreDictionary.registerOre("ingotComputationAlloy", ItemStack(METAL, 1, 0))
        OreDictionary.registerOre("dustComputationAlloy", ItemStack(METAL, 1, 1))
    }
}
