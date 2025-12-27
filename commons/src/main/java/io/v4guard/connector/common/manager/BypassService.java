package io.v4guard.connector.common.manager;

import java.util.List;
import java.util.UUID;

public class BypassService {

    private final BypassStorage storage;

    public BypassService(BypassStorage storage) {
        this.storage = storage;
    }

    public boolean addBypass(UUID uuid) {
        return storage.addUuid(uuid);
    }

    public boolean removeBypass(UUID uuid) {
        return storage.removeUuid(uuid);
    }

    public List<UUID> listBypasses() {
        return storage.getBypassedUUIDs();
    }

    public String listBypassesString() {
        List<UUID> list = listBypasses();
        if (list.isEmpty()) return "No bypass entries found.";
        return String.join(", ", list.stream().map(UUID::toString).toArray(String[]::new));
    }
}

