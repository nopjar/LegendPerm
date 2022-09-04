package net.playlegend.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.command.CommandService;
import net.playlegend.configuration.Config;
import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.listener.ListenerService;
import net.playlegend.permission.PermissionService;
import net.playlegend.repository.RepositoryService;

public class ServiceRegistry {

    private final Map<Class<? extends Service>, Service> services;

    public ServiceRegistry(LegendPerm plugin, Config config) {
        this.services = new ConcurrentHashMap<>();

        this.services.put(RepositoryService.class, new RepositoryService(plugin, config));
        this.services.put(CacheService.class, new CacheService(plugin));
        this.services.put(CommandService.class, new CommandService(plugin));
        this.services.put(ListenerService.class, new ListenerService(plugin));
        this.services.put(PermissionService.class, new PermissionService(plugin));
    }

    public void start() throws ServiceInitializeException {
        for (Service service : this.services.values()) {
            service.initialize();
        }
    }

    public void shutdown() {
        for (Service service : this.services.values()) {
            // we handle a shutdown differently from a start as we don't want to start the plugin
            // if just one service fails.
            // But we definitely want to give every service the opportunity to shut down even if one
            // fails.
            try {
                service.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public <T extends Service> T get(Class<T> clazz) {
        return (T) services.get(clazz);
    }

}
