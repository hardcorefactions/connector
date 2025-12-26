package io.v4guard.connector.platform.velocity.command.sub;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.connector.platform.velocity.VelocityInstance;
import io.v4guard.connector.platform.velocity.manager.BypassManager;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@SuppressWarnings({"unused"})
@Command(names = { "bypass" }, permission = "v4guard.command.bypass")
public class BypassCommand implements CommandClass {

    private final VelocityInstance plugin;
    private final BypassManager manager;

    private final List<String> helpMessage = List.of(
            "§d▲ §lV4GUARD §7Correct usage: /v4guard bypass add <username|uuid>",
            "§d▲ §lV4GUARD §7Correct usage: /v4guard bypass remove <username|uuid>",
            "§d▲ §lV4GUARD §7Use /v4guard bypass list to see current bypassed UUIDs"
    );

    public BypassCommand(VelocityInstance plugin) {
        this.plugin = plugin;
        this.manager = new BypassManager(plugin);
    }

    @Command(names = "")
    public void help(@Sender CommandSource source) {
        helpMessage.forEach(line -> source.sendMessage(plugin.getLegacyComponentSerializer().deserialize(line)));
    }

    @Command(names = "list")
    public void list(@Sender CommandSource source) {
        String out = manager.listBypassesString();
        source.sendMessage(plugin.getLegacyComponentSerializer().deserialize("§d▲ §lV4GUARD §7" + out));
    }

    @Command(names = "add")
    public void addBypass(@Sender CommandSource source,
                          @Suggestions(suggestions = {"<username>", "<uuid>"}) String value
    ) {
        try {
            UUID uuid = UUID.fromString(value);
            String result = manager.addBypass(uuid);
            source.sendMessage(plugin.getLegacyComponentSerializer().deserialize("§a" + result));
            return;
        } catch (IllegalArgumentException ignored) {
        }

        source.sendMessage(plugin.getLegacyComponentSerializer().deserialize("§eResolving username to UUID..."));
        CompletableFuture.runAsync(() -> {
            try {
                UUID uuid = resolveUsernameToUUID(value);
                if (uuid == null) {
                    source.sendMessage(plugin.getLegacyComponentSerializer().deserialize("§cCould not resolve username to UUID."));
                    return;
                }

                String result = manager.addBypass(uuid);
                source.sendMessage(plugin.getLegacyComponentSerializer().deserialize(result.startsWith("Bypass") ? "§a" + result : "§c" + result));
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error resolving username to UUID", e);
                source.sendMessage(plugin.getLegacyComponentSerializer().deserialize("§cAn error occurred while resolving the username."));
            }
        });
    }

    @Command(names = "remove")
    public void removeBypass(@Sender CommandSource source, @Suggestions(suggestions = "<username|uuid>") String value) {
        Player target = plugin.getServer().getPlayer(value).orElse(null);
        if (target != null) {
            UUID uuid = target.getUniqueId();
            String result = manager.removeBypass(uuid);
            source.sendMessage(plugin.getLegacyComponentSerializer().deserialize(result.startsWith("Bypass") ? "§a" + result : "§c" + result));
            return;
        }

        try {
            UUID uuid = UUID.fromString(value);
            String result = manager.removeBypass(uuid);
            source.sendMessage(plugin.getLegacyComponentSerializer().deserialize(result.startsWith("Bypass") ? "§a" + result : "§c" + result));
            return;
        } catch (IllegalArgumentException ignored) {
        }

        source.sendMessage(plugin.getLegacyComponentSerializer().deserialize("§eResolving username to UUID..."));
        CompletableFuture.runAsync(() -> {
            try {
                UUID uuid = resolveUsernameToUUID(value);
                if (uuid == null) {
                    source.sendMessage(plugin.getLegacyComponentSerializer().deserialize("§cCould not resolve username to UUID."));
                    return;
                }

                String result = manager.removeBypass(uuid);
                source.sendMessage(plugin.getLegacyComponentSerializer().deserialize(result.startsWith("Bypass") ? "§a" + result : "§c" + result));
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error resolving username to UUID", e);
                source.sendMessage(plugin.getLegacyComponentSerializer().deserialize("§cAn error occurred while resolving the username."));
            }
        });
    }

    private UUID resolveUsernameToUUID(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            int code = conn.getResponseCode();
            if (code != 200) return null;

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                String body = sb.toString();

                int idIndex = body.indexOf("\"id\":\"");
                if (idIndex == -1) return null;
                int start = idIndex + 6;
                int firstQuote = body.indexOf('"', start);
                if (firstQuote == -1) return null;
                int secondQuote = body.indexOf('"', firstQuote + 1);
                if (secondQuote == -1) return null;
                String id = body.substring(firstQuote + 1, secondQuote);

                StringBuilder uuidSb = new StringBuilder(id);
                uuidSb.insert(20, '-');
                uuidSb.insert(16, '-');
                uuidSb.insert(12, '-');
                uuidSb.insert(8, '-');
                return UUID.fromString(uuidSb.toString());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to resolve username via Mojang API", e);
            return null;
        }
    }
}