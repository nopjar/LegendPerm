package net.playlegend.command;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.UserCache;
import net.playlegend.configuration.MessageConfig;
import net.playlegend.domain.Group;
import net.playlegend.domain.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

class InfoCommand implements Command<Object> {

    private final LegendPerm plugin;
    private final MessageConfig messages;

    public InfoCommand(LegendPerm plugin, MessageConfig messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public int run(@NotNull CommandContext<Object> context) throws CommandSyntaxException {
        if (!(context.getSource() instanceof final Player player)) {
            ((CommandSender) context.getSource()).sendMessage(messages.onlyPlayer.get());
            return 1;
        }

        try {
            Optional<User> cacheResult = plugin.getServiceRegistry().get(CacheService.class)
                    .get(UserCache.class)
                    .get(player.getUniqueId());

            if (cacheResult.isEmpty())
                throw new NullPointerException("user must not be null");

            User user = cacheResult.get();
            Map<String, Object> replacements = new HashMap<>();
            replacements.put("user_name", user.getName());
            replacements.put("user_uuid", user.getUuid());
            replacements.put("groups", String.join("\n", buildGroups(user.getGroups())));

            player.sendMessage(messages.userInfo.parse(replacements));
        } catch (ExecutionException e) {
            e.printStackTrace();
            player.sendMessage(messages.unexpectedError.get());
        }

        return 1;
    }

    private List<String> buildGroups(ImmutableMap<Group, Long> groups) {
        if (groups.isEmpty()) return Collections.emptyList();

        List<String> strings = new ArrayList<>(groups.size());
        for (Map.Entry<Group, Long> entry : groups.entrySet()) {
            if (entry.getValue() == 0L) {
                strings.add(messages.userInfoGroupLinePermanent.parse(Map.of("group_name", entry.getKey().getName())));
            } else {
                String pattern = ZonedDateTime.ofInstant(Instant.ofEpochSecond(entry.getValue()), ZoneId.systemDefault())
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                strings.add(messages.userInfoGroupLineTemporary.parse(Map.of("group_name", entry.getKey().getName(), "group_duration", pattern)));
            }
        }
        return strings;
    }


}
