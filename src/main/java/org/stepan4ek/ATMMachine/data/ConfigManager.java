package org.stepan4ek.ATMMachine.data;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.stepan4ek.ATMMachine.ATMMachine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private static ConfigManager instance;
    private FileConfiguration config;
    private File configFile;

    // Main GUI
    private String mainTitle;
    private int mainSize;
    private List<Integer> mainEmptySlots;
    private List<ButtonConfig> mainButtons;

    // Transfer GUI
    private String transferTitle;
    private int transferSize;
    private int transferPlayersPerPage;
    private List<Integer> transferPlayerSlots;
    private List<ButtonConfig> transferButtons;
    private String chosedValuteSumm;

    // Withdraw GUI
    private String withdrawTitle;
    private int withdrawSize;
    private List<ButtonConfig> withdrawButtons;

    // Currency
    private List<CurrencyItem> currencyItems;

    // General
    private String currencyName;

    // Messages
    private String noPermission, invalidAmount, insufficientFunds;
    private String deposited, withdrawn, balanceMsg;
    private String onlyCurrency, playerNotFound;
    private String transferSuccess, transferReceived;
    private String noSlots;
    private String actionConfirmed;
    private String putValute;
    private String noSelectedPlayer;
    private String chosedPlayer;
    private ConfigManager() {
        configFile = new File(ATMMachine.getInstance().getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            ATMMachine.getInstance().saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        loadConfig();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private void loadConfig() {
        // Main GUI
        mainTitle = config.getString("gui.main.title", "§6§lБанкомат");
        mainSize = config.getInt("gui.main.size", 27);
        mainEmptySlots = config.getIntegerList("gui.main.empty-slots");
        mainButtons = loadButtons("gui.main.buttons");

        // Transfer GUI
        transferTitle = config.getString("gui.transfer.title", "§6§lПеревод средств");
        transferSize = config.getInt("gui.transfer.size", 54);
        transferPlayersPerPage = config.getInt("gui.transfer.players-per-page", 18);
        transferPlayerSlots = config.getIntegerList("gui.transfer.player-slots");
        transferButtons = loadButtons("gui.transfer.buttons");

        // Withdraw GUI
        withdrawTitle = config.getString("gui.withdraw.title", "§6§lВыбор суммы");
        withdrawSize = config.getInt("gui.withdraw.size", 27);
        withdrawButtons = loadButtons("gui.withdraw.buttons");

        // Currency
        currencyItems = loadCurrencyItems("currency-items");

        // General
        currencyName = config.getString("settings.currency-name", "Coin");

        // Messages
        noPermission = config.getString("messages.no-permission", "§cУ вас нет прав!");
        invalidAmount = config.getString("messages.invalid-amount", "§cВведите корректную сумму!");
        insufficientFunds = config.getString("messages.insufficient-funds", "§cНедостаточно средств!");
        deposited = config.getString("messages.deposited", "§aВы пополнили баланс на {amount} {currency}!");
        withdrawn = config.getString("messages.withdrawn", "§cВы сняли {amount} {currency}!");
        balanceMsg = config.getString("messages.balance", "§6Ваш баланс: §e{balance} {currency}");
        onlyCurrency = config.getString("messages.only-currency", "§cМожно класть только валюту!");
        playerNotFound = config.getString("messages.player-not-found", "§cИгрок не найден!");
        transferSuccess = config.getString("messages.transfer-success", "§aПереведено {amount} {currency} игроку {player}!");
        transferReceived = config.getString("messages.transfer-received", "§aПолучено {amount} {currency} от игрока {player}!");
        noSlots = config.getString("messages.withdrawn-no-slots", "§cУ вас недостаточно свободных слотов!");
        actionConfirmed = config.getString("messages.action-confirmed", "§aДействие подтверждено!");
        putValute = config.getString("messages.put-valute-to-empty-slots", "§cПоложите валюту в пустые слоты!");
        noSelectedPlayer = config.getString("messages.no-selected-player", "§cВы не выбрали игрока!");
        chosedValuteSumm = config.getString("messages.chosed-valute-summ", "§aВыбрана сумма: §e {amount} {currency}!");
        chosedPlayer = config.getString("messages.chosed-player", "§aВыбран игрок: §e {player}!");
    }

    private List<ButtonConfig> loadButtons(String path) {
        List<ButtonConfig> buttons = new ArrayList<>();

        if (!config.contains(path)) {
            ATMMachine.getInstance().getLogger().warning("Config section missing: " + path);
            return buttons;
        }

        List<Map<?, ?>> items = config.getMapList(path);

        for (Map<?, ?> item : items) {
            try {
                String action = (String) item.get("action");
                String material = (String) item.get("material");
                Integer slot = (Integer) item.get("slot");
                String name = (String) item.get("name");
                List<String> lore = (List<String>) item.get("lore");
                Integer customModel = item.containsKey("custom-model-data") ? (Integer) item.get("custom-model-data") : 0;
                Double amount = item.containsKey("amount") ? (Double) item.get("amount") : 0.0;

                // Skip if no req fields
                if (action == null || material == null || slot == null || name == null) {
                    ATMMachine.getInstance().getLogger().warning("Skipping invalid button config: missing required fields");
                    continue;
                }

                if (lore == null) lore = new ArrayList<>();

                buttons.add(new ButtonConfig(action, material, slot, name, lore, customModel, amount));
            } catch (Exception e) {
                ATMMachine.getInstance().getLogger().warning("Failed to load button from config: " + e.getMessage());
            }
        }

        return buttons;
    }

    private List<CurrencyItem> loadCurrencyItems(String path) {
        List<CurrencyItem> items = new ArrayList<>();

        if (!config.contains(path)) {
            ATMMachine.getInstance().getLogger().warning("Currency items section missing: " + path);
            return items;
        }

        List<Map<?, ?>> itemMaps = config.getMapList(path);

        for (Map<?, ?> map : itemMaps) {
            try {
                String material = (String) map.get("material");
                Integer value = (Integer) map.get("value");

                if (material == null || value == null) {
                    ATMMachine.getInstance().getLogger().warning("Skipping invalid currency item: missing material or value");
                    continue;
                }

                items.add(new CurrencyItem(material, value));
            } catch (Exception e) {
                ATMMachine.getInstance().getLogger().warning("Failed to load currency item: " + e.getMessage());
            }
        }

        return items;
    }

    public boolean isEmptySlot(int slot) {
        return mainEmptySlots.contains(slot);
    }

    public boolean isCurrencyItem(ItemStack item) {
        if (item == null) return false;
        for (CurrencyItem currency : currencyItems) {
            if (currency.matches(item)) return true;
        }
        return false;
    }

    public int getCurrencyValue(ItemStack item) {
        if (item == null) return 0;
        for (CurrencyItem currency : currencyItems) {
            if (currency.matches(item)) {
                return currency.getValue() * item.getAmount();
            }
        }
        return 0;
    }

    public int getTotalCurrencyValue(Inventory inv) {
        int total = 0;
        for (ItemStack item : inv.getContents()) {
            if (item != null) total += getCurrencyValue(item);
        }
        return total;
    }

    public void removeCurrencyItems(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && isCurrencyItem(item)) {
                inv.setItem(i, null);
            }
        }
    }

    public CurrencyItem getCurrencyItemByValue(double amount) {
        for (CurrencyItem currency : currencyItems) {
            if (currency.getValue() <= amount) {
                return currency;
            }
        }
        return currencyItems.isEmpty() ? null : currencyItems.get(0);
    }

    // Button config
    public static class ButtonConfig {
        private final String action;
        private final String material;
        private final int slot;
        private final String name;
        private final List<String> lore;
        private final int customModel;
        private final double amount;

        public ButtonConfig(String action, String material, int slot, String name, List<String> lore, int customModel, double amount) {
            this.action = action;
            this.material = material;
            this.slot = slot;
            this.name = name;
            this.lore = lore;
            this.customModel = customModel;
            this.amount = amount;
        }

        public String getAction() { return action; }
        public int getSlot() { return slot; }
        public String getName() { return name; }
        public double getAmount() { return amount; }

        public ItemStack create() {
            Material mat = Material.getMaterial(material.toUpperCase());
            if (mat == null) mat = Material.STONE;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                if (lore != null && !lore.isEmpty()) meta.setLore(lore);
                if (customModel > 0) meta.setCustomModelData(customModel);
                item.setItemMeta(meta);
            }
            return item;
        }
    }

    // Currency item
    public static class CurrencyItem {
        private final String material;
        private final int value;

        public CurrencyItem(String material, int value) {
            this.material = material;
            this.value = value;
        }

        public int getValue() { return value; }

        public boolean matches(ItemStack item) {
            if (item == null) return false;
            Material mat = Material.getMaterial(material.toUpperCase());
            return mat != null && item.getType() == mat;
        }

        public ItemStack create(int amount) {
            Material mat = Material.getMaterial(material.toUpperCase());
            if (mat == null) mat = Material.DIAMOND;
            return new ItemStack(mat, amount);
        }
    }

    // Getters
    public String getMainTitle() { return mainTitle; }
    public int getMainSize() { return mainSize; }
    public List<Integer> getMainEmptySlots() { return mainEmptySlots; }
    public List<ButtonConfig> getMainButtons() { return mainButtons; }

    public String getTransferTitle() { return transferTitle; }
    public int getTransferSize() { return transferSize; }
    public int getTransferPlayersPerPage() { return transferPlayersPerPage; }
    public List<Integer> getTransferPlayerSlots() { return transferPlayerSlots; }
    public List<ButtonConfig> getTransferButtons() { return transferButtons; }

    public String getWithdrawTitle() { return withdrawTitle; }
    public int getWithdrawSize() { return withdrawSize; }
    public List<ButtonConfig> getWithdrawButtons() { return withdrawButtons; }
    public String getCurrencyName() { return currencyName; }
    public String getNoPermission() { return noPermission; }
    public String getInvalidAmount() { return invalidAmount; }
    public String getInsufficientFunds() { return insufficientFunds; }
    public String getOnlyCurrency() { return onlyCurrency; }
    public String getPlayerNotFound() { return playerNotFound; }
    public String getNoSlots() {return noSlots;}
    public String getActionConfirmed() { return actionConfirmed; }
    public String getPutValute() { return putValute; }
    public String getNoSelectedPlayer() { return noSelectedPlayer; }

    public String getChosedPlayer(String player) { return chosedPlayer.replace("{player}", player); }

    public String getChosedValuteSumm(double amount) {
        return chosedValuteSumm.replace("{amount}", String.valueOf(amount)).replace("{currency}", currencyName);
    }

    public String getDeposited(double amount) {
        return deposited.replace("{amount}", String.valueOf(amount)).replace("{currency}", currencyName);
    }

    public String getWithdrawn(double amount, double balance) {
        return withdrawn.replace("{amount}", String.valueOf(amount)).replace("{currency}", currencyName).replace("{balance}", String.valueOf(balance));
    }

    public String getBalance(double amount) {
        return balanceMsg.replace("{balance}", String.valueOf(amount)).replace("{currency}", currencyName);
    }

    public String getTransferSuccess(double amount, String player) {
        return transferSuccess.replace("{amount}", String.valueOf(amount))
                .replace("{currency}", currencyName)
                .replace("{player}", player);
    }

    public String getTransferReceived(double amount, String player) {
        return transferReceived.replace("{amount}", String.valueOf(amount))
                .replace("{currency}", currencyName)
                .replace("{player}", player);
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        loadConfig();
    }
}