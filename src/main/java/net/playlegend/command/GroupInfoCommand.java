package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.GroupCache;
import net.playlegend.configuration.MessageConfig;
import net.playlegend.domain.Group;
import org.bukkit.command.CommandSender;

class GroupInfoCommand implements Command<Object> {

    private final LegendPerm plugin;
    private final MessageConfig messages;

    public GroupInfoCommand(LegendPerm plugin, MessageConfig messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public int run(CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();
        if (!sender.hasPermission("legendperm.group.info")) {
            sender.sendMessage(messages.notPermitted.get());
            return 1;
        }

        // fetch command data
        String groupName = context.getArgument("groupName", String.class);
        Map<String, Object> replacements = new HashMap<>();
        replacements.put("group_name", groupName);

        try {
            // fetch group
            Optional<Group> cacheResult = plugin.getServiceRegistry().get(CacheService.class)
                    .get(GroupCache.class)
                    .get(groupName);

            if (cacheResult.isEmpty()) {
                sender.sendMessage(messages.groupDoesNotExist.parse(replacements));
                return 1;
            }

            Group group = cacheResult.get();
            replacements.put("group_weight", group.getWeight());
            replacements.put("group_prefix", group.getPrefix());
            replacements.put("group_suffix", group.getSuffix());
            replacements.put("permissions", group.getPermissions());
            sender.sendMessage(messages.groupInfo.parse(replacements));
        } catch (ExecutionException e) {
            e.printStackTrace();
            sender.sendMessage(messages.unexpectedError.parse(replacements));
        }
        return 1;
    }

}
