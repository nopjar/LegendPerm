package net.playlegend.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.playlegend.LegendPerm;
import net.playlegend.configuration.MessageConfig;
import net.playlegend.repository.GroupRepository;
import net.playlegend.repository.RepositoryService;
import org.bukkit.command.CommandSender;

class ShowAllGroupsCommand implements Command<Object> {

    private final LegendPerm plugin;
    private final MessageConfig messages;

    public ShowAllGroupsCommand(LegendPerm plugin, MessageConfig messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public int run(CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = (CommandSender) context.getSource();
        if (!sender.hasPermission("legendperm.group.list")) {
            sender.sendMessage(messages.notPermitted.get());
            return 1;
        }

        try {
            List<String> names = plugin.getServiceRegistry()
                    .get(RepositoryService.class)
                    .get(GroupRepository.class)
                    .selectAllGroupNames();


            sender.sendMessage(messages.groupList.parse(Map.of("groups", "- " + String.join("\n -", names))));
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(messages.unexpectedError.get());
        }

        return 1;
    }

}
