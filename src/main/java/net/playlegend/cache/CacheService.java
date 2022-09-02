package net.playlegend.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.playlegend.LegendPerm;
import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.exception.ServiceShutdownException;
import net.playlegend.service.Service;
import org.bukkit.Bukkit;

public class CacheService extends Service {

    private final Map<Class<? extends Cache<?, ?>>, Cache<?, ?>> caches;

    public CacheService(LegendPerm plugin) {
        super(plugin);
        this.caches = new ConcurrentHashMap<>();

        this.caches.put(UserCache.class, new UserCache(plugin));
        this.caches.put(GroupCache.class, new GroupCache(plugin));
        this.caches.put(PermissionCache.class, new PermissionCache(plugin));
    }

    @Override
    public void initialize() throws ServiceInitializeException {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
                () -> this.caches.values().forEach(Cache::cleanup),
                100L,
                100L); // TODO: 02/09/2022 make it configurable
    }

    @Override
    public void shutdown() throws ServiceShutdownException {
        for (Cache<?, ?> cache : caches.values()) {
            cache.releaseAll();
        }
    }

    public <T extends Cache<?, ?>> T get(Class<T> clazz) {
        return (T) caches.get(clazz);
    }

}
