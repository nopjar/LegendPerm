package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import net.kyori.adventure.text.Component;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.UserCache;
import net.playlegend.domain.Group;
import net.playlegend.domain.TemporaryGroup;
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
            player.sendMessage(Component.text("Groups: " + (user.getGroups().size() == 0 ? "none" : "")));
            for (Group group : user.getGroups()) {
                player.sendMessage(Component.text("---------------------"));
                player.sendMessage(Component.text("Name: " + group.getName()));
                player.sendMessage(Component.text("Weight: " + group.getWeight()));
                player.sendMessage(Component.text("Prefix: " + group.getPrefix()));
                player.sendMessage(Component.text("Suffix: " + group.getSuffix()));
                player.sendMessage(Component.text("Permissions: " + group.getPermissions()));
                if (group instanceof TemporaryGroup temporaryGroup) {
                    player.sendMessage(Component.text("Expires in: " + Date.from(Instant.ofEpochSecond(temporaryGroup.getValidUntil()))));
                }
            }
            player.sendMessage(Component.text("======= " + user.getName() + " ======="));
        } catch (ExecutionException e) {
            e.printStackTrace();
            player.sendMessage("An unexpected error occurred!");
        }

        return 1;
    }

}
