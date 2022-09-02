package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.GroupCache;
import net.playlegend.domain.Group;
import net.playlegend.repository.GroupRepository;
import net.playlegend.repository.RepositoryService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

class GroupSetPropertyCommand implements Command<Object> {

    private final LegendPerm plugin;

    public GroupSetPropertyCommand(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();

        String groupName = context.getArgument("groupName", String.class);
        String key = context.getArgument("key", String.class);
        String value = ChatColor.translateAlternateColorCodes('&', getArgumentOrDefault(context, "value", String.class, ""));
        Group.Property property = Group.Property.find(key);
        if (property == null) {
            sender.sendMessage("Unknown Key!");
            return 1;
        }

        try {
            Optional<Group> cacheResult = plugin.getServiceRegistry().get(CacheService.class)
                    .get(GroupCache.class)
                    .get(groupName);

            if (cacheResult.isEmpty()) {
                sender.sendMessage("No group found!");
                return 1;
            }

            Group group = cacheResult.get();
            try {
                group.changeProperty(property, value);
            } catch (Exception e) {
                sender.sendMessage("ERROR: " + e.getMessage());
                return 0;
            }

            plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(GroupRepository.class)
                    .updateGroup(group);

            sender.sendMessage("Group updated!");
        } catch (SQLException | ExecutionException e) {
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
