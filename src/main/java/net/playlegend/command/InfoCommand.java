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
import net.playlegend.domain.Group;
import net.playlegend.domain.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

class InfoCommand implements Command<Object> {

    private final LegendPerm plugin;

    public InfoCommand(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(@NotNull CommandContext<Object> context) throws CommandSyntaxException {
        if (!(context.getSource() instanceof final Player player)) {
            ((CommandSender) context.getSource()).sendMessage("Only player!");
            return 1;
        }

        try {
            Optional<User> cacheResult = plugin.getServiceRegistry().get(CacheService.class)
                    .get(UserCache.class)
                    .get(player.getUniqueId());

            if (cacheResult.isEmpty())
                throw new NullPointerException("user must not be null");

            User user = cacheResult.get();
            player.sendMessage(Component.text("======= " + user.getName() + " ======="));
            player.sendMessage(Component.text("Groups: " + (user.getGroups().isEmpty() ? "none" : "")));
            for (Map.Entry<Group, Long> entry : user.getGroups().entrySet()) {
                player.sendMessage(Component.text("- " + entry.getKey().getName() + (entry.getValue() == 0 ? "" : "(" + entry.getValue() + ")")));
            }
            player.sendMessage(Component.text("======= " + user.getName() + " ======="));
        } catch (ExecutionException e) {
            e.printStackTrace();
            player.sendMessage("An unexpected error occurred!");
        }

        return 1;
    }

}
