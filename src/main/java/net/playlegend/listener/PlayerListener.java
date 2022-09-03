package net.playlegend.listener;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.GroupCache;
import net.playlegend.cache.PermissionCache;
import net.playlegend.cache.UserCache;
import net.playlegend.domain.Group;
import net.playlegend.domain.Permission;
import net.playlegend.domain.User;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.UserRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {

    private final LegendPerm plugin;

    public PlayerListener(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                UserRepository userRepository = plugin.getServiceRegistry().get(RepositoryService.class)
                        .get(UserRepository.class);
                UserCache userCache = plugin.getServiceRegistry().get(CacheService.class)
                        .get(UserCache.class);

                Optional<User> cacheResult = userCache.get(player.getUniqueId());

                // check if user data exists and if it does, check for correctness
                User user;
                if (cacheResult.isEmpty()) {
                    userRepository.updateUser(player.getUniqueId(), player.getName());
                    userRepository.addUserToGroup(player.getUniqueId(), "default", 0);
                    userCache.refresh(player.getUniqueId());
                    cacheResult = userCache.get(player.getUniqueId());
                    user = cacheResult.orElseThrow();
                } else if (!(user = cacheResult.get()).getName().equals(player.getName())) {
                    userRepository.updateUser(user);
                    userCache.refresh(user.getUuid());
                    user = userCache.get(player.getUniqueId())
                            .orElseThrow();
                }

                // fetch permissions
                List<Group> groups = plugin.getServiceRegistry().get(CacheService.class)
                        .get(GroupCache.class)
                        .getAll(user.getGroups().keySet())
                        .values()
                        .stream()
                        .map(Optional::orElseThrow) // throw because user should not have any groups that don't exist!
                        .toList();

                // apply permissions
                PermissionAttachment attachment = plugin.getServiceRegistry().get(CacheService.class)
                        .get(PermissionCache.class)
                        .get(player.getUniqueId());
                for (Group group : groups) {
                    for (Permission permission : group.getPermissions()) {
                        if (attachment.getPermissions().containsKey(permission.getNode()))
                            continue;

                        attachment.setPermission(permission.getNode(), permission.getMode());
                    }
                }
                player.recalculatePermissions();

                Bukkit.broadcast(Component.text("[" + groups.get(0).getPrefix() + "§r] §e" + player.getName() + " joined!"));
            } catch (SQLException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    @EventHandler
    private void onPlayerLeave(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // saving properties now as this is later needed in async (maybe no player object anymore)
        final String name = player.getName();
        final UUID uuid = player.getUniqueId();
        event.quitMessage(null);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                CacheService cacheService = plugin.getServiceRegistry().get(CacheService.class);
                UserCache userCache = cacheService.get(UserCache.class);

                PermissionCache permissionCache = cacheService.get(PermissionCache.class);
                permissionCache.get(uuid)
                        .remove();
                permissionCache.release(uuid);

                User user = userCache.get(uuid)
                        .orElseThrow();
                // fetch prefix
                String prefix = plugin.getServiceRegistry().get(CacheService.class)
                        .get(GroupCache.class)
                        .get(user.getMainGroupName())
                        .orElseThrow()
                        .getPrefix();

                Bukkit.broadcast(Component.text("[" + prefix + "§r] §e" + name + " left!"));
                userCache.release(uuid);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    @EventHandler
    private void onPlayerChat(@NotNull AsyncChatEvent event) {
        try {
            CacheService cacheService = plugin.getServiceRegistry().get(CacheService.class);
            User user = cacheService.get(UserCache.class)
                    .get(event.getPlayer().getUniqueId())
                    .orElseThrow();

            String prefix = cacheService.get(GroupCache.class)
                    .get(user.getMainGroupName())
                    .orElseThrow()
                    .getPrefix();

            ChatRenderer renderer = new CustomChatRenderer(prefix);
            event.renderer(renderer);
        } catch (ExecutionException e) {
            // TODO: 02/09/2022 notify player?
            e.printStackTrace();
        }
    }

    private record CustomChatRenderer(String prefix) implements ChatRenderer, ChatRenderer.ViewerUnaware {

        private CustomChatRenderer(String prefix) {
            this.prefix = "[" + prefix + "§r]";
        }

        @Override
        public @NotNull Component render(final @NotNull Player source, final @NotNull Component sourceDisplayName, final @NotNull Component message, final @NotNull Audience viewer) {
            return this.render(source, sourceDisplayName, message);
        }

        @Override
        public @NotNull Component render(final @NotNull Player source, final @NotNull Component sourceDisplayName, final @NotNull Component message) {
            return Component.text(prefix + " ")
                    .append(sourceDisplayName)
                    .append(Component.text(": "))
                    .append(message);
        }

    }

}
