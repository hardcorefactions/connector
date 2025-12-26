package io.v4guard.connector.platform.bungee.command.sub;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import io.v4guard.connector.platform.bungee.BungeeInstance;
import io.v4guard.connector.platform.bungee.manager.BypassManager;
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

    private final BungeeInstance plugin;
    private final BypassManager manager;

    private final List<String> helpMessage = List.of(
            "§d▲ §lV4GUARD §7Correct usage: /v4guard bypass add <username|uuid>",
            "§d▲ §lV4GUARD §7Correct usage: /v4guard bypass remove <username|uuid>",
            "§d▲ §lV4GUARD §7Use /v4guard bypass list to see current bypassed UUIDs"
    );

    public BypassCommand() {
        this.plugin = BungeeInstance.get();
        this.manager = new BypassManager(plugin);
    }

    @Command(names = "")
    public void help(@Sender CommandSender source) {
        helpMessage.forEach(line -> source.sendMessage(new ComponentBuilder(line).create()));
    }

    @Command(names = "list")
    public void list(@Sender CommandSender source) {
        String out = manager.listBypassesString();
        source.sendMessage(new ComponentBuilder("§d▲ §lV4GUARD §7" + out).create());
    }

    @Command(names = "add")
    public void addBypass(@Sender CommandSender source,
                          @Suggestions(suggestions = {"<username>", "<uuid>"}) String value
    ) {
        ProxiedPlayer target = plugin.getProxy().getPlayer(value);
        if (target != null) {
            UUID uuid = target.getUniqueId();
            String result = manager.addBypass(uuid);
            source.sendMessage(new ComponentBuilder("§a" + result).create());
            return;
        }

        try {
            UUID uuid = UUID.fromString(value);
            String result = manager.addBypass(uuid);
            source.sendMessage(new ComponentBuilder("§a" + result).create());
            return;
        } catch (IllegalArgumentException ignored) {
        }

        source.sendMessage(new ComponentBuilder("§eResolving username to UUID...").create());
        CompletableFuture.runAsync(() -> {
            try {
                UUID uuid = resolveUsernameToUUID(value);
                if (uuid == null) {
                    source.sendMessage(new ComponentBuilder("§cCould not resolve username to UUID.").create());
                    return;
                }

                String result = manager.addBypass(uuid);
                source.sendMessage(new ComponentBuilder((result.startsWith("Bypass") ? "§a" : "§c") + result).create());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error resolving username to UUID", e);
                source.sendMessage(new ComponentBuilder("§cAn error occurred while resolving the username.").create());
            }
        });
    }

    @Command(names = "remove")
    public void removeBypass(@Sender CommandSender source, @Suggestions(suggestions = "<username|uuid>") String value) {
        ProxiedPlayer target = plugin.getProxy().getPlayer(value);
        if (target != null) {
            UUID uuid = target.getUniqueId();
            String result = manager.removeBypass(uuid);
            source.sendMessage(new ComponentBuilder((result.startsWith("Bypass") ? "§a" : "§c") + result).create());
            return;
        }

        try {
            UUID uuid = UUID.fromString(value);
            String result = manager.removeBypass(uuid);
            source.sendMessage(new ComponentBuilder((result.startsWith("Bypass") ? "§a" : "§c") + result).create());
            return;
        } catch (IllegalArgumentException ignored) {
        }

        source.sendMessage(new ComponentBuilder("§eResolving username to UUID...").create());
        CompletableFuture.runAsync(() -> {
            try {
                UUID uuid = resolveUsernameToUUID(value);
                if (uuid == null) {
                    source.sendMessage(new ComponentBuilder("§cCould not resolve username to UUID.").create());
                    return;
                }

                String result = manager.removeBypass(uuid);
                source.sendMessage(new ComponentBuilder((result.startsWith("Bypass") ? "§a" : "§c") + result).create());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error resolving username to UUID", e);
                source.sendMessage(new ComponentBuilder("§cAn error occurred while resolving the username.").create());
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

