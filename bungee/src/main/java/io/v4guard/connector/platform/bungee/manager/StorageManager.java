package io.v4guard.connector.platform.bungee.manager;

import io.v4guard.connector.platform.bungee.BungeeInstance;
import io.v4guard.connector.common.manager.BypassStorage;

import java.util.List;
import java.util.UUID;

public class StorageManager {

    private final BungeeInstance plugin;
    private final BypassStorage storage;

    public StorageManager(BungeeInstance plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("StorageManager initialized.");
        this.storage = new BypassStorage(plugin.getDataFolder());
    }

    public List<UUID> getBypassedUUIDs() {
        return storage.getBypassedUUIDs();
    }

    public boolean addUuid(UUID uuid) {
        return storage.addUuid(uuid);
    }

    public boolean removeUuid(UUID uuid) {
        return storage.removeUuid(uuid);
    }

    public BypassStorage getStorage() {
        return storage;
    }
}
