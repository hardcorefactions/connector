package io.v4guard.connector.platform.velocity.manager;

import io.v4guard.connector.platform.velocity.VelocityInstance;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public class StorageManager {

    private CommentedConfigurationNode storageConfig;

    public StorageManager(VelocityInstance plugin) {
        plugin.getLogger().info("StorageManager initialized.");
        plugin.getDataFolder().mkdir();

        File file = new File(plugin.getDataFolder(), "storage.yml");

        if (!file.exists()) {
            try {
                InputStream stream = getClass().getClassLoader().getResourceAsStream("storage.yml");

                if (stream != null) {
                    file.createNewFile();
                    Files.copy(stream, file.toPath());
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Could not create storage.yml file!");
            }
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .file(file)
                .build();

        try {
            storageConfig = loader.load();
        } catch (Exception e) {
            plugin.getLogger().severe("Could not load storage.yml file!");
        }
    }

    public CommentedConfigurationNode getStorageConfig() {
        return storageConfig;
    }
}
