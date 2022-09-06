package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

class RemoveUserFromGroupCommand implements Command<Object> {

    private final LegendPerm plugin;
    private final MessageConfig messages;

    public RemoveUserFromGroupCommand(LegendPerm plugin, MessageConfig messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public int run(@NotNull CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();
        if (!sender.hasPermission("legendperm.user.removegroup")) {
            sender.sendMessage(messages.notPermitted.get());
            return 1;
        }

        String userName = context.getArgument("userName", String.class);
        String groupName = context.getArgument("groupName", String.class);
        String time = getArgumentOrDefault(context, "time", String.class, null);

        Map<String, Object> replacements = new HashMap<>();
        replacements.put("user_name", userName);
        replacements.put("group_name", groupName);
        replacements.put("time", time);

        try {
            CacheService cacheService = plugin.getServiceRegistry().get(CacheService.class);
            Optional<User> cacheResult = cacheService.get(UserCache.class)
                    .get(userName);
            // check if user exists
            if (cacheResult.isEmpty()) {
                sender.sendMessage(messages.unknownUser.parse(replacements));
                return 1;
            }
            User user = cacheResult.get();

            // check if user has group
            if (!user.hasGroup(groupName)) {
                sender.sendMessage(messages.userDoesNotHaveGroup.parse(replacements));
                return 1;
            }

            Group group = cacheService.get(GroupCache.class)
                    .get(groupName)
                    .orElseThrow();

            UserRepository userRepository = plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(UserRepository.class);
            // if group get permanently removed, there is no need to check for time
            //  if time is given, we need to check if group is temporary
            if (time == null) {
                userRepository.removeUserFromGroup(user.getUuid(), group.getName());
                user.removeGroup(group);
                sender.sendMessage(messages.userRemovedFromGroup.parse(replacements));
                return 0;
            }

            if (user.hasGroupPermanent(groupName)) {
                sender.sendMessage(messages.userCantReduceGroupAsPermanent.parse(replacements));
                return 0;
            }

            // get valid until reduced by the removed time
            LocalDateTime validUntilDT = TimeParser.localDateTimeFromEpochSeconds(user.getGroupValidUntil(groupName));
            LocalDateTime now = LocalDateTime.now();
            long seconds = new TimeParser(time, now).getInSeconds();
            LocalDateTime newValidUntilDT = validUntilDT.minus(seconds, ChronoUnit.SECONDS);
            if (newValidUntilDT.isBefore(now)) {
                userRepository.removeUserFromGroup(user.getUuid(), group.getName());
                user.removeGroup(group);
                sender.sendMessage(messages.userRemovedFromGroup.parse(replacements));
                return 0;
            }
            long validUntil = user.getGroupValidUntil(groupName) - seconds;

            // save to database
            userRepository.addUserToGroup(user.getUuid(), group.getName(), validUntil);
            user.updateValidUntil(group, validUntil);
            replacements.put("time_reduced", TimeParser.epochSecondsToInline(validUntil));
            sender.sendMessage(messages.userGroupTimeReduced.parse(replacements));
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
