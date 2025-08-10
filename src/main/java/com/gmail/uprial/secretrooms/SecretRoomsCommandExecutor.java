package com.gmail.uprial.secretrooms;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.gmail.uprial.secretrooms.common.CustomLogger;
import org.bukkit.entity.Player;

import java.util.*;

import static com.gmail.uprial.secretrooms.common.Formatter.format;

class SecretRoomsCommandExecutor implements CommandExecutor {
    public static class InvalidIntException extends Exception {
        public InvalidIntException(final String message) {
            super(message);
        }
    }
    public static class InvalidMaterialException extends Exception {
        public InvalidMaterialException(final String message) {
            super(message);
        }
    }

    public static final String COMMAND_NS = "secretrooms";

    private final SecretRooms plugin;

    SecretRoomsCommandExecutor(SecretRooms plugin) {
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
                        final int counter = new SecretRoomsExecutor(plugin).breakTerrain(args[1],
                                values.get("x"), values.get("y"), values.get("z"), values.get("radius"));
                        customLogger.info(String.format("%d blocks broken.", counter));
                        return true;
                    }
                } else if ((args.length >= 2) && (args[0].equalsIgnoreCase("break"))
                        && (sender instanceof Player)) {
                    if (sender.hasPermission(COMMAND_NS + ".break")) {
                        final Player player = (Player) sender;
                        final int counter = new SecretRoomsExecutor(plugin).breakTerrain(player.getWorld().getName(),
                                player.getLocation().getBlockX(),
                                player.getLocation().getBlockY(),
                                player.getLocation().getBlockZ(), getInt(args[1]));
                        customLogger.info(String.format("%d blocks broken.", counter));
                        return true;
                    }
                } else if ((args.length >= 2) && (args[0].equalsIgnoreCase("loaded-stats"))) {
                    if (sender.hasPermission(COMMAND_NS + ".loaded-stats")) {

                        final Material material = getMaterial(args[1]);

                        customLogger.info(String.format("Getting %s stats in loaded terrain...", material));
                        final Map<String,Integer> stats = new SecretRoomsExecutor(plugin).getLoadedStats(material);
                        if(stats.isEmpty()) {
                            customLogger.warning(String.format("Material '%s' not found.", args[1]));
                        } else {
                            for (final Map.Entry<String, Integer> entry : stats.entrySet()) {
                                customLogger.info(String.format("%s: %,d", entry.getKey(), entry.getValue()));
                            }
                        }
                        return true;
                    }
                } else if ((args.length >= 2) && (args[0].equalsIgnoreCase("loaded-locations"))) {
                    if (sender.hasPermission(COMMAND_NS + ".loaded-locations")) {

                        final Material material = getMaterial(args[1]);

                        customLogger.info(String.format("Getting %s locations in loaded terrain...", material));
                        final List<Location> locations = new SecretRoomsExecutor(plugin).getLoadedLocations(material);
                        if(locations.isEmpty()) {
                            customLogger.warning(String.format("Material '%s' not found.", args[1]));
                        } else {
                            for (final Location location : locations) {
                                customLogger.info(format(location));
                            }
                        }
                        return true;
                    }
                } else if ((args.length == 0) || (args[0].equalsIgnoreCase("help"))) {
                    String helpString = "==== SecretRooms help ====\n";

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
                    if (sender.hasPermission(COMMAND_NS + ".loaded-stats")) {
                        helpString += '/' + COMMAND_NS + " loaded-stats <material> - show material stats in loaded terrain\n";
                    }
                    if (sender.hasPermission(COMMAND_NS + ".loaded-locations")) {
                        helpString += '/' + COMMAND_NS + " loaded-locations <material> - show material location in loaded terrain\n";
                    }

                    customLogger.info(helpString);
                    return true;
                }
            } catch (InvalidIntException | InvalidMaterialException e) {
                customLogger.error(e.getMessage());
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

    private Material getMaterial(final String string) throws InvalidMaterialException {
        try {
            return Enum.valueOf(Material.class, string.toUpperCase(Locale.getDefault()));
        } catch (NumberFormatException ignored) {
            throw new InvalidMaterialException(String.format("Invalid material '%s'", string));
        }
    }
}
