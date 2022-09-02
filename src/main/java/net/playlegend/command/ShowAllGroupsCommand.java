package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.SQLException;
import java.util.List;
import net.playlegend.LegendPerm;
import net.playlegend.repository.GroupRepository;
import net.playlegend.repository.RepositoryService;
import org.bukkit.command.CommandSender;

class ShowAllGroupsCommand implements Command<Object> {

    private final LegendPerm plugin;

    public ShowAllGroupsCommand(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();

        try {
            List<String> names = plugin.getServiceRegistry()
                    .get(RepositoryService.class)
                    .get(GroupRepository.class)
                    .selectAllGroupNames();

            sender.sendMessage("====== Groups ======");
            sender.sendMessage("- " + String.join("\n- ", names));
            sender.sendMessage("====== Groups ======");
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage("An unexpected error occurred!");
        }

        return 1;
    }

}
