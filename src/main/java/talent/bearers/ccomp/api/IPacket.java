package talent.bearers.ccomp.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @author WireSegal
 *         Created at 8:13 PM on 3/1/17.
 */
public interface IPacket {
    /**
     * The type of the packet.
     * Default types are:
     *
     * signal
     * item
     * fluid
     * energy
     */
    @NotNull String getType();

    /**
     * Whether the type actually contains transferable data.
     * If a packet is a ghost, pushing it will only remove it, not do anything to the target.
     */
    boolean isGhost();

    /**
     * The data contained within the packet. Used by nodes to handle the data contained within.
     */
    @NotNull NBTTagCompound getData();

    /**
     * The size of a packet. Used for comparative operations.
     */
    int getSize();

    /**
     * If a packet dies while in a channel, it'll get dumped into the spell programmer.
     * This method allows a packet to handle dumping extra contents.
     *
     * This method is never called on ghost packets.
     *
     * Contents need not be transferred, as in the case of signal packets.
     */
    void dumpData(@NotNull IFluidHandler fluids, @NotNull IItemHandler items, @NotNull IEnergyStorage energy, @NotNull TileEntity general);
}
