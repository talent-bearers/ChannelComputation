package talent.bearers.ccomp.common.blocks

import com.teamwizardry.librarianlib.common.base.block.BlockMod
import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraftforge.oredict.OreDictionary

/**
 * @author WireSegal
 * Created at 9:50 PM on 3/1/17.
 */
object ModBlocks {
    val CABLE = BlockCable()
    val SIGNAL_NODE = BlockSignalNode()
    val ITEM_NODE = BlockItemNode()
    val FLUID_NODE = BlockFluidNode()
    val ENERGY_NODE = BlockEnergyNode()
    val ITEM_LANCER = BlockItemLancer()
    val FLUID_LANCER = BlockFluidLancer()
    val ENERGY_LANCER = BlockEnergyLancer()
    val ITEM_INTERACTION = BlockItemInteraction()
    val FLUID_INTERACTION = BlockFluidInteraction()
    val METAL_BLOCK: Block = BlockMod("metal_block", Material.IRON).setHardness(5.0F).setResistance(10.0F).apply { OreDictionary.registerOre("blockComputationAlloy", this) }
    val TANK = BlockFluidColumn()
    val CUBE = BlockEnergyCube()
}
