package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.SQLException;
import net.playlegend.LegendPerm;
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
            GroupRepository groupRepository = plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(GroupRepository.class);

            Group group = groupRepository.selectGroupByName(groupName);
            if (group == null) {
                sender.sendMessage("Group does not exist!");
                return 1;
            }

            boolean found = false;
            for (Permission permission : group.getPermissions()) {
                if (permission.node().equalsIgnoreCase(permissionNode)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                sender.sendMessage("Group does not contain permission!");
                return 1;
            }

            groupRepository.revokePermissionFromGroup(group, permissionNode);
            sender.sendMessage("Permission revoked!");
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage("An unexpected error occurred!");
        }

        return 1;
    }

}
