package talent.bearers.ccomp.client.core

import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import talent.bearers.ccomp.common.blocks.BlockFluidColumn
import talent.bearers.ccomp.common.core.CommonProxy

/**
 * @author WireSegal
 * Created at 4:29 PM on 3/1/17.
 */
class ClientProxy : CommonProxy() {
    override fun pre(e: FMLPreInitializationEvent) {
        super.pre(e)
        PulsarHUDHandler
    }

    override fun init(e: FMLInitializationEvent) {
        super.init(e)
        ClientRegistry.bindTileEntitySpecialRenderer(BlockFluidColumn.TileFluidColumn::class.java, ColumnRenderer)
    }

    override fun post(e: FMLPostInitializationEvent) {
        super.post(e)
    }
}
