package io.v4guard.connector.platform.velocity.manager;

import io.v4guard.connector.platform.velocity.VelocityInstance;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class StorageManager {

    private final VelocityInstance plugin;
    private final YamlConfigurationLoader loader;
    private CommentedConfigurationNode storageConfig;
    private final Object lock = new Object();

    public StorageManager(VelocityInstance plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("StorageManager initialized.");

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            boolean ok = dataFolder.mkdirs();
            if (!ok) plugin.getLogger().warning("Could not create data folder: " + dataFolder.getAbsolutePath());
        }

        File file = new File(dataFolder, "storage.yml");
        if (!file.exists()) {
            try (InputStream stream = getClass().getResourceAsStream("/defaults/storage.yml")) {
                if (stream != null) {
                    Files.copy(stream, file.toPath());
                } else {
                    boolean created = file.createNewFile();
                    if (!created) plugin.getLogger().warning("Could not create storage.yml file at " + file.getAbsolutePath());
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create storage.yml file!", e);
            }
        }

        this.loader = YamlConfigurationLoader.builder()
                .file(file)
                .build();

        try {
            this.storageConfig = loader.load();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load storage.yml file!", e);
            this.storageConfig = loader.createNode();
        }
    }

    public List<UUID> getBypassedUUIDs() {
        synchronized (lock) {
            try {
                List<UUID> list = storageConfig.node("uuids").getList(UUID.class);
                return list == null ? new ArrayList<>() : new ArrayList<>(list);
            } catch (SerializationException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to read uuids from storage.yml", e);
                return new ArrayList<>();
            }
        }
    }

    public boolean addUuid(UUID uuid) {
        synchronized (lock) {
            try {
                List<UUID> list = storageConfig.node("uuids").getList(UUID.class);
                if (list == null) list = new ArrayList<>();
                if (list.contains(uuid)) return false;
                list.add(uuid);
                storageConfig.node("uuids").setList(UUID.class, list);
                saveAsync();
                return true;
            } catch (SerializationException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to add uuid to storage.yml", e);
                return false;
            }
        }
    }

    public boolean removeUuid(UUID uuid) {
        synchronized (lock) {
            try {
                List<UUID> list = storageConfig.node("uuids").getList(UUID.class);
                if (list == null || !list.contains(uuid)) return false;
                list.remove(uuid);
                storageConfig.node("uuids").setList(UUID.class, list);
                saveAsync();
                return true;
            } catch (SerializationException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to remove uuid from storage.yml", e);
                return false;
            }
        }
    }

    public void save() {
        synchronized (lock) {
            try {
                loader.save(storageConfig);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save storage.yml", e);
            }
        }
    }

    public void saveAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                save();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Async save failed", e);
            }
        });
    }
}
