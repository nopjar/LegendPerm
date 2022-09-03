package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import net.kyori.adventure.text.Component;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.UserCache;
import net.playlegend.domain.User;
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
            Optional<User> cacheResult = plugin.getServiceRegistry().get(CacheService.class)
                    .get(UserCache.class)
                    .get(userName);
            if (cacheResult.isEmpty()) {
                sender.sendMessage("Unknown User!");
                return 0;
            }

            User user = cacheResult.get();
            sender.sendMessage(Component.text("======= " + user.getName() + " ======="));
            sender.sendMessage(Component.text("Groups: " + (user.getGroups().isEmpty() ? "none" : "")));
            for (Map.Entry<String, Long> entry : user.getGroups().entrySet()) {
                sender.sendMessage(Component.text("- " + entry.getKey() + (entry.getValue() == 0 ? "" : "(" + entry.getValue() + ")")));
            }
            sender.sendMessage(Component.text("======= " + user.getName() + " ======="));
        } catch (ExecutionException e) {
            e.printStackTrace();
            sender.sendMessage("An unexpected error occurred!");
        }

        return 1;
    }

}
