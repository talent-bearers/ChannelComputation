package talent.bearers.ccomp.api.pathing;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import talent.bearers.ccomp.api.packet.IPacket;

/**
 * @author WireSegal
 *         Created at 8:12 PM on 3/1/17.
 */
public interface IDataNode extends ICableConnectible {
    /**
     * What facing the data node is attached to. This is for path crawler connections.
     * This is provided mostly for convenience: This can be ignored if isSideAvailable is overridden.
     */
    @Nullable
    EnumFacing connectionPoint(@NotNull BlockPos pos, @NotNull IBlockAccess world);

    /**
     * The default implementation for a data node's connectibility.
     */
    @Override
    default boolean isSideAvailable(@NotNull EnumFacing side, @NotNull BlockPos pos, @NotNull IBlockAccess world) {
        return side == connectionPoint(pos, world);
    }

    /**
     * Request a ghost packet containing the contents from a pull packet.
     */
    @Nullable
    IPacket requestReadPacket(@NotNull String packetType, int strength, @NotNull BlockPos pos, @NotNull WorldServer world);

    /**
     * Request a packet containing the contents of the target.
     * Strength implies how much quantity should be extracted from the target.
     * The meaning of strength depends on the type.
     *
     * If the target node is not compatible with the type of request sent
     * or has nothing to be pulled, it should return null.
     */
    @Nullable
    IPacket requestPullPacket(@NotNull String packetType, int strength, @NotNull BlockPos pos, @NotNull WorldServer world);

    /**
     * Push a packet into the target. The remainder from the packet should be returned
     * as a new packet, with the same general behaviors as the old one.
     * Ghost packets are never passed to this method unless they're signal packets.
     */
    @Nullable
    IPacket pushPacket(@NotNull IPacket packet, @NotNull BlockPos pos, @NotNull WorldServer world);
}
