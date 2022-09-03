package net.playlegend.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.domain.User;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.UserRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
    public Optional<User> get(UUID uuid) throws ExecutionException {
        return this.cache.get(uuid);
    }

    public Optional<User> get(String name) throws ExecutionException {
        // check if player is online to speed up lookup
        Player player = Bukkit.getPlayerExact(name);
        if (player != null) {
            return get(player.getUniqueId());
        }

        // check if user is already loaded
        for (Map.Entry<UUID, Optional<User>> entry : this.cache.asMap().entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            if (entry.getValue().get().getName().equalsIgnoreCase(name))
                return entry.getValue();
        }

        // load user and put it into the cache manually
        try {
            User user = plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(UserRepository.class)
                    .selectUserByName(name);

            Optional<User> optionalUser = Optional.ofNullable(user);
            if (optionalUser.isPresent())
                this.cache.put(user.getUuid(), Optional.of(user));

            return optionalUser;
        } catch (SQLException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public ImmutableMap<UUID, Optional<User>> getAll(Iterable<UUID> iterable) throws ExecutionException {
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
        this.cache.asMap()
                .entrySet()
                .removeIf(e -> e.getValue().isEmpty());

        this.cache.cleanUp();
    }

}
