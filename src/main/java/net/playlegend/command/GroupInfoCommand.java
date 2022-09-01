package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.SQLException;
import net.playlegend.LegendPerm;
import net.playlegend.domain.Group;
import net.playlegend.repository.GroupRepository;
import net.playlegend.repository.RepositoryService;
import org.bukkit.command.CommandSender;

class GroupInfoCommand implements Command<Object> {

    private final LegendPerm plugin;

    public GroupInfoCommand(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();

        String groupName = context.getArgument("groupName", String.class);

        try {
            Group group = plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(GroupRepository.class)
                    .selectGroupByName(groupName);

            if (group == null) {
                sender.sendMessage("No group with this name exists. Use /lp group " + groupName + " create");
                return 1;
            }

            sender.sendMessage("Group: " + group);
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage("An unexpected error occurred!");
        }
        return 1;
    }

}
