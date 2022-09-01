package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import net.kyori.adventure.text.Component;
import net.playlegend.LegendPerm;
import net.playlegend.domain.Group;
import net.playlegend.domain.TemporaryGroup;
import net.playlegend.domain.User;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.UserRepository;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

class UserInfoCommand implements Command<Object> {

    private final LegendPerm plugin;
    
    public UserInfoCommand(LegendPerm plugin) {
        this.plugin = plugin;
    }

    // TODO: 02/09/2022 make abstract userInfoCommand? to remove duplicate code!

    @Override
    public int run(@NotNull CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();

        String userName = context.getArgument("userName", String.class);
        
        try {
            User user = plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(UserRepository.class)
                    .selectUserByName(userName);

            if (user == null) {
                sender.sendMessage("Unknown User!");
                return 0;
            }

            sender.sendMessage(Component.text("======= " + user.getName() + " ======="));
            sender.sendMessage(Component.text("Groups: " + (user.getGroups().size() == 0 ? "none" : "")));
            for (Group group : user.getGroups()) {
                sender.sendMessage(Component.text("---------------------"));
                sender.sendMessage(Component.text("Name: " + group.getName()));
                sender.sendMessage(Component.text("Weight: " + group.getWeight()));
                sender.sendMessage(Component.text("Prefix: " + group.getPrefix()));
                sender.sendMessage(Component.text("Suffix: " + group.getSuffix()));
                sender.sendMessage(Component.text("Permissions: " + group.getPermissions()));
                if (group instanceof TemporaryGroup temporaryGroup) {
                    sender.sendMessage(Component.text("Expires in: " + Date.from(Instant.ofEpochMilli(temporaryGroup.getValidUntil()))));
                }
            }
            sender.sendMessage(Component.text("======= " + user.getName() + " ======="));
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            sender.sendMessage("An unexpected error occurred!");
        }

        return 1;
    }

}
