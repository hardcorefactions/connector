package io.v4guard.connector.platform.velocity.manager;

import io.v4guard.connector.platform.velocity.VelocityInstance;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.internal.snakeyaml.nodes.Node;

import java.util.List;
import java.util.UUID;

public class BypassManager {

    // prevent initialization
    private BypassManager() { }

    public static String addBypass(UUID uuid) {
        List<UUID> uuids = getBypassedUUIDs();

        if (uuids == null) {
            return "&cUUID list is null! Please report this to the developer of the fork.";
        }

        if (uuids.contains(uuid)) {
            return "&cThis UUID already has a bypass.";
        }

        uuids.add(uuid);
        return "&aBypass added successfully.";
    }

    public static String removeBypass(UUID uuid) {
        List<UUID> uuids = getBypassedUUIDs();

        if (uuids == null) {
            return "&cUUID list is null! Please report this to the developer of the fork.";
        }

        if (!uuids.contains(uuid)) {
            return "&cThis UUID doesn't have a bypass.";
        }

        uuids.add(uuid);
        return "&aBypass removed successfully.";
    }

    private static List<UUID> getBypassedUUIDs() {
        try {
            CommentedConfigurationNode storage = VelocityInstance.get().getStorageManager().getStorageConfig();
            return storage.node("uuids").getList(UUID.class);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }
}
