package org.stepan4ek.ATMMachine.listeners;

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
import org.stepan4ek.ATMMachine.economy.EconomyManager;

public class GUIListener implements Listener {
    private final EconomyManager economy = EconomyManager.getInstance();

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        if (!e.getView().getTitle().equals("§6§lБанкомат")) return;

        // Handle Shift+Click - move all diamonds to GUI
        if (e.isShiftClick() && e.getClickedInventory() != null &&
                e.getClickedInventory().getType() == InventoryType.PLAYER) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null || item.getType() != Material.DIAMOND) {
                p.sendMessage("§cYou can only put diamonds!");
                return;
            }

            int total = item.getAmount();
            int moved = 0;
            Inventory gui = p.getOpenInventory().getTopInventory();

            // Find empty slots or slots with diamonds
            for (int i = 0; i < gui.getSize() && total > 0; i++) {
                ItemStack slot = gui.getItem(i);
                if (slot == null) {
                    int place = Math.min(total, 64);
                    ItemStack copy = item.clone();
                    copy.setAmount(place);
                    gui.setItem(i, copy);
                    total -= place;
                    moved += place;
                } else if (slot.getType() == Material.DIAMOND && slot.getAmount() < 64) {
                    int space = 64 - slot.getAmount();
                    int add = Math.min(total, space);
                    slot.setAmount(slot.getAmount() + add);
                    total -= add;
                    moved += add;
                }
            }

            if (moved > 0) {
                item.setAmount(total > 0 ? total : 0);
                if (total <= 0) e.setCurrentItem(null);
                p.sendMessage("§aMoved §6" + moved + " §adiamonds!");
            } else {
                p.sendMessage("§cNo free slots available!");
            }
            return;
        }

        // Allow player inventory interaction
        if (e.getClickedInventory() != null &&
                e.getClickedInventory().getType() == InventoryType.PLAYER) {
            e.setCancelled(false);
            return;
        }

        // Handle ATM GUI interaction
        if (e.getClickedInventory() != null &&
                e.getClickedInventory().getType() == InventoryType.CHEST) {
            e.setCancelled(true);

            // Empty slot - allow only diamonds
            if (e.getCurrentItem() == null) {
                ItemStack cursor = e.getCursor();
                if (cursor != null && cursor.getType() == Material.DIAMOND) {
                    e.setCancelled(false);
                } else {
                    p.sendMessage("§cYou can only put diamonds!");
                }
                return;
            }

            // Diamond in slot - allow taking it
            if (e.getCurrentItem().getType() == Material.DIAMOND) {
                e.setCancelled(false);
                return;
            }

            // Handle buttons
            String name = e.getCurrentItem().getItemMeta().getDisplayName();
            Inventory gui = e.getInventory();

            switch (name) {
                case "§a§lПополнить": // Deposit
                    int totalDiamonds = 0;
                    for (ItemStack item : gui.getContents()) {
                        if (item != null && item.getType() == Material.DIAMOND) {
                            totalDiamonds += item.getAmount();
                        }
                    }
                    if (totalDiamonds > 0) {
                        for (int i = 0; i < gui.getSize(); i++) {
                            ItemStack item = gui.getItem(i);
                            if (item != null && item.getType() == Material.DIAMOND) {
                                gui.setItem(i, null);
                            }
                        }
                        economy.deposit(p, totalDiamonds);
                        p.sendMessage("§aDeposited §6" + totalDiamonds + " §acoins!");
                    } else {
                        p.sendMessage("§cPut diamonds in the slots!");
                    }
                    break;

                case "§6§lБаланс": // Balance
                    p.sendMessage("§6Your balance: §e" + economy.getBalance(p));
                    break;

                case "§c§lСнять": // Withdraw
                    if (economy.withdraw(p, 1)) {
                        p.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
                        p.sendMessage("§cWithdrawn 1 coin!");
                    }
                    break;

                case "§c§lЗакрыть": // Close
                    returnDiamonds(p);
                    p.closeInventory();
                    break;
            }
        }
    }

    // Return diamonds to player inventory
    private void returnDiamonds(Player p) {
        Inventory gui = p.getOpenInventory().getTopInventory();
        for (ItemStack item : gui.getContents()) {
            if (item != null && item.getType() == Material.DIAMOND) {
                p.getInventory().addItem(item);
            }
        }
        gui.clear();
    }

    // Return diamonds when GUI is closed
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();
        if (!e.getView().getTitle().equals("§6§lБанкомат")) return;

        returnDiamonds(p);
        p.sendMessage("§eDiamonds returned to inventory!");
    }

    // Prevent dragging items in GUI
    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!e.getView().getTitle().equals("§6§lБанкомат")) return;
        e.setCancelled(true);
    }
}