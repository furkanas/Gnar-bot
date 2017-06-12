package xyz.gnarbot.gnar.commands.executors.general

import net.dv8tion.jda.core.JDAInfo
import net.dv8tion.jda.core.OnlineStatus
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.CommandExecutor
import xyz.gnarbot.gnar.utils.Context
import xyz.gnarbot.gnar.utils.link
import xyz.gnarbot.gnar.utils.ln
import java.lang.management.ManagementFactory

@Command(
        aliases = arrayOf("info", "botinfo"),
        description = "Show information about the bot."
)
class BotInfoCommand : CommandExecutor() {
    override fun execute(context: Context, args: Array<String>) {
        val registry = context.bot.commandRegistry

        // Uptime
        val s = ManagementFactory.getRuntimeMXBean().uptime / 1000
        val m = s / 60
        val h = m / 60
        val d = h / 24

        var voiceConnections = 0

        var requests = 0
        var textChannels = 0
        var voiceChannels = 0
        var guildData = 0 // wrapper
        var guilds = 0

        var users = 0
        var offline = 0
        var online = 0
        var inactive = 0

        for (shard in context.bot.shards) {
            guilds += shard.guilds.size
            requests += shard.requests

            for (guild in shard.guilds) {
                for (member in guild.members) {
                    when (member.onlineStatus) {
                        OnlineStatus.ONLINE -> online++
                        OnlineStatus.OFFLINE -> offline++
                        OnlineStatus.IDLE -> inactive++
                        else -> {}
                    }
                }

                guild?.selfMember?.voiceState?.channel?.let {
                    voiceConnections++
                }
            }

            users += shard.users.size
            textChannels += shard.textChannels.size
            voiceChannels += shard.voiceChannels.size
            guildData += shard.guildData.size()
        }

        val commandSize = registry.entries.count { it.info.category.show }

        context.send().embed("Bot Information") {
            field("Requests", true) { requests }
            field("Requests Per Hour", true) { requests / Math.max(1, h) }
            field("Website", true) { "gnarbot.xyz" link "https://gnarbot.xyz" }

            field("Text Channels", true) { textChannels }
            field("Voice Channels", true) { voiceChannels }
            field("Voice Connections", true) { voiceConnections }

            field("Guilds", true) { guilds }
            field("Guild Data", true) { guildData }
            field("Uptime", true) { "${d}d ${h % 24}h ${m % 60}m ${s % 60}s" }

            field("Users", true) {
                buildString {
                    append("Total: ").append(users).ln()
                    append("Online: ").append(online).ln()
                    append("Offline: ").append(offline).ln()
                    append("Inactive: ").append(inactive).ln()
                }
            }

            field("Others", true) {
                buildString {
                    append("Creators: **[Avarel](https://github.com/Avarel)** and **[Xevryll](https://github.com/xevryll)**").ln()
                    append("Contributor: **[Gatt](https://github.com/RealGatt)**").ln()
                    append("Commands: **$commandSize**").ln()
                    append("Library: Java **[JDA ${JDAInfo.VERSION}](${JDAInfo.GITHUB})**").ln()
                }
            }
        }.action().queue()
    }
}
