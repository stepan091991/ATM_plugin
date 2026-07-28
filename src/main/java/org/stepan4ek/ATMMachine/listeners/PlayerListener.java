package org.stepan4ek.ATMMachine.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.stepan4ek.ATMMachine.data.DataManager;

public class PlayerListener implements Listener {
    private final DataManager dataManager = DataManager.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        dataManager.getOrCreate(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        dataManager.removeFromCache(event.getPlayer());
    }
}