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
                "PSP",
                'C', "ingotComputationAlloy",
                'I', "diskFluidIn",
                'O', "diskFluidOut",
                'S', "diskSignalIn",
                'P', Blocks.PISTON,
                'D', Blocks.DROPPER)

        shapedRecipe(ModBlocks.ITEM_INTERACTION,
                "IDO",
                " C ",
                "PSP",
                'C', "ingotComputationAlloy",
                'I', "diskItemIn",
                'O', "diskItemOut",
                'S', "diskSignalIn",
                'P', Blocks.PISTON,
                'D', Blocks.DROPPER)

        shapedRecipe(ModBlocks.ITEM_EXISTENCE,
                "EID",
                "ICO",
                "COC",
                'C', "ingotComputationAlloy",
                'I', "diskItemIn",
                'O', "diskItemOut",
                'E', "enderpearl",
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

        shapedRecipe(ModBlocks.TANK,
                "CIC",
                "GSG",
                "COC",
                'C', "ingotComputationAlloy",
                'I', "diskFluidIn",
                'G', "blockGlass",
                'S', "diskFluid",
                'O', "diskFluidOut")

        shapedRecipe(ModBlocks.CUBE,
                "CIC",
                "GSG",
                "COC",
                'C', "ingotComputationAlloy",
                'I', "diskEnergyIn",
                'G', "ingotGold",
                'S', "diskEnergy",
                'O', "diskEnergyOut")

        shapedRecipe(ModBlocks.CRAFTER,
                "III",
                "SCS",
                " O ",
                'C', "ingotComputationAlloy",
                'I', "diskItemIn",
                'S', "diskItem",
                'O', "diskItemOut")

        shapedRecipe(ItemStack(ModItems.PLACEHOLDER, 32),
                " S ",
                "SCS",
                " S ",
                'S', "stickWood",
                'C', "ingotComputationAlloy")

        shapedRecipe(ModBlocks.METAL_BLOCK,
                "III", "III", "III",
                'I', "ingotComputationAlloy")

        shapedRecipe(ItemStack(ModItems.METAL, 9),
                "B", 'B', "blockComputationAlloy")
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
