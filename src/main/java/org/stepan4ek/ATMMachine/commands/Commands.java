package org.stepan4ek.ATMMachine.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.stepan4ek.ATMMachine.data.ConfigManager;
import org.stepan4ek.ATMMachine.economy.EconomyManager;
import org.stepan4ek.ATMMachine.gui.MainGUI;

public class Commands implements CommandExecutor {
    private final ConfigManager config = ConfigManager.getInstance();
    private final EconomyManager economy = EconomyManager.getInstance();

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

        // Set balance
        if (args.length > 0 && args[0].equalsIgnoreCase("set")) {
            if (!player.hasPermission("atm.admin")) {
                player.sendMessage(config.getNoPermission());
                return true;
            }

            if (args.length < 3) {
                player.sendMessage("§cИспользование: /atm set <игрок> <сумма>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(config.getPlayerNotFound());
                return true;
            }

            try {
                double amount = Double.parseDouble(args[2]);
                if (amount < 0) {
                    player.sendMessage("§cСумма не может быть отрицательной!");
                    return true;
                }

                economy.setBalance(target, amount);
                player.sendMessage("§aБаланс игрока §e" + target.getName() + " §aустановлен на §6" + amount + " §aмонет!");
                target.sendMessage("§aВаш баланс установлен на §6" + amount + " §aмонет!");
            } catch (NumberFormatException e) {
                player.sendMessage("§cВведите корректную сумму!");
            }
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