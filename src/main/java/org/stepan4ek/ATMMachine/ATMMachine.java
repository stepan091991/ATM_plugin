package org.stepan4ek.ATMMachine;

import org.stepan4ek.ATMMachine.data.DataManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ATMMachine extends JavaPlugin {
    private static ATMMachine instance;

    @Override
    public void onEnable() {
        instance = this;

        DataManager.getInstance();

        getLogger().info("§aATMMachine enabled!");
    }

    @Override
    public void onDisable() {
        DataManager.getInstance().saveAll();
        getLogger().info("§cATMMachine disabled!");
    }

    public static ATMMachine getInstance() {
        return instance;
    }
}