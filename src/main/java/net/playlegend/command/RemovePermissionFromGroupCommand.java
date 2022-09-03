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
import net.playlegend.domain.Permission;
import net.playlegend.repository.GroupRepository;
import net.playlegend.repository.RepositoryService;
import org.bukkit.command.CommandSender;

class RemovePermissionFromGroupCommand implements Command<Object> {

    private final LegendPerm plugin;

    public RemovePermissionFromGroupCommand(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();

        String groupName = context.getArgument("groupName", String.class);
        String permissionNode = context.getArgument("permissionNode", String.class);

        try {
            Optional<Group> cacheResult = plugin.getServiceRegistry().get(CacheService.class)
                    .get(GroupCache.class)
                    .get(groupName);

            if (cacheResult.isEmpty()) {
                sender.sendMessage("Group does not exist!");
                return 1;
            }

            Group group = cacheResult.get();
            boolean found = false;
            for (Permission permission : group.getPermissions()) {
                if (permission.getNode().equalsIgnoreCase(permissionNode)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                sender.sendMessage("Group does not contain permission!");
                return 1;
            }

            plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(GroupRepository.class)
                    .revokePermissionFromGroup(group, permissionNode);
            group.removePermission(permissionNode);

            sender.sendMessage("Permission revoked!");
        } catch (SQLException | ExecutionException e) {
            e.printStackTrace();
            sender.sendMessage("An unexpected error occurred!");
        }

        return 1;
    }

}
