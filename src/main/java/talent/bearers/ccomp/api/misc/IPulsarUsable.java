package talent.bearers.ccomp.api.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * @author WireSegal
 *         Created at 9:44 PM on 3/4/17.
 */
public interface IPulsarUsable {
    @NotNull
    EnumActionResult onPulsarUse(@NotNull ItemStack stack, @NotNull EntityPlayer playerIn, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ);

    default boolean shouldBreak(@NotNull ItemStack stack, @NotNull EntityPlayer playerIn, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        return true;
    }
}
