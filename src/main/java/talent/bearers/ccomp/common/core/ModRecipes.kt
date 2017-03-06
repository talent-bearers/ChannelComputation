package talent.bearers.ccomp.common.core

import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.OreDictionary
import net.minecraftforge.oredict.ShapedOreRecipe
import net.minecraftforge.oredict.ShapelessOreRecipe
import talent.bearers.ccomp.common.blocks.ModBlocks
import talent.bearers.ccomp.common.items.ItemEncodedDisk
import talent.bearers.ccomp.common.items.ModItems

/**
 * @author WireSegal
 * Created at 2:52 PM on 3/5/17.
 */
object ModRecipes {
    init {
        for (i in ItemEncodedDisk.VARIANTS.indices) {
            val type = ItemEncodedDisk.getDiskType(i)
            val direction = ItemEncodedDisk.getDiskDirection(i)
            if (type == ItemEncodedDisk.Type.BASIC)
                shapedRecipe(ItemStack(ModItems.DISK, 8, i),
                        " I ",
                        "IMI",
                        " I ",
                        'I', "ingotComputationAlloy",
                        'M', type.material)
            else when (direction) {
                ItemEncodedDisk.Direction.BASIC -> shapelessRecipe(ItemStack(ModItems.DISK, 1, i),
                        "diskBasic", type.material)

                ItemEncodedDisk.Direction.INPUT -> shapelessRecipe(ItemStack(ModItems.DISK, 1, i),
                        "disk" + type.name.toLowerCase().capitalize(), "gemLapis")

                ItemEncodedDisk.Direction.OUTPUT -> shapelessRecipe(ItemStack(ModItems.DISK, 1, i),
                        "disk" + type.name.toLowerCase().capitalize(), "dustRedstone")
            }
        }

        shapelessRecipe(ItemStack(ModItems.METAL, 4, 1),
                if (OreDictionary.doesOreNameExist("dustIron")) "dustIron" else "ingotIron",
                "dustRedstone", "nuggetGold", "gunpowder", ItemStack(Items.COAL, 1, 1))

        furnaceRecipe(ItemStack(ModItems.METAL, 1, 0), ItemStack(ModItems.METAL, 1, 1), 0.1f)

        shapedRecipe(ModBlocks.SIGNAL_NODE,
                "IO",
                "SC",
                'C', "ingotComputationAlloy",
                'I', "diskSignalIn",
                'O', "diskSignalOut",
                'S', "diskSignal")

        shapedRecipe(ModBlocks.ENERGY_NODE,
                "IO",
                "SC",
                'C', "ingotComputationAlloy",
                'I', "diskEnergyIn",
                'O', "diskEnergyOut",
                'S', "diskSignalIn")

        shapedRecipe(ModBlocks.FLUID_NODE,
                "IO",
                "SC",
                'C', "ingotComputationAlloy",
                'I', "diskFluidIn",
                'O', "diskFluidOut",
                'S', "diskSignalIn")

        shapedRecipe(ModBlocks.ITEM_NODE,
                "IO",
                "SC",
                'C', "ingotComputationAlloy",
                'I', "diskItemIn",
                'O', "diskItemOut",
                'S', "diskSignalIn")

        shapedRecipe(ModBlocks.ENERGY_LANCER,
                "DI",
                "SC",
                'I', "ingotIron",
                'C', "ingotComputationAlloy",
                'D', "diskEnergyIn",
                'S', "diskSignalIn")

        shapedRecipe(ModBlocks.FLUID_LANCER,
                "DI",
                "SC",
                'I', "ingotIron",
                'C', "ingotComputationAlloy",
                'D', "diskFluidIn",
                'S', "diskSignalIn")

        shapedRecipe(ModBlocks.ITEM_LANCER,
                "DI",
                "SC",
                'I', "ingotIron",
                'C', "ingotComputationAlloy",
                'D', "diskItemIn",
                'S', "diskSignalIn")


        shapedRecipe(ModBlocks.FLUID_INTERACTION,
                "IDO",
                " C ",
                "MSM",
                'C', "ingotComputationAlloy",
                'I', "diskFluidIn",
                'O', "diskFluidOut",
                'M', "diskFluid",
                'S', "diskSignalIn",
                'D', Blocks.DROPPER)

        shapedRecipe(ModBlocks.ITEM_INTERACTION,
                "IDO",
                " C ",
                "MSM",
                'C', "ingotComputationAlloy",
                'I', "diskItemIn",
                'O', "diskItemOut",
                'M', "diskItem",
                'S', "diskSignalIn",
                'D', Blocks.DROPPER)

        shapedRecipe(ItemStack(ModBlocks.CABLE, 16),
                " D ",
                "DCD",
                " D ",
                'D', "diskBasic",
                'C', "ingotComputationAlloy")

        shapedRecipe(ModItems.PULSAR,
                " D ",
                " CD",
                "C  ",
                'D', "diskBasic",
                'C', "ingotComputationAlloy")
    }

    fun furnaceRecipe(stack: Any, input: Any, xp: Float) {
        val inp = when (input) {
            is Item -> ItemStack(input)
            is Block -> ItemStack(input)
            is ItemStack -> input
            else -> throw IllegalArgumentException()
        }
        val out = when (stack) {
            is Item -> ItemStack(stack)
            is Block -> ItemStack(stack)
            is ItemStack -> stack
            else -> throw IllegalArgumentException()
        }
        GameRegistry.addSmelting(inp, out, xp)
    }

    fun shapedRecipe(stack: Item, vararg inputs: Any) = shapedRecipe(ItemStack(stack), *inputs)
    fun shapedRecipe(stack: Block, vararg inputs: Any) = shapedRecipe(ItemStack(stack), *inputs)
    fun shapedRecipe(stack: ItemStack, vararg inputs: Any) = GameRegistry.addRecipe(ShapedOreRecipe(stack, *inputs))

    fun shapelessRecipe(stack: Item, vararg inputs: Any) = shapelessRecipe(ItemStack(stack), *inputs)
    fun shapelessRecipe(stack: Block, vararg inputs: Any) = shapelessRecipe(ItemStack(stack), *inputs)
    fun shapelessRecipe(stack: ItemStack, vararg inputs: Any) = GameRegistry.addRecipe(ShapelessOreRecipe(stack, *inputs))


}
