package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.SQLException;
import net.playlegend.LegendPerm;
import net.playlegend.domain.Group;
import net.playlegend.domain.TemporaryGroup;
import net.playlegend.domain.User;
import net.playlegend.repository.GroupRepository;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.UserRepository;
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
        String time = getArgumentOrDefault(context, "suffix", String.class, null);

        try {
            UserRepository userRepository = plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(UserRepository.class);

            // make sure to prevent SQLException
            User user = userRepository.selectUserByName(userName);
            if (user == null) {
                sender.sendMessage("Unknown User!");
                return 1;
            }

            long currentTime = 0;
            for (Group group : user.getGroups()) {
                if (group.getName().equalsIgnoreCase(groupName)) {
                    if (group instanceof TemporaryGroup tg) {
                        currentTime = Math.max(0, tg.getValidUntil() - System.currentTimeMillis());
                        break;
                    } else {
                        sender.sendMessage("User is already permanent is this group!");
                        return 1;
                    }
                }
            }

            // TODO: 02/09/2022 Create TimeParser and get timemillis validUntil, add those together
            //  and update database. Remember, that it maybe appears, that time is not given and group
            //  shall get granted permanently.

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
