package io.v4guard.connector.platform.bungee.manager;

import io.v4guard.connector.platform.bungee.BungeeInstance;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BypassManager {

    private final StorageManager storage;

    public BypassManager(BungeeInstance instance) {
        this.storage = instance.getStorageManager();
    }

    public boolean isInitialized() {
        return this.storage != null;
    }

    public String addBypass(UUID uuid) {
        if (!isInitialized()) return "Storage not initialized.\nPlease report to the server administrator.";
        boolean added = storage.addUuid(uuid);
        return added ? "Bypass added successfully." : "This UUID already has a bypass.";
    }

    public String removeBypass(UUID uuid) {
        if (!isInitialized()) return "Storage not initialized.\nPlease report to the server administrator.";
        boolean removed = storage.removeUuid(uuid);
        return removed ? "Bypass removed successfully." : "This UUID doesn't have a bypass.";
    }

    public List<UUID> listBypasses() {
        if (!isInitialized()) return List.of();
        return storage.getBypassedUUIDs();
    }

    public String listBypassesString() {
        List<UUID> list = listBypasses();
        if (list.isEmpty()) return "No bypass entries found.";
        return list.stream().map(UUID::toString).collect(Collectors.joining(", "));
    }
}
