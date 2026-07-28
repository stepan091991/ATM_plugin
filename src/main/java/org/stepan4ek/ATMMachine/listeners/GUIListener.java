package org.stepan4ek.ATMMachine.listeners;

import org.bukkit.inventory.Inventory;
import org.stepan4ek.ATMMachine.economy.EconomyManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {
    private final EconomyManager economy = EconomyManager.getInstance();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Check GUI name
        if (!title.equals("§6§lБанкомат")) return;

        // Allow player inventory
        if (event.getClickedInventory() != null &&
                event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(false);
            return;
        }

        // Process ATM GUI
        if (event.getClickedInventory() != null &&
                event.getClickedInventory().getType() == InventoryType.CHEST) {

            event.setCancelled(true);

            if (event.getCurrentItem() == null) {
                handleEmptySlotClick(event, player);
                return;
            }

            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem.getType() == Material.DIAMOND) {
                event.setCancelled(false);
                return;
            }

            String itemName = clickedItem.getItemMeta().getDisplayName();
            handleGUIClick(player, itemName);
        }
    }

    // Process click by empty slot
    private void handleEmptySlotClick(InventoryClickEvent event, Player player) {
        ItemStack cursor = event.getCursor();

        if (cursor != null && cursor.getType() == Material.DIAMOND) {
            event.setCancelled(false);
        } else {
            event.setCancelled(true);
            player.sendMessage("§cМожно класть только алмазы!");
        }
    }

    // Process button clicks
    private void handleGUIClick(Player player, String itemName) {
        switch (itemName) {
            case "§a§lПополнить":
                // Counting all valute in GUI
                int totalValute = 0;
                Inventory inv = player.getOpenInventory().getTopInventory();

                for (ItemStack item : inv.getContents()) {
                    if (item != null && item.getType() == Material.DIAMOND) {
                        totalValute += item.getAmount();
                    }
                }

                if (totalValute > 0) {

                    // Clear all valute in GUI
                    for (int i = 0; i < inv.getSize(); i++) {
                        ItemStack item = inv.getItem(i);
                        if (item != null && item.getType() == Material.DIAMOND) {
                            inv.setItem(i, null);
                        }
                    }

                    // Deposit valute to player
                    economy.deposit(player, totalValute);
                    player.sendMessage("§aВы пополнили баланс на §6" + totalValute + "!");
                } else {
                    player.sendMessage("§cПоложите валюту в пустые слоты!");
                }
                break;

            case "§6§lБаланс":
                player.sendMessage("§6Ваш баланс: §e" + economy.getBalance(player));
                break;

            case "§c§lСнять":
                if (economy.withdraw(player, 1)) {
                    player.sendMessage("§cСнята 1 валюта!");
                    player.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
                }
                break;

            case "§c§lЗакрыть":
                // Return valute on close GUI
                returnValute(player);
                player.closeInventory();
                break;
        }
    }

    // Return valute on close GUI
    private void returnValute(Player player) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        boolean hasValute = false;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == Material.DIAMOND) {
                player.getInventory().addItem(item);
                inv.setItem(i, null);
                hasValute = true;
            }
        }

        if (hasValute) {
            player.sendMessage("§Валюта возвращена в инвентарь!");
        }
    }

    // Restrict drag in ATM GUI
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.equals("§6§lБанкомат")) return;

        for (Integer slot : event.getRawSlots()) {
            if (slot >= 0 && slot <= 26) {
                // Check if drag item is valute
                ItemStack item = event.getNewItems().get(slot);
                if (item != null && item.getType() == Material.DIAMOND) {
                    // Allow in empty slot only
                    if (event.getView().getTopInventory().getItem(slot) == null) {
                        event.setCancelled(false);
                    } else {
                        event.setCancelled(true);
                        player.sendMessage("§cСлот занят!");
                        return;
                    }
                } else {
                    event.setCancelled(true);
                    if (item != null) {
                        player.sendMessage("§cМожно класть только алмазы!");
                    }
                    return;
                }
            }
        }

        // Allow drag in player inventory
        event.setCancelled(false);
    }

    // Process close GUI
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        // Check GUI name
        if (!title.equals("§6§lБанкомат")) return;

        // Return valute on GUI close
        Inventory inv = event.getInventory();
        boolean hasValute = false;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == Material.DIAMOND) {
                player.getInventory().addItem(item);
                inv.setItem(i, null);
                hasValute = true;
            }
        }

        if (hasValute) {
            player.sendMessage("§eАлмазы возвращены в инвентарь!");
        }
    }
}