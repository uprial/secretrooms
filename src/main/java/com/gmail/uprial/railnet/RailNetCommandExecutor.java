package com.gmail.uprial.railnet;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.gmail.uprial.railnet.common.CustomLogger;

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
            else if((args.length >= 1) && (args[0].equalsIgnoreCase("forcibly-generate"))) {
                if (sender.hasPermission(COMMAND_NS + ".forcibly-generate")) {
                    plugin.forciblyGenerate();
                    customLogger.info("RailNet forcibly generated terrain.");
                    return true;
                }
            }
            else if((args.length >= 1) && (args[0].equalsIgnoreCase("generate-loaded"))) {
                if (sender.hasPermission(COMMAND_NS + ".generate-loaded")) {
                    plugin.generateLoaded();
                    customLogger.info("RailNet generated loaded terrain.");
                    return true;
                }
            }
            else if((args.length == 0) || (args[0].equalsIgnoreCase("help"))) {
                String helpString = "==== RailNet help ====\n";

                if (sender.hasPermission(COMMAND_NS + ".reload")) {
                    helpString += '/' + COMMAND_NS + " reload - reload config from disk\n";
                }
                if (sender.hasPermission(COMMAND_NS + ".forcibly-generate")) {
                    helpString += '/' + COMMAND_NS + " forcibly-generate - forcibly generate terrain\n";
                }
                if (sender.hasPermission(COMMAND_NS + ".generate-loaded")) {
                    helpString += '/' + COMMAND_NS + " generate-loaded - generate loaded terrain\n";
                }

                customLogger.info(helpString);
                return true;
            }
        }
        return false;
    }
}
