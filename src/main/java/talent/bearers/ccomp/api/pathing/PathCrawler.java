package talent.bearers.ccomp.api.pathing;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import talent.bearers.ccomp.api.pathing.ICrawlableCable;
import talent.bearers.ccomp.api.pathing.IDataNode;

import java.util.List;

/**
 * @author WireSegal
 *         Created at 10:31 AM on 3/2/17.
 */
public final class PathCrawler {
    @NotNull
    public static List<BlockPos> crawlPath(@NotNull IBlockAccess world, @NotNull BlockPos origin) {
        List<BlockPos> found = Lists.newArrayList();
        Block block = world.getBlockState(origin).getBlock();
        if (!(block instanceof ICrawlableCable)) {
            if (block instanceof IDataNode)
                found.add(origin);
            return found;
        }
        crawlPath(world, block, origin, null, found, Lists.newArrayList(), 0);
        found.sort((pos1, pos2) -> (int) (pos1.distanceSq(origin) - pos2.distanceSq(origin)));
        return found;
    }

    private static void crawlPath(@NotNull IBlockAccess world, @NotNull Block block, @NotNull BlockPos point, @Nullable EnumFacing from, @NotNull List<BlockPos> found, @NotNull List<BlockPos> crawled, int distance) {
        if (crawled.contains(point) || distance >= 32) return;
        crawled.add(point);
        if (block instanceof IDataNode) {
            found.add(point);
            return;
        } else if (!(block instanceof ICrawlableCable)) return;

        for (EnumFacing facing : EnumFacing.VALUES) if (from == null || facing != from.getOpposite()) {
            if (((ICrawlableCable) block).connectedOnSide(facing, point, world)) {
                BlockPos shift = point.offset(facing);
                IBlockState state = world.getBlockState(shift);
                crawlPath(world, state.getBlock(), shift, facing, found, crawled, distance + 1);
            }
        }
    }
}
