package talent.bearers.ccomp.api.packet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author WireSegal
 *         Created at 4:09 PM on 3/2/17.
 */
public class Channel {
    private final Queue<IPacket> channel = new ArrayDeque<>();

    public void push(@NotNull IPacket packet) {
        channel.add(packet);
    }

    @Nullable
    public IPacket peek() {
        IPacket packet = channel.peek();
        return packet == null ? null : new GhostWrapper(packet);
    }

    @Nullable
    public IPacket take() {
        return channel.poll();
    }

    private static class GhostWrapper implements IPacket {
        private final IPacket wrapped;

        private GhostWrapper(IPacket wrapped) {
            this.wrapped = wrapped;
        }

        @NotNull
        @Override
        public String getType() {
            return wrapped.getType();
        }

        @Override
        public boolean isGhost() {
            return true;
        }

        @NotNull
        @Override
        public NBTTagCompound getData() {
            return wrapped.getData();
        }

        @Override
        public int getSize() {
            return wrapped.getSize();
        }

        @Override
        public void dumpData(@NotNull IFluidHandler fluids, @NotNull IItemHandler items, @NotNull IEnergyStorage energy, @NotNull TileEntity general) {
            wrapped.dumpData(fluids, items, energy, general);
        }
    }
}
