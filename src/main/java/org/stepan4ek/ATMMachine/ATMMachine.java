package org.stepan4ek.ATMMachine;

import org.bukkit.plugin.java.JavaPlugin;
import org.stepan4ek.ATMMachine.commands.Commands;
import org.stepan4ek.ATMMachine.data.ConfigManager;
import org.stepan4ek.ATMMachine.data.DataManager;
import org.stepan4ek.ATMMachine.listeners.GUIListener;
import org.stepan4ek.ATMMachine.listeners.PlayerListener;

public class ATMMachine extends JavaPlugin {
    private static ATMMachine instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        ConfigManager.getInstance();
        DataManager.getInstance();

        getCommand("atm").setExecutor(new Commands());
        getCommand("bank").setExecutor(new Commands());

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);

        getLogger().info("ATMMachine enabled!");
    }

    @Override
    public void onDisable() {
        DataManager.getInstance().saveAll();
        getLogger().info("ATMMachine disabled!");
    }

    public static ATMMachine getInstance() {
        return instance;
    }
}