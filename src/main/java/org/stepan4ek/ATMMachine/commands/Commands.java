package org.stepan4ek.ATMMachine.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.stepan4ek.ATMMachine.data.ConfigManager;
import org.stepan4ek.ATMMachine.gui.MainGUI;

public class Commands implements CommandExecutor {
    private final ConfigManager config = ConfigManager.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        // Reload command
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("atm.reload")) {
                player.sendMessage(config.getNoPermission());
                return true;
            }
            ConfigManager.getInstance().reload();
            player.sendMessage("§aКонфиг перезагружен!");
            return true;
        }

        // Open GUI
        if (cmd.getName().equalsIgnoreCase("atm") || cmd.getName().equalsIgnoreCase("bank")) {
            if (!player.hasPermission("atm.open")) {
                player.sendMessage(config.getNoPermission());
                return true;
            }
            new MainGUI().open(player);
            return true;
        }

        return false;
    }
}