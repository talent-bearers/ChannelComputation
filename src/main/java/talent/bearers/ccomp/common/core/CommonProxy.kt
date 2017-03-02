package talent.bearers.ccomp.common.core

import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import talent.bearers.ccomp.common.blocks.ModBlocks

/**
 * @author WireSegal
 * Created at 4:27 PM on 3/1/17.
 */
open class CommonProxy {
    open fun pre(e: FMLPreInitializationEvent) {
        ModBlocks
    }

    open fun init(e: FMLInitializationEvent) {

    }

    open fun post(e: FMLPostInitializationEvent) {

    }
}
