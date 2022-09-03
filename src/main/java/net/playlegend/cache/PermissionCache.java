package net.playlegend.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;

public class PermissionCache extends Cache<UUID, PermissionAttachment> {

    private final LoadingCache<UUID, PermissionAttachment> cache;

    public PermissionCache(LegendPerm plugin) {
        super(plugin);

        CacheLoader<UUID, PermissionAttachment> loader = new CacheLoader<>() {
            @Override
            public @NotNull PermissionAttachment load(@NotNull UUID uuid) throws Exception {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player == null)
                    throw new IllegalArgumentException("Player is not online!");

                return player.addAttachment(plugin);
            }
        };

        this.cache = CacheBuilder.newBuilder()
                .build(loader);
    }

    @Override
    protected void releaseAll() {
        this.cache.invalidateAll();
    }

    @Override
    public PermissionAttachment get(UUID key) throws ExecutionException {
        return this.cache.get(key);
    }

    @Override
    public ImmutableMap<UUID, PermissionAttachment> getAll(Iterable<UUID> iterable) throws ExecutionException {
        return this.cache.getAll(iterable);
    }

    @Override
    public void refresh(UUID key) {
        throw new UnsupportedOperationException("PermissionCache does not support refreshing.");
    }

    @Override
    public void release(UUID key) {
        this.cache.invalidate(key);
    }

    @Override
    public void cleanup() {
        // this is empty on purpose as there won't be any cleanups to do
    }

}
