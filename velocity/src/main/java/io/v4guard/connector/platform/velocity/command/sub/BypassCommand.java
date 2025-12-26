package io.v4guard.connector.platform.velocity.command.sub;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.command.internal.annotations.CommandFlag;
import io.v4guard.connector.common.request.WhitelistRequest;
import io.v4guard.connector.common.utils.StringUtils;
import io.v4guard.connector.platform.velocity.VelocityInstance;
import io.v4guard.connector.platform.velocity.manager.BypassManager;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Command(names = { "bypass" }, permission = "v4guard.command.bypass")
public class BypassCommand implements CommandClass {

    private final VelocityInstance plugin;

    private final List<String> helpMessage = List.of(
            "§d▲ §lV4GUARD §7Correct usage: /v4guard bypass add <username|uuid>",
            "§d▲ §lV4GUARD §7Correct usage: /v4guard bypass remove <username|uuid>"
    );

    public BypassCommand(VelocityInstance plugin) {
        this.plugin = plugin;
    }

    @Command(names = "")
    public void help(@Sender CommandSource source) {
        helpMessage.forEach(line -> source.sendMessage(plugin.getLegacyComponentSerializer().deserialize(line)));
    }

    @Command(names = "add")
    public void addBypass(@Sender CommandSource source,
                             @Suggestions(suggestions = {"<username>", "<uuid>"}) String value
    ) {
        Player target = plugin.getServer().getPlayer(value).orElse(null);
        UUID identifier = UUID.fromString(target != null ? target.getUniqueId().toString() : value);

        String result = BypassManager.addBypass(identifier);
        source.sendMessage(plugin.getLegacyComponentSerializer().deserialize(result));
    }

    @Command(names = "remove")
    public void removeBypass(@Sender CommandSource source, @Suggestions(suggestions = "<username|uuid>") String value) {
        Player target = plugin.getServer().getPlayer(value).orElse(null);
        UUID identifier = UUID.fromString(target != null ? target.getUniqueId().toString() : value);

        String result = BypassManager.removeBypass(identifier);
        source.sendMessage(plugin.getLegacyComponentSerializer().deserialize(result));
    }

}