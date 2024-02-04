package me.arcator.sdi

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.absoluteValue

@Suppress("unused")
class SDI : JavaPlugin() {
    private fun log(sender: Player, message: String) {
        sender.sendMessage(message)
        logger.info(message)
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ) =
        when (command.name) {
            "ecount" -> ecounter(sender)
            "sdi" -> sdi(sender, args)
            else -> false
        }


    private fun ecounter(sender: CommandSender): Boolean {
        if (sender is Player) {
            val c = sender.location.chunk
            sender.sendMessage("Entities ${c.entities.size}, Tile Entities ${c.tileEntities.size}")
        } else {
            sender.sendMessage("You must be a player to use this command!")
        }
        return sender is Player
    }

    private fun sdi(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender is Player) {
            val toPurge = sender.world.entities
                .filter { it !is Player }
                .filter { it.location.y.absoluteValue > 4000 }

            toPurge.forEach {
                var message = "${it.type.name} at Y: ${it.location.y.toInt()}"
                if (args.isNotEmpty() && args.first() == "purge") {
                    message = "Purged! $message"
                    it.remove()
                }

                log(sender, message)
            }
            log(sender, "Total results - ${toPurge.size}")
        } else {
            sender.sendMessage("You must be a player to use this command!")
        }
        return sender is Player
    }
}
