package talent.bearers.ccomp.common.core

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandBase.getListOfStringsMatchingLastWord
import net.minecraft.command.CommandBase.getTabCompletionCoordinate
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer
import talent.bearers.ccomp.MODID
import talent.bearers.ccomp.api.pathing.IDataNode
import talent.bearers.ccomp.api.pathing.PathCrawler
import talent.bearers.ccomp.common.core.CommandPacket.ACTIONS

/**
 * @author WireSegal
 * Created at 3:01 PM on 3/3/17.
 */
object CommandPacket : CommandBase() {
    val ACTIONS = listOf("pull", "read")
    val TYPES = mutableListOf("signal", "item", "fluid", "energy")

    override fun getRequiredPermissionLevel() = 2

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.size < 7) throw WrongUsageException(getCommandUsage())

        val world = sender.entityWorld as? WorldServer ?: throw CommandException("this ought to be impossible")

        val pos = parseBlockPos(sender, args, 0, false)
        val strength = parseInt(args[3])
        val type = args[4]
        val action = args[5]
        if (action !in ACTIONS) throw WrongUsageException(getCommandUsage())


        val nodes = PathCrawler.crawlPath(sender.entityWorld, pos)
        if (nodes.isEmpty())
            throw CommandException("$MODID.command.request.nonode")

        val id = parseInt(args[6], 0)
        if (nodes.size <= id)
            throw CommandException("$MODID.command.request.bigid")
        val node = nodes[id]

        val state = sender.entityWorld.getBlockState(node)
        val block = state.block as IDataNode
        val packet = if (action == "pull")
            block.requestPullPacket(type, strength, node, world)
        else
            block.requestReadPacket(type, strength, node, world)

        if (packet == null)
            notifyCommandListener(sender, this, "$MODID.command.request.nopacket")
        else {
            notifyCommandListener(sender, this, "$MODID.command.request.success.type", packet.type)
            notifyCommandListener(sender, this, "$MODID.command.request.success.ghost", packet.isGhost)
            notifyCommandListener(sender, this, "$MODID.command.request.success.size", packet.size)
            notifyCommandListener(sender, this, "$MODID.command.request.success.data", packet.data)
        }
    }

    override fun getTabCompletionOptions(server: MinecraftServer, sender: ICommandSender, args: Array<out String>, pos: BlockPos?): List<String> {
        return when (args.size) {
            1, 2, 3 -> getTabCompletionCoordinate(args, 0, pos)
            5 -> getListOfStringsMatchingLastWord(args, TYPES)
            6 -> getListOfStringsMatchingLastWord(args, ACTIONS)
            else -> emptyList()
        }
    }

    override fun getCommandName() = "request-packet"
    fun getCommandUsage() = "$MODID.command.request.usage"
    override fun getCommandUsage(sender: ICommandSender?) = getCommandUsage()
}
