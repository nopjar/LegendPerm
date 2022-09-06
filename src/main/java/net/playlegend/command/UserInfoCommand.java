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
import org.jetbrains.annotations.NotNull;

class UserInfoCommand implements Command<Object> {

    private final LegendPerm plugin;
    private final MessageConfig messages;

    public UserInfoCommand(LegendPerm plugin, MessageConfig messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public int run(@NotNull CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();
        if (!sender.hasPermission("legendperm.user.infoother")) {
            sender.sendMessage(messages.notPermitted.get());
            return 1;
        }

        String userName = context.getArgument("userName", String.class);
        Map<String, Object> replacements = new HashMap<>();
        replacements.put("user_name", userName);

        try {
            Optional<User> cacheResult = plugin.getServiceRegistry().get(CacheService.class)
                    .get(UserCache.class)
                    .get(userName);
            if (cacheResult.isEmpty()) {
                sender.sendMessage(messages.unknownUser.parse(replacements));
                return 0;
            }

            User user = cacheResult.get();
            replacements.put("user_name", user.getName());
            replacements.put("user_uuid", user.getUuid());
            replacements.put("groups", String.join("\n", buildGroups(user.getGroups())));
            sender.sendMessage(messages.userInfo.parse(replacements));
        } catch (ExecutionException e) {
            e.printStackTrace();
            sender.sendMessage(messages.unexpectedError.parse(replacements));
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
