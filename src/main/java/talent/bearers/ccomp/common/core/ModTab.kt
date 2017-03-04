package talent.bearers.ccomp.common.core

import com.teamwizardry.librarianlib.common.base.ModCreativeTab
import net.minecraft.item.ItemStack
import talent.bearers.ccomp.common.blocks.ModBlocks

/**
 * @author WireSegal
 * Created at 9:22 PM on 3/2/17.
 */
object ModTab : ModCreativeTab() {

    override val iconStack by lazy {
        ItemStack(ModBlocks.SIGNAL_NODE)
    }

    init {
        registerDefaultTab()
    }
}
