package io.v4guard.connector.common.manager;

import java.util.List;
import java.util.UUID;

public class CommonBypassManager {

    private final BypassService service;

    public CommonBypassManager(BypassStorage storage) {
        this.service = new BypassService(storage);
    }

    public boolean add(UUID uuid) {
        return service.addBypass(uuid);
    }

    public boolean remove(UUID uuid) {
        return service.removeBypass(uuid);
    }

    public List<UUID> list() {
        return service.listBypasses();
    }

    public String listString() {
        return service.listBypassesString();
    }
}
