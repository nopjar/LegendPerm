package net.playlegend.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.domain.Group;
import net.playlegend.repository.GroupRepository;
import net.playlegend.repository.RepositoryService;
import org.jetbrains.annotations.NotNull;

public class GroupCache extends Cache<String, Optional<Group>> {

    private final LoadingCache<String, Optional<Group>> cache;

    public GroupCache(LegendPerm plugin) {
        super(plugin);

        CacheLoader<String, Optional<Group>> loader = new CacheLoader<>() {
            @Override
            public @NotNull Optional<Group> load(@NotNull String key) throws Exception {
                System.out.println("Loading from db to cache: " + key);
                return Optional.ofNullable(plugin.getServiceRegistry()
                        .get(RepositoryService.class)
                        .get(GroupRepository.class)
                        .selectGroupByName(key));
            }
        };

        this.cache = CacheBuilder.newBuilder().build(loader);
    }

    @Override
    protected void releaseAll() {
        this.cache.invalidateAll();
    }

    @Override
    public Optional<Group> get(String key) throws ExecutionException {
        return this.cache.get(key.toLowerCase(Locale.ROOT));
    }

    @Override
    public ImmutableMap<String, Optional<Group>> getAll(Iterable<String> iterable) throws ExecutionException {
        return this.cache.getAll(iterable);
    }

    @Override
    public void refresh(String key) {
        this.cache.refresh(key.toLowerCase(Locale.ROOT));
    }

    @Override
    public void release(String key) {
        this.cache.invalidate(key.toLowerCase(Locale.ROOT));
    }

    @Override
    public void cleanup() {
        this.cache.asMap()
                .entrySet()
                .removeIf(e -> e.getValue().isEmpty());

        this.cache.cleanUp();
    }

    void preload() throws SQLException {
        List<Group> groups = plugin.getServiceRegistry().get(RepositoryService.class)
                .get(GroupRepository.class)
                .selectAllGroups();

        groups.forEach(g -> cache.put(g.getName(), Optional.of(g)));
    }

}
