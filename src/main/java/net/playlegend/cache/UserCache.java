package net.playlegend.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.domain.User;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.UserRepository;
import org.jetbrains.annotations.NotNull;

public class UserCache extends Cache<UUID, Optional<User>> {

    private final LoadingCache<UUID, Optional<User>> cache;

    public UserCache(LegendPerm plugin) {
        super(plugin);

        CacheLoader<UUID, Optional<User>> loader = new CacheLoader<>() {
            @Override
            public @NotNull Optional<User> load(@NotNull UUID uuid) throws Exception {
                return Optional.ofNullable(plugin.getServiceRegistry()
                        .get(RepositoryService.class)
                        .get(UserRepository.class)
                        .selectUserByUUID(uuid));
            }
        };

        this.cache = CacheBuilder.newBuilder().build(loader);
    }

    @Override
    protected void releaseAll() {
        this.cache.invalidateAll();
    }

    @Override
    public Optional<User> get(UUID uuid) throws ExecutionException {
        return this.cache.get(uuid);
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
        this.cache.asMap()
                .entrySet()
                .removeIf(e -> e.getValue().isEmpty());

        this.cache.cleanUp();
    }

}
