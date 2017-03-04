package talent.bearers.ccomp.common.items

import com.google.common.collect.Lists
import com.teamwizardry.librarianlib.common.base.item.ItemMod
import com.teamwizardry.librarianlib.common.util.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagInt
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import talent.bearers.ccomp.MODID
import talent.bearers.ccomp.api.pathing.ICableConnectible
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

    val TAG_POS = "pos"

    val CHANNEL_ID = "cable connection".hashCode()

    override fun onItemUse(stack: ItemStack, playerIn: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {

        if (!worldIn.isRemote) {
            val block = worldIn.getBlockState(pos)
            val pick = block.block.getPickBlock(block, RayTraceResult(RayTraceResult.Type.BLOCK, vec(hitX, hitY, hitZ), facing, pos), worldIn, pos, playerIn)
            if (block.block is ICableConnectible) {
                if (block.block is ICrawlableCable) {
                    ItemNBTHelper.setList(stack, TAG_POS, NBTTagList().apply {
                        appendTag(NBTTagInt(pos.x))
                        appendTag(NBTTagInt(pos.y))
                        appendTag(NBTTagInt(pos.z))
                    })
                    playerIn.sendSpamlessMessage(TextComponentTranslation("$MODID.misc.cableselected", pick.textComponent), CHANNEL_ID)
                } else {
                    if (block.block !is IDataNode)
                        playerIn.sendSpamlessMessage(TextComponentTranslation("$MODID.misc.notanode", pick.textComponent).setStyle(Style().setColor(TextFormatting.RED)), CHANNEL_ID)
                    else {
                        val cable = ItemNBTHelper.getList(stack, TAG_POS, NBTTypes.INT, false)?.run {
                            BlockPos(getIntAt(0), getIntAt(1), getIntAt(2))
                        } ?: return EnumActionResult.SUCCESS
                        val nodes = PathCrawler.crawlPath(worldIn, cable)
                        val index = nodes.indexOf(pos)
                        if (index == -1)
                            playerIn.sendSpamlessMessage(TextComponentTranslation("$MODID.misc.notconnected", pick.textComponent).setStyle(Style().setColor(TextFormatting.RED)), CHANNEL_ID)
                        else
                            playerIn.sendSpamlessMessage(TextComponentTranslation("$MODID.misc.foundconnection", pick.textComponent, index), CHANNEL_ID)
                    }
                }
            } else
                playerIn.sendSpamlessMessage(TextComponentTranslation("$MODID.misc.notonnetwork", pick.textComponent).setStyle(Style().setColor(TextFormatting.RED)), CHANNEL_ID)
        }
        return EnumActionResult.SUCCESS
    }
}
