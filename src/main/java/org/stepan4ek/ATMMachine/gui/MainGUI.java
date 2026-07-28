package org.stepan4ek.ATMMachine.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MainGUI {
    private final Inventory inventory;

    public MainGUI() {
        // Create inventory
        this.inventory = Bukkit.createInventory(null, 54, "§f\uE000\uE000\uE000\uE000\uE000\uE000\uE000\uE000\uE001");
    }

    // Open inventory to player
    public void open(Player player) {
        fillInventory();
        player.openInventory(inventory);
    }

    // Set inventory content
    private void fillInventory() {
        // Balance button
        inventory.setItem(9, createItem(
                Material.SUNFLOWER,
                "§6§lБаланс",
                "§7Ваш текущий баланс",
                "§7Нажмите чтобы проверить"
        ));

        // Deposit button
        inventory.setItem(11, createItem(
                Material.EMERALD,
                "§a§lПополнить",
                "§7Пополнить баланс",
                "§7Нажмите чтобы пополнить"
        ));

        // Withdraw button
        inventory.setItem(13, createItem(
                Material.REDSTONE,
                "§c§lСнять",
                "§7Снять деньги",
                "§7Нажмите чтобы снять"
        ));

        // Transfer button
        inventory.setItem(15, createItem(
                Material.GOLD_INGOT,
                "§6§lПеревести",
                "§7Перевести деньги другому игроку",
                "§7Нажмите чтобы перевести"
        ));

        // Close button
        inventory.setItem(17, createItem(
                Material.BARRIER,
                "§c§lЗакрыть",
                "§7Закрыть меню"
        ));

        // Set all unused slots
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, createItem(
                        Material.BLACK_STAINED_GLASS_PANE,
                        " "
                ));
            }
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public Inventory getInventory() {
        return inventory;
    }
}