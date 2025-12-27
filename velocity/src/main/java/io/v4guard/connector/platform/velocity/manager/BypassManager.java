package io.v4guard.connector.platform.velocity.manager;

import io.v4guard.connector.common.manager.CommonBypassManager;
import io.v4guard.connector.platform.velocity.VelocityInstance;

import java.util.List;
import java.util.UUID;

public class BypassManager {

    private final CommonBypassManager commonManager;

    public BypassManager(VelocityInstance instance) {
        this.commonManager = new CommonBypassManager(instance.getStorageManager().getStorage());
    }

    public boolean isInitialized() {
        return this.commonManager != null;
    }

    public String addBypass(UUID uuid) {
        if (!isInitialized()) return "Storage not initialized.\nPlease report to the server administrator.";
        boolean added = commonManager.add(uuid);
        return added ? "Bypass added successfully." : "This UUID already has a bypass.";
    }

    public String removeBypass(UUID uuid) {
        if (!isInitialized()) return "Storage not initialized.\nPlease report to the server administrator.";
        boolean removed = commonManager.remove(uuid);
        return removed ? "Bypass removed successfully." : "This UUID doesn't have a bypass.";
    }

    public List<UUID> listBypasses() {
        if (!isInitialized()) return List.of();
        return commonManager.list();
    }

    public String listBypassesString() {
        return commonManager.listString();
    }
}
