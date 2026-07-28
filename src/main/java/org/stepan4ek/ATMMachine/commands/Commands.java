package org.stepan4ek.ATMMachine.commands;

import org.stepan4ek.ATMMachine.gui.MainGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Only player command
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;

        // Check command name
        if (cmd.getName().equalsIgnoreCase("atm") || cmd.getName().equalsIgnoreCase("bank")) {
            // Open GUI
            new MainGUI().open(player);
            return true;
        }

        return false;
    }
}