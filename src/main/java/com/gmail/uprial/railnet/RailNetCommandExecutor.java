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
    public class InvalidIntException extends Exception {
        public InvalidIntException(final String message) {
            super(message);
        }
    }

    public static final String COMMAND_NS = "railnet";

    private final RailNet plugin;

    RailNetCommandExecutor(RailNet plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase(COMMAND_NS)) {
            final CustomLogger customLogger = new CustomLogger(plugin.getLogger(), sender);
            try {
                if ((args.length >= 5) && (args[0].equalsIgnoreCase("repopulate-loaded"))) {
                    if (sender.hasPermission(COMMAND_NS + ".repopulate-loaded")) {
                        final Map<Integer, String> params = ImmutableMap.<Integer, String>builder()
                                .put(2, "x")
                                .put(3, "z")
                                .put(4, "radius")
                                .build();
                        final Map<String, Integer> values = new HashMap<>();

                        for (Map.Entry<Integer, String> param : params.entrySet()) {
                            values.put(param.getValue(), getInt(args[param.getKey()]));
                        }
                        final int counter = plugin.repopulateLoaded(args[1],
                                values.get("x"), values.get("z"), values.get("radius"));
                        customLogger.info(String.format("%d chunks repopulated.", counter));
                        return true;
                    }
                } else if ((args.length >= 2) && (args[0].equalsIgnoreCase("repopulate-loaded"))
                        && (sender instanceof Player)) {
                    if (sender.hasPermission(COMMAND_NS + ".repopulate-loaded")) {
                        final Player player = (Player) sender;
                        final Chunk chunk = player.getLocation().getChunk();
                        final int counter = plugin.repopulateLoaded(player.getWorld().getName(),
                                chunk.getX(), chunk.getZ(), getInt(args[1]));
                        customLogger.info(String.format("%d chunks repopulated.", counter));
                        return true;
                    }
                } else if ((args.length >= 2) && (args[0].equalsIgnoreCase("claim"))) {
                    if (sender.hasPermission(COMMAND_NS + ".claim")) {
                        final int density = getInt(args[1]);
                        plugin.populatePlayer((Player) sender, density);
                        customLogger.info(String.format("inventory claimed with density %d.", density));
                        return true;
                    }
                } else if ((args.length >= 6) && (args[0].equalsIgnoreCase("break"))) {
                    if (sender.hasPermission(COMMAND_NS + ".break")) {
                        final Map<Integer, String> params = ImmutableMap.<Integer, String>builder()
                                .put(2, "x")
                                .put(3, "y")
                                .put(4, "z")
                                .put(5, "radius")
                                .build();
                        final Map<String, Integer> values = new HashMap<>();

                        for (Map.Entry<Integer, String> param : params.entrySet()) {
                            values.put(param.getValue(), getInt(args[param.getKey()]));
                        }
                        final int counter = plugin.breakTerrain(args[1],
                                values.get("x"), values.get("y"), values.get("z"), values.get("radius"));
                        customLogger.info(String.format("%d blocks broken.", counter));
                        return true;
                    }
                } else if ((args.length >= 2) && (args[0].equalsIgnoreCase("break"))
                        && (sender instanceof Player)) {
                    if (sender.hasPermission(COMMAND_NS + ".break")) {
                        final Player player = (Player) sender;
                        final int counter = plugin.breakTerrain(player.getWorld().getName(),
                                player.getLocation().getBlockX(),
                                player.getLocation().getBlockY(),
                                player.getLocation().getBlockZ(), getInt(args[1]));
                        customLogger.info(String.format("%d blocks broken.", counter));
                        return true;
                    }
                } else if ((args.length == 0) || (args[0].equalsIgnoreCase("help"))) {
                    String helpString = "==== RailNet help ====\n";

                    if (sender.hasPermission(COMMAND_NS + ".populate-loaded")) {
                        helpString += '/' + COMMAND_NS + " repopulate-loaded <radius> - repopulate loaded terrain around player\n";
                        helpString += '/' + COMMAND_NS + " repopulate-loaded <world> <x> <z> <radius> - repopulate loaded terrain\n";
                    }
                    if (sender.hasPermission(COMMAND_NS + ".claim")) {
                        helpString += '/' + COMMAND_NS + " claim <density> - generate player inventory like it's a chest\n";
                    }
                    if (sender.hasPermission(COMMAND_NS + ".break")) {
                        helpString += '/' + COMMAND_NS + " break <radius> - break terrain around player\n";
                        helpString += '/' + COMMAND_NS + " break <world> <x> <y> <z> <radius> - break terrain\n";
                    }

                    customLogger.info(helpString);
                    return true;
                }
            } catch (InvalidIntException e) {
                customLogger.error(String.format("<%s> should be an integer", args[1]));
            }
        }
        return false;
    }

    private int getInt(final String string) throws InvalidIntException {
        try {
            return Integer.valueOf(string);
        } catch (NumberFormatException ignored) {
            throw new InvalidIntException(String.format("<%s> should be an integer", string));
        }
    }
}
