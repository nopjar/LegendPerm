package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.GroupCache;
import net.playlegend.domain.Group;
import net.playlegend.repository.GroupRepository;
import net.playlegend.repository.RepositoryService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

class GroupCreateCommand implements Command<Object> {

    private final LegendPerm plugin;

    public GroupCreateCommand(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(@NotNull CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();

        String groupName = context.getArgument("groupName", String.class);
        int weight = getArgumentOrDefault(context, "weight", Integer.class, 100);
        String prefix = ChatColor.translateAlternateColorCodes('&', getArgumentOrDefault(context, "prefix", String.class, ""));
        String suffix = ChatColor.translateAlternateColorCodes('&', getArgumentOrDefault(context, "suffix", String.class, ""));

        try {
            GroupCache groupCache = plugin.getServiceRegistry().get(CacheService.class)
                    .get(GroupCache.class);

            // make sure to prevent SQLException
            if (groupCache.get(groupName).isPresent()) {
                sender.sendMessage("Group " + groupName + " already exists!");
                return 1;
            }

            plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(GroupRepository.class)
                    .createGroup(groupName, weight, prefix, suffix);

            sender.sendMessage("Group created! " + groupName);
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
