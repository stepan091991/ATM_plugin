package org.stepan4ek.ATMMachine.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.stepan4ek.ATMMachine.data.ConfigManager;

public class WithdrawGUI {
    private final Inventory inventory;
    private final ConfigManager config;

    public WithdrawGUI() {
        this.config = ConfigManager.getInstance();
        this.inventory = Bukkit.createInventory(null, config.getWithdrawSize(), config.getWithdrawTitle());
    }

    public void open(Player player) {
        fill();
        player.openInventory(inventory);
    }

    private void fill() {
        // All buttons
        for (ConfigManager.ButtonConfig button : config.getWithdrawButtons()) {
            inventory.setItem(button.getSlot(), button.create());
        }

        // Decoration
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass());
            }
        }
    }

    private ItemStack glass() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(1);
        meta.setDisplayName(" ");
        meta.setHideTooltip(true);
        item.setItemMeta(meta);
        return item;
    }

    public Inventory getInventory() {
        return inventory;
    }
}