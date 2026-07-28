package org.stepan4ek.ATMMachine.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.stepan4ek.ATMMachine.ATMMachine;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    private static DataManager instance;
    private final Map<UUID, PlayerData> cache;
    private final Gson gson;
    private final File dataFolder;

    private DataManager() {
        this.cache = new HashMap<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataFolder = new File(ATMMachine.getInstance().getDataFolder(), "playerdata");
        if (!dataFolder.exists()) dataFolder.mkdirs();
    }

    public static DataManager getInstance() {
        if (instance == null) instance = new DataManager();
        return instance;
    }

    // Get or create player data file
    public PlayerData getOrCreate(Player player) {
        UUID uuid = player.getUniqueId();

        if (cache.containsKey(uuid)) {
            cache.get(uuid).setPlayerName(player.getName());
            return cache.get(uuid);
        }

        File file = new File(dataFolder, uuid + ".json");
        PlayerData data;

        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                data = gson.fromJson(reader, PlayerData.class);
                data.setPlayerName(player.getName());
            } catch (IOException e) {
                data = new PlayerData(uuid, player.getName());
            }
        } else {
            data = new PlayerData(uuid, player.getName());
        }

        cache.put(uuid, data);
        return data;
    }

    // Save player data to file
    public void save(PlayerData data) {
        File file = new File(dataFolder, data.getUuid() + ".json");
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            ATMMachine.getInstance().getLogger().warning("Failed to save data for " + data.getPlayerName());
        }
    }

    // Save all players data to files
    public void saveAll() {
        cache.values().forEach(this::save);
    }

    public void removeFromCache(Player player) {
        cache.remove(player.getUniqueId());
    }
}