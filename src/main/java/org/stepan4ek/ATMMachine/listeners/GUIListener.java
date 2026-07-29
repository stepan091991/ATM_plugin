package org.stepan4ek.ATMMachine.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.stepan4ek.ATMMachine.data.ConfigManager;
import org.stepan4ek.ATMMachine.economy.EconomyManager;
import org.stepan4ek.ATMMachine.gui.MainGUI;
import org.stepan4ek.ATMMachine.gui.TransferGUI;
import org.stepan4ek.ATMMachine.gui.WithdrawGUI;

import java.util.HashMap;
import java.util.Map;

public class GUIListener implements Listener {
    private final EconomyManager economy = EconomyManager.getInstance();
    private final ConfigManager config = ConfigManager.getInstance();

    private final Map<Player, Player> selectedPlayer = new HashMap<>();
    private final Map<Player, Double> selectedAmount = new HashMap<>();

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        if (!isOurGUI(title)) return;

        // Shift
        if (e.isShiftClick()) {
            e.setCancelled(true);
            handleShiftClick(p, e);
            return;
        }

        // Player inventory
        if (e.getClickedInventory() != null && e.getClickedInventory().getType() == InventoryType.PLAYER) {
            e.setCancelled(false);
            return;
        }

        // Our GUI
        if (e.getClickedInventory() != null && e.getClickedInventory().getType() == InventoryType.CHEST) {
            e.setCancelled(true);

            // Empty slot
            if (e.getCurrentItem() == null) {
                ItemStack cursor = e.getCursor();
                if (cursor != null && config.isCurrencyItem(cursor) && config.isEmptySlot(e.getRawSlot())) {
                    e.setCancelled(false);
                } else if (cursor != null) {
                    p.sendMessage(config.getOnlyCurrency());
                }
                return;
            }

            // Currency item
            if (config.isCurrencyItem(e.getCurrentItem())) {
                e.setCancelled(false);
                return;
            }

            // Button click
            String name = e.getCurrentItem().getItemMeta().getDisplayName();
            handleButtonClick(p, name, title, e);
        }
    }

    private void handleShiftClick(Player p, InventoryClickEvent e) {
        // From player inventory
        if (e.getClickedInventory() != null && e.getClickedInventory().getType() == InventoryType.PLAYER) {
            ItemStack item = e.getCurrentItem();
            if (item != null && config.isCurrencyItem(item)) {
                moveCurrencyToGUI(p, item);
            }
            return;
        }

        // From GUI
        if (e.getClickedInventory() != null && e.getClickedInventory().getType() == InventoryType.CHEST) {
            ItemStack item = e.getCurrentItem();
            if (item != null && config.isCurrencyItem(item)) {
                moveCurrencyToPlayer(p, item);
            }
        }
    }

    private void moveCurrencyToGUI(Player p, ItemStack item) {
        Inventory gui = p.getOpenInventory().getTopInventory();
        int total = item.getAmount();

        for (int i = 0; i < gui.getSize() && total > 0; i++) {
            if (config.isEmptySlot(i) && gui.getItem(i) == null) {
                int toPlace = Math.min(total, 64);
                ItemStack copy = item.clone();
                copy.setAmount(toPlace);
                gui.setItem(i, copy);
                total -= toPlace;
            }
        }

        if (total > 0) {
            item.setAmount(total);
        } else {
            item.setAmount(0);
            p.getInventory().removeItem(item);
        }
    }

    private void moveCurrencyToPlayer(Player p, ItemStack item) {
        Inventory gui = p.getOpenInventory().getTopInventory();
        int total = item.getAmount();

        int slot = -1;
        for (int i = 0; i < gui.getSize(); i++) {
            ItemStack guiItem = gui.getItem(i);
            if (guiItem != null && guiItem.equals(item)) {
                slot = i;
                break;
            }
        }

        if (slot == -1) return;

        int space = 0;
        for (ItemStack invItem : p.getInventory().getContents()) {
            if (invItem == null) {
                space += 64;
            } else if (invItem.isSimilar(item) && invItem.getAmount() < 64) {
                space += 64 - invItem.getAmount();
            }
        }

        if (space == 0) return;

        int toMove = Math.min(total, space);
        ItemStack copy = item.clone();
        copy.setAmount(toMove);
        p.getInventory().addItem(copy);

        if (total > toMove) {
            item.setAmount(total - toMove);
        } else {
            gui.setItem(slot, null);
        }
    }

    private boolean isOurGUI(String title) {
        return title.equals(config.getMainTitle()) ||
                title.equals(config.getTransferTitle()) ||
                title.equals(config.getWithdrawTitle());
    }

    private void handleButtonClick(Player p, String name, String title, InventoryClickEvent e) {
        // Main GUI
        if (title.equals(config.getMainTitle())) {
            for (ConfigManager.ButtonConfig button : config.getMainButtons()) {
                if (name.equals(button.getName())) {
                    executeAction(p, button.getAction(), button.getAmount());
                    return;
                }
            }
            return;
        }

        // Transfer GUI
        if (title.equals(config.getTransferTitle())) {
            TransferGUI gui = (TransferGUI) e.getInventory().getHolder();

            // Player head
            if (e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) e.getCurrentItem().getItemMeta();
                if (meta.getOwningPlayer() != null) {
                    Player target = Bukkit.getPlayer(meta.getOwningPlayer().getName());
                    if (target != null && target.isOnline()) {
                        selectedPlayer.put(p, target);
                        p.sendMessage(config.getChosedPlayer(target.getName()));
                    }
                }
                return;
            }

            // Nav
            if (name.equals("§6§l<< Назад")) {
                gui.previousPage();
                gui.refresh(p);
                return;
            }
            if (name.equals("§6§lВперед >>")) {
                gui.nextPage();
                gui.refresh(p);
                return;
            }

            // Buttons
            for (ConfigManager.ButtonConfig button : config.getTransferButtons()) {
                if (name.equals(button.getName())) {
                    String action = button.getAction();

                    // Handle amount selection
                    if (action.equals("select_amount")) {
                        double amount = button.getAmount();
                        selectedAmount.put(p, amount);
                        p.sendMessage(config.getChosedValuteSumm(amount));
                        return;
                    }

                    // Handle confirm
                    if (action.equals("confirm_transfer")) {
                        Player target = selectedPlayer.get(p);
                        Double amount = selectedAmount.get(p);

                        if (target == null) {
                            p.sendMessage(config.getNoSelectedPlayer());
                            return;
                        }
                        if (amount == null) {
                            p.sendMessage(config.getInvalidAmount());
                            return;
                        }
                        if (!target.isOnline()) {
                            p.sendMessage(config.getPlayerNotFound());
                            selectedPlayer.remove(p);
                            return;
                        }
                        if (economy.getBalance(p) < amount) {
                            p.sendMessage(config.getInsufficientFunds());
                            return;
                        }

                        if (economy.transfer(p, target, amount)) {
                            p.sendMessage(config.getTransferSuccess(amount, target.getName()));
                            target.sendMessage(config.getTransferReceived(amount, p.getName()));
                            p.closeInventory();
                        }
                        return;
                    }

                    // Handle cancel
                    if (action.equals("cancel")) {
                        selectedPlayer.remove(p);
                        selectedAmount.remove(p);
                        new MainGUI().open(p);
                        return;
                    }
                }
            }
            return;
        }

        // Withdraw GUI
        if (title.equals(config.getWithdrawTitle())) {
            for (ConfigManager.ButtonConfig button : config.getWithdrawButtons()) {
                if (name.equals(button.getName())) {
                    executeAction(p, button.getAction(), button.getAmount());
                    return;
                }
            }
        }
    }

    private void executeAction(Player p, String action, double amount) {
        switch (action) {
            case "balance":
                p.sendMessage(config.getBalance(economy.getBalance(p)));
                break;

            case "deposit":
                deposit(p);
                break;

            case "withdraw":
                new WithdrawGUI().open(p);
                break;

            case "transfer":
                selectedPlayer.remove(p);
                selectedAmount.remove(p);
                new TransferGUI().open(p);
                break;

            case "close":
                p.closeInventory();
                break;

            case "withdraw_amount":
                // Check balance
                if (economy.getBalance(p) < amount) {
                    p.sendMessage(config.getInsufficientFunds());
                    return;
                }

                // Check free slots in player inventory
                int freeSlots = 0;
                for (ItemStack item : p.getInventory().getStorageContents()) {
                    if (item == null) freeSlots++;
                }

                ConfigManager.CurrencyItem currency = config.getCurrencyItemByValue(amount);
                if (currency == null) {
                    return;
                }

                int totalItems = (int) (amount / currency.getValue());
                if (totalItems <= 0) {
                    return;
                }

                int slotsNeeded = (totalItems + 63) / 64;

                if (freeSlots < slotsNeeded) {
                    p.sendMessage(config.getNoSlots());
                    return;
                }

                if (economy.withdraw(p, amount)) {
                    p.sendMessage(config.getWithdrawn(amount, economy.getBalance(p)));
                    giveCurrency(p, amount);
                }
                break;

            case "confirm":
                p.sendMessage(config.getActionConfirmed());
                p.closeInventory();
                break;

            case "cancel":
                new MainGUI().open(p);
                break;
        }
    }

    private void deposit(Player p) {
        Inventory gui = p.getOpenInventory().getTopInventory();
        int total = config.getTotalCurrencyValue(gui);

        if (total > 0) {
            config.removeCurrencyItems(gui);
            economy.deposit(p, total);
            p.sendMessage(config.getDeposited(total));
        } else {
            p.sendMessage(config.getPutValute());
        }
    }

    private void giveCurrency(Player p, double amount) {
        ConfigManager.CurrencyItem currency = config.getCurrencyItemByValue(amount);
        if (currency == null) return;

        int totalItems = (int) (amount / currency.getValue());
        if (totalItems <= 0) return;

        int remaining = totalItems;
        while (remaining > 0) {
            int toGive = Math.min(remaining, 64);
            p.getInventory().addItem(currency.create(toGive));
            remaining -= toGive;
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;

        Player p = (Player) e.getPlayer();
        String title = e.getView().getTitle();

        if (!isOurGUI(title)) return;

        if (title.equals(config.getTransferTitle())) {
            selectedPlayer.remove(p);
            selectedAmount.remove(p);
        }

        // Return items to player
        Inventory gui = e.getInventory();
        for (ItemStack item : gui.getContents()) {
            if (item != null && config.isCurrencyItem(item)) {
                p.getInventory().addItem(item);
            }
        }
        gui.clear();
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (isOurGUI(e.getView().getTitle())) {
            e.setCancelled(true);
        }
    }
}