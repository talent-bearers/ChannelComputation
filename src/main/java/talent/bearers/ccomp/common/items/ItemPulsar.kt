package talent.bearers.ccomp.common.items

import com.google.common.collect.Lists
import com.teamwizardry.librarianlib.common.base.item.ItemMod
import com.teamwizardry.librarianlib.common.util.sendMessage
import com.teamwizardry.librarianlib.common.util.sendSpamlessMessage
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import talent.bearers.ccomp.api.pathing.ICrawlableCable
import talent.bearers.ccomp.api.pathing.IDataNode
import talent.bearers.ccomp.api.pathing.PathCrawler

/**
 * @author WireSegal
 * Created at 10:47 AM on 3/2/17.
 */
class ItemPulsar : ItemMod("ghost_pulsar") {
    init {
        setMaxStackSize(1)
    }

    override fun onItemUse(stack: ItemStack, playerIn: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {
        if (!worldIn.isRemote) {
            playerIn.sendSpamlessMessage(PathCrawler.crawlPath(worldIn, pos).toString(), 1111) // DEBUG
        }
        return EnumActionResult.SUCCESS
    }
}
