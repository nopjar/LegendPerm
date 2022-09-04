package net.playlegend.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.domain.Sign;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.SignRepository;
import org.jetbrains.annotations.NotNull;

public class SignCache extends Cache<UUID, List<Sign>> {

    private final LoadingCache<UUID, List<Sign>> cache;

    public SignCache(LegendPerm plugin) {
        super(plugin);

        CacheLoader<UUID, List<Sign>> loader = new CacheLoader<>() {
            @Override
            public @NotNull List<Sign> load(@NotNull UUID uuid) throws Exception {
                return plugin.getServiceRegistry()
                        .get(RepositoryService.class)
                        .get(SignRepository.class)
                        .selectSignsByUser(uuid);
            }
        };

        this.cache = CacheBuilder.newBuilder().build(loader);
    }

    @Override
    public List<Sign> get(UUID uuid) throws ExecutionException {
        return this.cache.get(uuid);
    }

    @Override
    public ImmutableMap<UUID, List<Sign>> getAll(Iterable<UUID> iterable) throws ExecutionException {
        return this.cache.getAll(iterable);
    }

    @Override
    protected void releaseAll() {
        this.cache.invalidateAll();
    }

    @Override
    public void refresh(UUID uuid) {
        this.cache.refresh(uuid);
    }

    @Override
    public void release(UUID uuid) {
        this.cache.invalidate(uuid);
    }

    @Override
    public void cleanup() {
        this.cache.cleanUp();
    }

}
