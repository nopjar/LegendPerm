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
            User user = plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(UserRepository.class)
                    .selectUserByUUID(player.getUniqueId());

            if (user == null)
                throw new NullPointerException("user must not be null");

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
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            player.sendMessage("An unexpected error occurred!");
        }

        return 1;
    }

}
