package talent.bearers.ccomp

import com.teamwizardry.librarianlib.LibrarianLib
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import talent.bearers.ccomp.common.core.CommonProxy

/**
 * @author WireSegal
 * Created at 4:22 PM on 3/1/17.
 */
const val MODID = "ccomp"
const val MODNAME = "Channel Computation"
const val VERSION = "1.0"
const val DEPENDENCIES = "required-after:librarianlib"
const val COMMON_PROXY = "talent.bearers.ccomp.common.core.CommonProxy"
const val CLIENT_PROXY = "talent.bearers.ccomp.client.core.ClientProxy"

@Mod(modid = MODID, name = MODNAME, version = VERSION, dependencies = DEPENDENCIES, modLanguageAdapter = LibrarianLib.ADAPTER)
object ChannelComputation {
    @SidedProxy(serverSide = COMMON_PROXY, clientSide = CLIENT_PROXY)
    lateinit var proxy: CommonProxy

    @Mod.EventHandler fun pre(e: FMLPreInitializationEvent) = proxy.pre(e)
    @Mod.EventHandler fun init(e: FMLInitializationEvent) = proxy.init(e)
    @Mod.EventHandler fun post(e: FMLPostInitializationEvent) = proxy.post(e)
}