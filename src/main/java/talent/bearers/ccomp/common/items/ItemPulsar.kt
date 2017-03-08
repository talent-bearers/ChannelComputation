package talent.bearers.ccomp.common.items

import com.google.common.collect.Lists
import com.teamwizardry.librarianlib.client.structure.InWorldRender.pos
import com.teamwizardry.librarianlib.client.util.TooltipHelper
import com.teamwizardry.librarianlib.common.base.item.ItemMod
import com.teamwizardry.librarianlib.common.util.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagInt
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import talent.bearers.ccomp.MODID
import talent.bearers.ccomp.api.misc.IPulsarUsable
import talent.bearers.ccomp.api.pathing.ICableConnectible
import talent.bearers.ccomp.api.pathing.ICrawlableCable
import talent.bearers.ccomp.api.pathing.IDataNode
import talent.bearers.ccomp.api.pathing.PathCrawler
import talent.bearers.ccomp.common.core.ItemCC

/**
 * @author WireSegal
 * Created at 10:47 AM on 3/2/17.
 */
class ItemPulsar : ItemCC("ghost_pulsar") {
    init {
        setMaxStackSize(1)
    }

    companion object {
        val CHANNEL_ID = "cable connection".hashCode()
    }

    val TAG_POS = "pos"

    override fun onItemUse(stack: ItemStack, playerIn: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {
        val block = worldIn.getBlockState(pos)
        val pick = block.block.getPickBlock(block, RayTraceResult(RayTraceResult.Type.BLOCK, vec(hitX, hitY, hitZ), facing, pos), worldIn, pos, playerIn)

        if (playerIn.isSneaking &&
                (block.block is ICableConnectible ||
                        (block.block is IPulsarUsable &&
                                (block.block as IPulsarUsable).shouldBreak(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ))) ) {
            if (!worldIn.isRemote) {
                if (block.block !is IPulsarUsable || !(block.block as IPulsarUsable).customDropImplementation(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ))
                    block.block.dropBlockAsItem(worldIn, pos, block, 0)
                worldIn.setBlockState(pos, Blocks.AIR.defaultState)
                worldIn.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1f, 1f)
            }
            return EnumActionResult.SUCCESS
        }
        if (block.block is ICableConnectible) {
            if (block.block is ICrawlableCable) {
                if (!worldIn.isRemote) {
                    ItemNBTHelper.setList(stack, TAG_POS, NBTTagList().apply {
                        appendTag(NBTTagInt(pos.x))
                        appendTag(NBTTagInt(pos.y))
                        appendTag(NBTTagInt(pos.z))
                    })
                    playerIn.sendSpamlessMessage(TextComponentTranslation("$MODID.misc.cableselected", pick.textComponent), CHANNEL_ID)
                }
                return EnumActionResult.SUCCESS
            } else {
                if (block.block !is IDataNode) {
                    if (!worldIn.isRemote)
                        playerIn.sendSpamlessMessage(TextComponentTranslation("$MODID.misc.notanode", pick.textComponent).setStyle(Style().setColor(TextFormatting.RED)), CHANNEL_ID)
                    return EnumActionResult.FAIL
                } else {
                    if (!worldIn.isRemote) {
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
                    return EnumActionResult.SUCCESS
                }
            }

        } else if (block.block is IPulsarUsable)
            return (block.block as IPulsarUsable).onPulsarUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ)
        return EnumActionResult.PASS
    }
}
