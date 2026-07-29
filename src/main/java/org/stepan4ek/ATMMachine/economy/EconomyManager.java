package org.stepan4ek.ATMMachine.economy;

import org.bukkit.entity.Player;
import org.stepan4ek.ATMMachine.data.DataManager;
import org.stepan4ek.ATMMachine.data.PlayerData;

public class EconomyManager {
    private static EconomyManager instance;
    private final DataManager dataManager;

    private EconomyManager() {
        this.dataManager = DataManager.getInstance();
    }

    public static EconomyManager getInstance() {
        if (instance == null) {
            instance = new EconomyManager();
        }
        return instance;
    }

    public double getBalance(Player player) {
        return dataManager.getOrCreate(player).getBalance();
    }

    public void deposit(Player player, double amount) {
        if (amount <= 0) return;
        PlayerData data = dataManager.getOrCreate(player);
        data.addBalance(amount);
        dataManager.save(data);
    }

    public boolean withdraw(Player player, double amount) {
        if (amount <= 0) return false;
        PlayerData data = dataManager.getOrCreate(player);
        if (!data.hasEnough(amount)) return false;
        data.removeBalance(amount);
        dataManager.save(data);
        return true;
    }

    public boolean transfer(Player from, Player to, double amount) {
        if (amount <= 0 || from.equals(to)) return false;

        PlayerData fromData = dataManager.getOrCreate(from);
        PlayerData toData = dataManager.getOrCreate(to);

        if (!fromData.hasEnough(amount)) return false;

        fromData.removeBalance(amount);
        toData.addBalance(amount);

        dataManager.save(fromData);
        dataManager.save(toData);
        return true;
    }

    public void setBalance(Player player, double amount) {
        if (amount < 0) amount = 0;

        PlayerData data = dataManager.getOrCreate(player);
        data.setBalance(amount);
        dataManager.save(data);
    }
}