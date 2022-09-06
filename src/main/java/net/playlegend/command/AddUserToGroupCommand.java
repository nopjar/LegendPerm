package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.GroupCache;
import net.playlegend.cache.UserCache;
import net.playlegend.configuration.MessageConfig;
import net.playlegend.domain.Group;
import net.playlegend.domain.User;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.UserRepository;
import net.playlegend.time.TimeParser;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

class AddUserToGroupCommand implements Command<Object> {

    private final LegendPerm plugin;
    private final MessageConfig messages;

    public AddUserToGroupCommand(LegendPerm plugin, MessageConfig messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public int run(@NotNull CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();
        if (!sender.hasPermission("legendperm.user.addgroup")) {
            sender.sendMessage(messages.notPermitted.get());
            return 1;
        }

        String userName = context.getArgument("userName", String.class);
        String groupName = context.getArgument("groupName", String.class);
        String time = getArgumentOrDefault(context, "time", String.class, null);
        Map<String, Object> replacements = new HashMap<>();
        replacements.put("user_name", userName);
        replacements.put("group_name", groupName);

        try {
            CacheService cacheService = plugin.getServiceRegistry().get(CacheService.class);
            UserRepository userRepository = plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(UserRepository.class);

            // check if user exists
            Optional<User> userCacheResult = cacheService.get(UserCache.class)
                    .get(userName);
            if (userCacheResult.isEmpty()) {
                sender.sendMessage(messages.unknownUser.parse(replacements));
                return 1;
            }
            User user = userCacheResult.get();

            // check if user has any time remaining in this group or is already permanently in this
            //  group.
            long currentTime = 0;
            if (user.hasGroup(groupName)) {
                if (user.hasGroupPermanent(groupName)) {
                    sender.sendMessage(messages.userAlreadyPermanentInGroup.parse(replacements));
                    return 1;
                } else {
                    currentTime = user.getGroupValidUntil(groupName);
                }
            }

            GroupCache groupCache = cacheService.get(GroupCache.class);
            Optional<Group> groupCacheResult = groupCache.get(groupName);

            // check for existence of group to give
            // this checks the case that the user just didn't have the group
            if (groupCacheResult.isEmpty()) {
                sender.sendMessage(messages.groupDoesNotExist.parse(replacements));
                return 1;
            }

            Group group = groupCacheResult.get();

            // calculate time in group (if time == null, the group is permanent)
            //  setting validUntil to 0 in the database causes the group to be permanent
            long validUntil = 0;
            if (time != null) {
                LocalDateTime base = currentTime == 0 ?
                        LocalDateTime.now() :
                        TimeParser.localDateTimeFromEpochSeconds(currentTime);

                TimeParser timeParser = new TimeParser(time, base);
                validUntil = timeParser.parseToEpochSeconds();
            }

            // save to database
            userRepository.addUserToGroup(user.getUuid(), group.getName(), validUntil);
            user.addGroup(group, validUntil);
            if (validUntil == 0) {
                sender.sendMessage(messages.userAddedPermanentToGroup.parse(replacements));
            } else {
                replacements.put("time", TimeParser.localDateTimeFromEpochSeconds(validUntil).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                sender.sendMessage(messages.userAddedTemporaryToGroup.parse(replacements));
            }
        } catch (SQLException | ExecutionException e) {
            e.printStackTrace();
            sender.sendMessage(messages.unexpectedError.parse(replacements));
        }

        return 1;
    }

    private <T> T getArgumentOrDefault(CommandContext<Object> context, String name, Class<T> type, T def) {
        try {
            return context.getArgument(name, type);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().startsWith("No such argument")) {
                return def;
            }

            throw e;
        }
    }

}
