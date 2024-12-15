package com.gmail.uprial.railnet;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.gmail.uprial.railnet.common.CustomLogger;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

class RailNetCommandExecutor implements CommandExecutor {
    public static final String COMMAND_NS = "railnet";

    private final RailNet plugin;

    RailNetCommandExecutor(RailNet plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase(COMMAND_NS)) {
            final CustomLogger customLogger = new CustomLogger(plugin.getLogger(), sender);

            if((args.length >= 1) && (args[0].equalsIgnoreCase("reload"))) {
                if (sender.hasPermission(COMMAND_NS + ".reload")) {
                    plugin.reloadConfig(customLogger);
                    customLogger.info("RailNet reloaded config from disk.");
                    return true;
                }
            }
            else if((args.length >= 5) && (args[0].equalsIgnoreCase("repopulate-loaded"))) {
                if (sender.hasPermission(COMMAND_NS + ".repopulate-loaded")) {
                    final Map<Integer,String> params = ImmutableMap.<Integer,String>builder()
                            .put(2, "x")
                            .put(3, "z")
                            .put(4, "radius")
                            .build();
                    final Map<String,Integer> values = new HashMap<>();

                    for(Map.Entry<Integer, String> param : params.entrySet()) {
                        final int value;
                        try {
                            value = Integer.valueOf(args[param.getKey()]);
                        } catch (NumberFormatException ignored) {
                            customLogger.error(String.format("<%s> should be an integer", param.getValue()));
                            return false;
                        }
                        values.put(param.getValue(), value);
                    }
                    final int counter = plugin.repopulateLoaded(args[1],
                            values.get("x"), values.get("z"), values.get("radius"));
                    customLogger.info(String.format("%d chunks repopulated.", counter));
                    return true;
                }
            } else if((args.length >= 2) && (args[0].equalsIgnoreCase("repopulate-loaded"))
                    && (sender instanceof Player)) {
                final int radius;
                try {
                    radius = Integer.valueOf(args[1]);
                } catch (NumberFormatException ignored) {
                    customLogger.error(String.format("<%s> should be an integer", args[1]));
                    return false;
                }
                final Player player = (Player)sender;
                final Chunk chunk = player.getLocation().getChunk();
                final int counter = plugin.repopulateLoaded(player.getWorld().getName(),
                        chunk.getX(), chunk.getZ(), radius);
                customLogger.info(String.format("%d chunks repopulated.", counter));
                return true;
            } else if((args.length == 0) || (args[0].equalsIgnoreCase("help"))) {
                String helpString = "==== RailNet help ====\n";

                if (sender.hasPermission(COMMAND_NS + ".reload")) {
                    helpString += '/' + COMMAND_NS + " reload - reload config from disk\n";
                }
                if (sender.hasPermission(COMMAND_NS + ".populate-loaded")) {
                    helpString += '/' + COMMAND_NS + " repopulate-loaded <radius> - repopulate loaded terrain around player\n";
                    helpString += '/' + COMMAND_NS + " repopulate-loaded <world> <x> <z> <radius> - repopulate loaded terrain\n";
                }

                customLogger.info(helpString);
                return true;
            }
        }
        return false;
    }
}
