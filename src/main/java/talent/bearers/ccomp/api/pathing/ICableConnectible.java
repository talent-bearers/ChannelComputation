package talent.bearers.ccomp.api.pathing;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

/**
 * @author WireSegal
 *         Created at 9:16 PM on 3/1/17.
 */
public interface ICableConnectible {
    /**
     * Whether the given side is a possible connection.
     * Used for connection tests in implementations of {@link ICrawlableCable#connectedOnSide(EnumFacing, BlockPos, IBlockAccess)}.
     */
    boolean isSideAvailable(@NotNull EnumFacing side, @NotNull BlockPos pos, @NotNull IBlockAccess world);
}
