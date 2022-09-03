package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.GroupCache;
import net.playlegend.cache.UserCache;
import net.playlegend.domain.Group;
import net.playlegend.domain.User;
import net.playlegend.misc.GroupWeightComparator;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.UserRepository;
import net.playlegend.time.TimeParser;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

class AddUserToGroupCommand implements Command<Object> {

    private final LegendPerm plugin;

    public AddUserToGroupCommand(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(@NotNull CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();

        String userName = context.getArgument("userName", String.class);
        String groupName = context.getArgument("groupName", String.class);
        String time = getArgumentOrDefault(context, "time", String.class, null);

        try {
            CacheService cacheService = plugin.getServiceRegistry().get(CacheService.class);
            UserRepository userRepository = plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(UserRepository.class);

            // check if user exists
            Optional<User> userCacheResult = cacheService.get(UserCache.class)
                    .get(userName);
            if (userCacheResult.isEmpty()) {
                sender.sendMessage("Unknown User!");
                return 1;
            }
            User user = userCacheResult.get();

            // check if user has any time remaining in this group or is already permanently in this
            //  group.
            long currentTime = 0;
            if (user.hasGroup(groupName)) {
                if (user.hasGroupPermanent(groupName)) {
                    sender.sendMessage("User is already permanent is this group!");
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
                sender.sendMessage("Group does not exist!");
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
            updateUserGroups(groupCache, user, group, validUntil);
            if (validUntil == 0) {
                sender.sendMessage("Added " + user.getName() + " permanently to group " + group.getName() + "!");
            } else {
                sender.sendMessage("Added " + user.getName() + " until " + TimeParser.epochSecondsToInline(validUntil) + ".");
            }
        } catch (SQLException | ExecutionException e) {
            e.printStackTrace();
            sender.sendMessage("An unexpected error occurred!");
        }

        return 1;
    }

    private void updateUserGroups(GroupCache groupCache, User user, Group newGroup, long validUntil) throws ExecutionException {
        List<Group> groups = groupCache.getAll(user.getGroups().keySet())
                .values()
                .stream()
                .map(Optional::orElseThrow)
                .toList();

        // TODO: 03/09/2022 implement own search algorithm (https://www.geeksforgeeks.org/search-insert-position-of-k-in-a-sorted-array/)
        int index = Collections.binarySearch(groups, newGroup, new GroupWeightComparator());
        user.addGroup(index, newGroup.getName(), validUntil);
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
