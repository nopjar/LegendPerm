package net.playlegend.listener;

import net.playlegend.LegendPerm;
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
        Bukkit.getPluginManager().registerEvents(new PlayerListener(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new SignListener(plugin), plugin);
    }

    @Override
    public void shutdown() throws ServiceShutdownException {

    }

}
