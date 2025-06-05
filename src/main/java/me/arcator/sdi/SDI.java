package me.arcator.sdi;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SDI extends JavaPlugin {
    @Override
    public void onEnable() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var registrar = event.registrar();
            registrar.register("ecount", new ECountCommand());
            registrar.register("sdi", new SDICommand());
        });
    }
}

class ECountCommand implements BasicCommand {
    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        CommandSender sender = commandSourceStack.getSender();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("You must be a player to use this command!", NamedTextColor.RED));
            return;
        }

        int radius;

        if (args.length == 0) {
            radius = 1;
        } else {
            try {
                radius = Integer.parseInt(args[0]);
                if (radius < 1) {
                    radius = 1;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid radius! Please provide a number >= 1", NamedTextColor.RED));
                return;
            }
        }

        Chunk centerChunk = player.getLocation().getChunk();
        World world = player.getWorld();
        int totalEntities = 0;
        int totalTileEntities = 0;

        for (int x = -radius + 1; x < radius; x++) {
            for (int z = -radius + 1; z < radius; z++) {
                Chunk chunk = world.getChunkAt(centerChunk.getX() + x, centerChunk.getZ() + z);
                totalEntities += chunk.getEntities().length;
                totalTileEntities += chunk.getTileEntities().length;
            }
        }

        sender.sendMessage(
                Component.text()
                        .append(Component.text("Chunk scan results (radius: " + radius + "):", NamedTextColor.GREEN))
                        .appendNewline()
                        .append(Component.text("Total Entities: ", NamedTextColor.GRAY))
                        .append(Component.text(totalEntities, NamedTextColor.WHITE))
                        .appendNewline()
                        .append(Component.text("Total Tile Entities: ", NamedTextColor.GRAY))
                        .append(Component.text(totalTileEntities, NamedTextColor.WHITE))
                        .build()
        );
    }

    @Override
    public @NotNull String permission() {
        return "ecounter.basic";
    }
}

class SDICommand implements BasicCommand {
    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        CommandSender sender = commandSourceStack.getSender();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("You must be a player to use this command!", NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(
                    Component.text()
                            .append(Component.text("Usage: /sdi <y-limit> [purge]", NamedTextColor.YELLOW))
                            .appendNewline()
                            .append(Component.text("Example: /sdi 4000 - List entities beyond ±4000 Y", NamedTextColor.GRAY))
                            .appendNewline()
                            .append(Component.text("Example: /sdi 4000 purge - Remove entities beyond ±4000 Y", NamedTextColor.GRAY))
                            .build()
            );
            return;
        }

        int yLimit;
        try {
            yLimit = Integer.parseInt(args[0]);
            if (yLimit < 1) {
                yLimit = 1;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid Y limit! Please provide a positive number", NamedTextColor.RED));
            return;
        }

        boolean purge = args.length > 1 && args[1].equalsIgnoreCase("purge");

        List<Entity> toPurge = new ArrayList<>();
        for (Entity entity : player.getWorld().getEntities()) {
            if (!(entity instanceof Player) && Math.abs(entity.getLocation().getY()) > yLimit) {
                toPurge.add(entity);
            }
        }

        if (toPurge.isEmpty()) {
            sender.sendMessage(
                    Component.text("No entities found beyond ±" + yLimit + " Y coordinate", NamedTextColor.YELLOW)
            );
            return;
        }

        sender.sendMessage(
                Component.text()
                        .append(Component.text(purge ? "Purging entities..." : "Found entities:", NamedTextColor.GOLD))
                        .build()
        );

        for (Entity entity : toPurge) {
            Component message = Component.text()
                    .append(Component.text(purge ? "✗ Removed: " : "• ",
                            purge ? NamedTextColor.RED : NamedTextColor.GRAY))
                    .append(Component.text(entity.getType().name(), NamedTextColor.WHITE))
                    .append(Component.text(" at Y: ", NamedTextColor.GRAY))
                    .append(Component.text((int) entity.getLocation().getY(), NamedTextColor.AQUA))
                    .build();

            sender.sendMessage(message);

            if (purge) {
                entity.remove();
            }
        }

        sender.sendMessage(
                Component.text()
                        .appendNewline()
                        .append(Component.text("Total: ", NamedTextColor.GRAY))
                        .append(Component.text(toPurge.size(), NamedTextColor.WHITE))
                        .append(Component.text(" entities", NamedTextColor.GRAY))
                        .append(Component.text(purge ? " removed" : " found", NamedTextColor.GRAY))
                        .build()
        );
    }

    @Override
    public @NotNull String permission() {
        return "sdi.basic";
    }
}
