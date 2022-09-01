package net.playlegend.command;

import com.mojang.brigadier.CommandDispatcher;
import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.exception.ServiceShutdownException;
import net.playlegend.service.Service;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;

public class CommandService implements Service {

    public CommandService() {
    }

    @Override
    public void initialize() throws ServiceInitializeException {
        CommandDispatcher<Object> dispatcher = new CommandDispatcher<>();
        dispatcher.register(new CommandTreeConstructor().construct());

        CommandMediator mediator = new CommandMediator(dispatcher);

        PluginCommand legendPerm = Bukkit.getPluginCommand("legendperm");
        legendPerm.setExecutor(mediator);
        legendPerm.setTabCompleter(mediator);
    }

    @Override
    public void shutdown() throws ServiceShutdownException {

    }

}
