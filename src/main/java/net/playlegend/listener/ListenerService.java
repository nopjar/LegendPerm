package net.playlegend.listener;

import net.playlegend.LegendPerm;
import net.playlegend.configuration.ConfigurationService;
import net.playlegend.configuration.MessageConfig;
import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.exception.ServiceShutdownException;
import net.playlegend.service.Service;
import org.bukkit.Bukkit;

public class ListenerService extends Service {

    public ListenerService(LegendPerm plugin) {
        super(plugin);
    }

    @Override
    public void initialize() throws ServiceInitializeException {
        MessageConfig config = plugin.getServiceRegistry().get(ConfigurationService.class)
                .get(MessageConfig.class);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(plugin, config), plugin);
        Bukkit.getPluginManager().registerEvents(new SignListener(plugin), plugin);
    }

    @Override
    public void shutdown() throws ServiceShutdownException {

    }

}
