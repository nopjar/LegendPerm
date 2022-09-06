package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.GroupCache;
import net.playlegend.configuration.MessageConfig;
import net.playlegend.domain.Group;
import net.playlegend.repository.GroupRepository;
import net.playlegend.repository.RepositoryService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

class GroupSetPropertyCommand implements Command<Object> {

    private final LegendPerm plugin;
    private final MessageConfig messages;

    public GroupSetPropertyCommand(LegendPerm plugin, MessageConfig messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public int run(CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();
        if (!sender.hasPermission("legendperm.group.update")) {
            sender.sendMessage(messages.notPermitted.get());
            return 1;
        }

        // fetch command data
        String groupName = context.getArgument("groupName", String.class);
        String key = context.getArgument("key", String.class);
        String value = ChatColor.translateAlternateColorCodes('&', getArgumentOrDefault(context, "value", String.class, ""));
        Map<String, Object> replacements = new HashMap<>();
        replacements.put("group_name", groupName);
        replacements.put("key", key);
        replacements.put("value", value);

        Group.Property property = Group.Property.find(key);
        if (property == null) {
            sender.sendMessage(messages.groupUnknownProperty.parse(replacements));
            return 1;
        }

        try {
            // fetch group
            Optional<Group> cacheResult = plugin.getServiceRegistry().get(CacheService.class)
                    .get(GroupCache.class)
                    .get(groupName);

            if (cacheResult.isEmpty()) {
                sender.sendMessage(messages.groupDoesNotExist.parse(replacements));
                return 1;
            }

            // change property of group
            Group group = cacheResult.get();
            try {
                group.changeProperty(property, value);
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(messages.unexpectedError.parse(replacements));
                return 0;
            }

            // save change to db
            plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(GroupRepository.class)
                    .updateGroup(group);

            sender.sendMessage(messages.groupUpdated.parse(replacements));
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
