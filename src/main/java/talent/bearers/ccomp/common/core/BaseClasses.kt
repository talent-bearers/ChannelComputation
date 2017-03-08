package talent.bearers.ccomp.common.core

import com.teamwizardry.librarianlib.LibrarianLib
import com.teamwizardry.librarianlib.client.util.TooltipHelper
import com.teamwizardry.librarianlib.common.base.block.BlockMod
import com.teamwizardry.librarianlib.common.base.block.BlockModContainer
import com.teamwizardry.librarianlib.common.base.item.ItemMod
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
 * @author WireSegal
 * Created at 5:14 PM on 3/7/17.
 */
abstract class ContainerBlockCC(name: String, materialIn: Material, vararg variants: String) : BlockModContainer(name, materialIn, *variants) {
    override fun addInformation(stack: ItemStack, player: EntityPlayer, tooltip: MutableList<String>, advanced: Boolean) = addInfo(stack, tooltip)
}

open class BlockCC(name: String, materialIn: Material, vararg variants: String) : BlockMod(name, materialIn, *variants) {
    override fun addInformation(stack: ItemStack, player: EntityPlayer, tooltip: MutableList<String>, advanced: Boolean) = addInfo(stack, tooltip)
}

open class ItemCC(name: String, vararg variants: String) : ItemMod(name, *variants) {
    override fun addInformation(stack: ItemStack, player: EntityPlayer, tooltip: MutableList<String>, advanced: Boolean) = addInfo(stack, tooltip)
}

private fun addInfo(stack: ItemStack, tooltip: MutableList<String>) {
    val desc = stack.unlocalizedName + ".desc"
    if (LibrarianLib.PROXY.canTranslate(desc))
        TooltipHelper.tooltipIfShift(tooltip) {
            TooltipHelper.addToTooltip(tooltip, desc)
        }
}
