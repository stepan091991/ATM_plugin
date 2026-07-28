package org.stepan4ek.ATMMachine.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.stepan4ek.ATMMachine.data.ConfigManager;
import org.stepan4ek.ATMMachine.economy.EconomyManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransferGUI {
    private final Inventory inventory;
    private final ConfigManager config;
    private final EconomyManager economy;
    private final int playersPerPage;
    private final List<Integer> playerSlots;
    private int page = 0;
    private List<Player> players;

    public TransferGUI() {
        this.config = ConfigManager.getInstance();
        this.economy = EconomyManager.getInstance();
        this.playersPerPage = config.getTransferPlayersPerPage();
        this.playerSlots = new ArrayList<>(config.getTransferPlayerSlots());
        this.inventory = Bukkit.createInventory(null, config.getTransferSize(), config.getTransferTitle());
        this.players = new ArrayList<>();
    }

    public void open(Player player) {
        refresh(player);
        player.openInventory(inventory);
    }

    public void refresh(Player viewer) {
        inventory.clear();
        players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.remove(viewer);

        int start = page * playersPerPage;
        int end = Math.min(start + playersPerPage, players.size());

        // Player heads
        int playerIndex = start;
        for (int slot : playerSlots) {
            if (playerIndex >= end) break;
            Player target = players.get(playerIndex);
            inventory.setItem(slot, head(target));
            playerIndex++;
        }

        // Nav buttons
        if (page > 0) {
            inventory.setItem(18, nav(Material.ARROW, "§6§l<< Назад"));
        }
        if (end < players.size()) {
            inventory.setItem(26, nav(Material.ARROW, "§6§lВперед >>"));
        }

        // Info
        inventory.setItem(0, info(
                Material.BOOK,
                "§e§lИнформация",
                "§7Выберите игрока и сумму"
        ));

        // All buttons
        for (ConfigManager.ButtonConfig button : config.getTransferButtons()) {
            inventory.setItem(button.getSlot(), button.create());
        }

        // Decoration
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass());
            }
        }
    }

    private ItemStack head(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName("§a§l" + player.getName());
        meta.setLore(Arrays.asList(
                "§7Баланс: §6" + economy.getBalance(player) + " " + config.getCurrencyName(),
                "",
                "§eНажмите чтобы выбрать"
        ));
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack nav(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack info(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
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

    public void nextPage() {
        if ((page + 1) * playersPerPage < players.size()) page++;
    }

    public void previousPage() {
        if (page > 0) page--;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public List<Player> getPlayers() {
        return players;
    }
}