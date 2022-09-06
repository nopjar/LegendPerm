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
import net.playlegend.domain.Permission;
import net.playlegend.repository.GroupRepository;
import net.playlegend.repository.RepositoryService;
import org.bukkit.command.CommandSender;

class AddPermissionToGroupCommand implements Command<Object> {

    private final LegendPerm plugin;
    private final MessageConfig messages;

    public AddPermissionToGroupCommand(LegendPerm plugin, MessageConfig messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public int run(CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();
        if (!sender.hasPermission("legendperm.group.addpermission")) {
            sender.sendMessage(messages.notPermitted.get());
            return 1;
        }

        String groupName = context.getArgument("groupName", String.class);
        String permissionNode = context.getArgument("permissionNode", String.class);
        boolean mode = context.getArgument("mode", Boolean.class);
        Map<String, Object> replacements = new HashMap<>();
        replacements.put("group_name", groupName);
        replacements.put("permission_node", permissionNode);
        replacements.put("permission_mode", mode);

        try {
            Optional<Group> cacheResult = plugin.getServiceRegistry().get(CacheService.class)
                    .get(GroupCache.class)
                    .get(groupName);

            if (cacheResult.isEmpty()) {
                sender.sendMessage(messages.groupDoesNotExist.parse(replacements));
                return 1;
            }

            Group group = cacheResult.get();
            Permission permission = new Permission(permissionNode, mode);
            if (group.getPermissions().contains(permission)) {
                sender.sendMessage(messages.groupDoesAlreadyContainPermission.parse(replacements));
                return 1;
            }

            plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(GroupRepository.class)
                    .updatePermissionInGroup(group, permission);
            group.addPermission(permission);

            sender.sendMessage(messages.groupAddedPermission.parse(replacements));
        } catch (SQLException | ExecutionException e) {
            e.printStackTrace();
            sender.sendMessage(messages.unexpectedError.parse(replacements));
        }

        return 1;
    }

}
