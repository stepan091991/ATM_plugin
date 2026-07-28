package org.stepan4ek.ATMMachine;

import org.stepan4ek.ATMMachine.commands.Commands;
import org.stepan4ek.ATMMachine.data.DataManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.stepan4ek.ATMMachine.listeners.GUIListener;

public class ATMMachine extends JavaPlugin {
    private static ATMMachine instance;

    @Override
    public void onEnable() {
        instance = this;

        DataManager.getInstance();

        getCommand("atm").setExecutor(new Commands());
        getCommand("bank").setExecutor(new Commands());

        getServer().getPluginManager().registerEvents(new GUIListener(), this);

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