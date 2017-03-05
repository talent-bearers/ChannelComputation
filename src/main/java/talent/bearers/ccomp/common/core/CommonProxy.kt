package talent.bearers.ccomp.common.core

import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import talent.bearers.ccomp.ChannelComputation
import talent.bearers.ccomp.common.blocks.ModBlocks
import talent.bearers.ccomp.common.items.ModItems

/**
 * @author WireSegal
 * Created at 4:27 PM on 3/1/17.
 */
open class CommonProxy {
    open fun pre(e: FMLPreInitializationEvent) {
        ModTab
        ModBlocks
        ModItems
    }

    open fun init(e: FMLInitializationEvent) {
        ModRecipes
    }

    open fun post(e: FMLPostInitializationEvent) {

    }

    open fun serverStart(e: FMLServerStartingEvent) {
        e.registerServerCommand(CommandPacket)
        e.registerServerCommand(CommandPortForward)
    }
}
