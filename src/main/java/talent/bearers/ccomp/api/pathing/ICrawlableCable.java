package talent.bearers.ccomp.api.pathing;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import talent.bearers.ccomp.api.pathing.ICableConnectible;

/**
 * @author WireSegal
 *         Created at 8:12 PM on 3/1/17.
 */
public interface ICrawlableCable extends ICableConnectible {
    /**
     * Whether the cable block is connected on the given side. Used for pathcrawling.
     * If a block that's not cable-connectible is returned, crawlers will crash.
     */
    default boolean connectedOnSide(@NotNull EnumFacing side, @NotNull BlockPos pos, @NotNull IBlockAccess world) {
        BlockPos offset = pos.offset(side);
        IBlockState state = world.getBlockState(offset);
        return isSideAvailable(side, pos, world)
                && state.getBlock() instanceof ICableConnectible
                && ((ICableConnectible) state.getBlock()).isSideAvailable(side.getOpposite(), offset, world);
    }
}
