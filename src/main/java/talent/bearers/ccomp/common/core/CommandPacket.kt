package talent.bearers.ccomp.common.core

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import talent.bearers.ccomp.MODID
import talent.bearers.ccomp.api.pathing.IDataNode
import talent.bearers.ccomp.api.pathing.PathCrawler

/**
 * @author WireSegal
 * Created at 3:01 PM on 3/3/17.
 */
object CommandPacket : CommandBase() {
    val ACTIONS = listOf("pull", "read")
    val SWAPS = listOf("id", "pos")

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.size < 7) throw WrongUsageException(getCommandUsage())

        val pos = parseBlockPos(sender, args, 0, false)
        val strength = parseInt(args[3])
        val type = args[4]
        val action = args[5]
        if (action !in ACTIONS) throw WrongUsageException(getCommandUsage())

        val swap = args[6]

        val nodes = PathCrawler.crawlPath(sender.entityWorld, pos)
        if (nodes.isEmpty())
            throw CommandException("$MODID.command.request.nonode")

        val node = if (swap == "id") {
            if (args.size < 8) throw WrongUsageException(getCommandUsage("id"))
            val id = parseInt(args[7], 0)
            if (nodes.size <= id)
                throw CommandException("$MODID.command.request.bigid")
            nodes[id]
        } else if (swap == "pos") {
            if (args.size < 10) throw WrongUsageException(getCommandUsage("pos"))
            val target = parseBlockPos(sender, args, 7, false)
            nodes.firstOrNull { target == it } ?: throw throw CommandException("$MODID.command.request.emptypos")
        } else throw WrongUsageException(getCommandUsage())

        val state = sender.entityWorld.getBlockState(node)
        val block = state.block as IDataNode
        val packet = if (action == "pull")
            block.requestPullPacket(type, strength, node, sender.entityWorld)
        else
            block.requestReadPacket(type, strength, node, sender.entityWorld)

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
            5 -> ACTIONS
            6 -> SWAPS
            7, 8, 9 -> if (args[6] == "pos") getTabCompletionCoordinate(args, 6, pos) else emptyList()
            else -> emptyList()
        }
    }

    override fun getCommandName() = "request-packet"
    fun getCommandUsage(postFix: String? = null) = "$MODID.command.request.usage${if (postFix != null) "." + postFix else ""}"
    override fun getCommandUsage(sender: ICommandSender?) = getCommandUsage()
}
