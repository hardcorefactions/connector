package io.v4guard.connector.common.manager;

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

public class BypassStorage {

    private final File file;
    private final YamlConfigurationLoader loader;
    private CommentedConfigurationNode storageConfig;
    private final Object lock = new Object();

    public BypassStorage(File dataFolder) {
        if (!dataFolder.exists()) dataFolder.mkdirs();

        this.file = new File(dataFolder, "storage.yml");

        if (!file.exists()) {
            try (InputStream stream = getClass().getResourceAsStream("/defaults/storage.yml")) {
                if (stream != null) Files.copy(stream, file.toPath());
                else file.createNewFile();
            } catch (Exception e) {
                System.err.println("Could not create storage.yml: " + e.getMessage());
            }
        }

        this.loader = YamlConfigurationLoader.builder().file(file).build();

        try {
            this.storageConfig = loader.load();
        } catch (Exception e) {
            System.err.println("Could not load storage.yml: " + e.getMessage());
            this.storageConfig = loader.createNode();
        }
    }

    public List<UUID> getBypassedUUIDs() {
        synchronized (lock) {
            try {
                List<UUID> list = storageConfig.node("uuids").getList(UUID.class);
                return list == null ? new ArrayList<>() : new ArrayList<>(list);
            } catch (SerializationException e) {
                System.err.println("Failed to read uuids: " + e.getMessage());
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
                System.err.println("Failed to add uuid: " + e.getMessage());
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
                System.err.println("Failed to remove uuid: " + e.getMessage());
                return false;
            }
        }
    }

    public void save() {
        synchronized (lock) {
            try { loader.save(storageConfig); }
            catch (Exception e) { System.err.println("Failed to save storage.yml: " + e.getMessage()); }
        }
    }

    public void saveAsync() {
        CompletableFuture.runAsync(this::save);
    }

    public File getFile() { return file; }
}

