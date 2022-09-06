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
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

class GroupDeleteCommand implements Command<Object> {

    private final LegendPerm plugin;
    private final MessageConfig messages;

    public GroupDeleteCommand(LegendPerm plugin, MessageConfig messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public int run(@NotNull CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();

        String groupName = context.getArgument("groupName", String.class);
        Map<String, Object> replacements = new HashMap<>();
        replacements.put("group_name", groupName);

        try {
            GroupCache groupCache = plugin.getServiceRegistry().get(CacheService.class)
                    .get(GroupCache.class);
            Optional<Group> cacheResult = groupCache.get(groupName);

            if (cacheResult.isEmpty()) {
                sender.sendMessage(messages.groupDoesNotExist.parse(replacements));
                return 1;
            }

            Group group = cacheResult.get();
            plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(GroupRepository.class)
                    .deleteGroup(group);

            group.delete();
            groupCache.release(groupName);
            sender.sendMessage(messages.groupDeleted.parse(replacements));
        } catch (SQLException | ExecutionException e) {
            e.printStackTrace();
            sender.sendMessage(messages.unexpectedError.parse(replacements));
        }

        return 1;
    }

}
