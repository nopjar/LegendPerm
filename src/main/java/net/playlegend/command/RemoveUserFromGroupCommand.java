package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import net.playlegend.LegendPerm;
import net.playlegend.domain.Group;
import net.playlegend.domain.TemporaryGroup;
import net.playlegend.domain.User;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.UserRepository;
import net.playlegend.time.TimeParser;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

class RemoveUserFromGroupCommand implements Command<Object> {

    private final LegendPerm plugin;

    public RemoveUserFromGroupCommand(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(@NotNull CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();

        String userName = context.getArgument("userName", String.class);
        String groupName = context.getArgument("groupName", String.class);
        String time = getArgumentOrDefault(context, "time", String.class, null);

        try {
            UserRepository userRepository = plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(UserRepository.class);

            // check if user exists
            User user = userRepository.selectUserByName(userName);
            if (user == null) {
                sender.sendMessage("Unknown User!");
                return 1;
            }

            // check if user has group
            Group group = null;
            for (Group userGroup : user.getGroups()) {
                if (userGroup.getName().equalsIgnoreCase(groupName)) {
                    group = userGroup;
                }
            }

            if (group == null) {
                sender.sendMessage("User does not have group!");
                return 1;
            }

            // if group get permanently removed, there is no need to check for time
            //  if time is given, we need to check if group is temporary
            if (time == null) {
                userRepository.removeUserFromGroup(user.getUuid(), group.getName());
                sender.sendMessage("User " + user.getName() + " removed from Group " + group.getName() + ".");
                return 0;
            }

            if (!(group instanceof TemporaryGroup tg)) {
                sender.sendMessage("Can't remove time from Group as user has the group permanent!");
                return 0;
            }

            // get valid until reduced by the removed time
            LocalDateTime validUntilDT = TimeParser.localDateTimeFromEpochSeconds(tg.getValidUntil());
            LocalDateTime now = LocalDateTime.now();
            long seconds = new TimeParser(time, now).getInSeconds();
            LocalDateTime newValidUntilDT = validUntilDT.minus(seconds, ChronoUnit.SECONDS);
            if (newValidUntilDT.isBefore(now)) {
                userRepository.removeUserFromGroup(user.getUuid(), group.getName());
                sender.sendMessage("User " + user.getName() + " removed from Group " + group.getName() + ".");
                return 0;
            }
            long validUntil = tg.getValidUntil() - seconds;

            // save to database
            userRepository.addUserToGroup(user.getUuid(), group.getName(), validUntil);
            sender.sendMessage(user.getName() + " grouptime reduced to " + TimeParser.epochSecondsToInline(validUntil) + ".");
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage("An unexpected error occurred!");
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
