package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.GroupCache;
import net.playlegend.configuration.MessageConfig;
import net.playlegend.repository.GroupRepository;
import net.playlegend.repository.RepositoryService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

class GroupCreateCommand implements Command<Object> {

    private final LegendPerm plugin;
    private final MessageConfig messages;

    public GroupCreateCommand(LegendPerm plugin, MessageConfig messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public int run(@NotNull CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();
        if (!sender.hasPermission("legendperm.group.create")) {
            sender.sendMessage(messages.notPermitted.get());
            return 1;
        }

        // fetch all command data
        String groupName = context.getArgument("groupName", String.class);
        int weight = getArgumentOrDefault(context, "weight", Integer.class, 100);
        String prefix = ChatColor.translateAlternateColorCodes('&', getArgumentOrDefault(context, "prefix", String.class, ""));
        String suffix = ChatColor.translateAlternateColorCodes('&', getArgumentOrDefault(context, "suffix", String.class, ""));

        Map<String, Object> replacements = new HashMap<>();
        replacements.put("group_name", groupName);
        replacements.put("group_weight", weight);
        replacements.put("group_prefix", prefix);
        replacements.put("group_suffix", suffix);

        try {
            GroupCache groupCache = plugin.getServiceRegistry().get(CacheService.class)
                    .get(GroupCache.class);

            // check if group already exists
            if (groupCache.get(groupName).isPresent()) {
                sender.sendMessage(messages.groupAlreadyExists.parse(replacements));
                return 1;
            }

            // save to db and refresh cache
            plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(GroupRepository.class)
                    .createGroup(groupName, weight, prefix, suffix);
            groupCache.refresh(groupName);

            sender.sendMessage(messages.groupCreated.parse(replacements));
        } catch (SQLException | ExecutionException e) {
            e.printStackTrace();
            sender.sendMessage(messages.unexpectedError.parse(replacements));
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
