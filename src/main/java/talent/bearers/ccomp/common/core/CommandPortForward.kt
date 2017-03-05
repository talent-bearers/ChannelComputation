package talent.bearers.ccomp.common.core

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer
import talent.bearers.ccomp.MODID
import talent.bearers.ccomp.api.pathing.IDataNode
import talent.bearers.ccomp.api.pathing.PathCrawler

/**
 * @author WireSegal
 * Created at 3:01 PM on 3/3/17.
 */
object CommandPortForward : CommandBase() {
    val FORWARDING_TYPES = mutableListOf("signal")

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.size < 7) throw WrongUsageException(getCommandUsage())

        val world = sender.entityWorld as? WorldServer ?: throw CommandException("this ought to be impossible")

        val pos = parseBlockPos(sender, args, 0, false)
        val strength = parseInt(args[3])
        val type = args[4]

        val nodes = PathCrawler.crawlPath(sender.entityWorld, pos)
        if (nodes.isEmpty())
            throw CommandException("$MODID.command.request.nonode")

        val idFrom = parseInt(args[5], 0)
        if (nodes.size <= idFrom)
            throw CommandException("$MODID.command.request.bigid")
        val nodeFrom = nodes[idFrom]

        val idTo = parseInt(args[6], 0)
        if (nodes.size <= idTo)
            throw CommandException("$MODID.command.request.bigid")
        val nodeTo = nodes[idTo]

        val state = sender.entityWorld.getBlockState(nodeFrom)
        val block = state.block as IDataNode
        val packet = block.requestPullPacket(type, strength, nodeFrom, world)

        if (packet == null)
            notifyCommandListener(sender, this, "$MODID.command.request.nopacket")
        else {
            notifyCommandListener(sender, this, "$MODID.command.request.success.type", packet.type)
            notifyCommandListener(sender, this, "$MODID.command.request.success.ghost", packet.isGhost)
            notifyCommandListener(sender, this, "$MODID.command.request.success.size", packet.size)
            notifyCommandListener(sender, this, "$MODID.command.request.success.data", packet.data)

            if (!packet.isGhost || packet.type in FORWARDING_TYPES) {
                val toState = sender.entityWorld.getBlockState(nodeTo)
                val toBlock = toState.block as IDataNode
                val result = toBlock.pushPacket(packet, nodeTo, world)

                if (result == null)
                    notifyCommandListener(sender, this, "$MODID.command.request.nopacket")
                else {
                    notifyCommandListener(sender, this, "$MODID.command.channel.success.size", result.size)
                    notifyCommandListener(sender, this, "$MODID.command.channel.success.data", result.data)
                }
            }
        }
    }

    override fun getTabCompletionOptions(server: MinecraftServer, sender: ICommandSender, args: Array<out String>, pos: BlockPos?): List<String> {
        return when (args.size) {
            1, 2, 3 -> getTabCompletionCoordinate(args, 0, pos)
            5 -> getListOfStringsMatchingLastWord(args, CommandPacket.TYPES)
            else -> emptyList()
        }
    }

    override fun getCommandName() = "channel-packet"
    fun getCommandUsage() = "$MODID.command.channel.usage"
    override fun getCommandUsage(sender: ICommandSender?) = getCommandUsage()
}
