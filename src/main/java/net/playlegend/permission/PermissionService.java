package net.playlegend.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.PermissionCache;
import net.playlegend.cache.UserCache;
import net.playlegend.domain.Group;
import net.playlegend.domain.Permission;
import net.playlegend.domain.User;
import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.exception.ServiceShutdownException;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.UserRepository;
import net.playlegend.service.Service;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;

public class PermissionService extends Service {

    private final Map<UUID, Integer> taskIds;

    public PermissionService(LegendPerm plugin) {
        super(plugin);
        this.taskIds = new ConcurrentHashMap<>();
    }

    @Override
    public void initialize() throws ServiceInitializeException {

    }

    @Override
    public void shutdown() throws ServiceShutdownException {

    }

    public void processPlayerJoin(@NotNull Player player, @NotNull Map<Group, Long> groups) throws ExecutionException {
        // get attachment
        PermissionAttachment attachment = plugin.getServiceRegistry().get(CacheService.class)
                .get(PermissionCache.class)
                .get(player.getUniqueId());

        // check if user has any timed group, if so we need to run a user specific thread to check
        //  if time is over
        // also add permissions to player object
        boolean hasTimedGroups = false;
        for (Map.Entry<Group, Long> entry : groups.entrySet()) {
            if (!hasTimedGroups)
                hasTimedGroups = entry.getValue() != 0;

            for (Permission permission : entry.getKey().getPermissions()) {
                if (attachment.getPermissions().containsKey(permission.getNode()))
                    continue;

                attachment.setPermission(permission.getNode(), permission.getMode());
            }
        }
        player.recalculatePermissions();

        // if player has timed group, start thread like mentioned before
        if (hasTimedGroups) {
            UserCache userCache = plugin.getServiceRegistry().get(CacheService.class)
                    .get(UserCache.class);
            UserRepository userRepository = plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(UserRepository.class);

            int taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
                    new PermissionCheckingThread(player.getUniqueId(), userCache, userRepository),
                    20L,
                    20L
            ).getTaskId();

            this.taskIds.put(player.getUniqueId(), taskId);
        }
    }

    public void processPlayerLeave(@NotNull UUID uuid) throws ExecutionException {
        PermissionCache permissionCache = plugin.getServiceRegistry().get(CacheService.class)
                .get(PermissionCache.class);

        // remove from cache
        permissionCache.get(uuid).remove();
        permissionCache.release(uuid);

        Integer taskId = this.taskIds.get(uuid);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
            this.taskIds.remove(uuid);
        }
    }

    public void processTemporaryGroupChange(User user) {
        // check if user now contains a temporary group. If so, start a custom thread to watch group
        // if not already running.
        // otherwise if user does not have any timed group anymore: stop the thread
        boolean containsTemporary = false;
        for (Long value : user.getGroups().values()) {
            if (value != 0) {
                containsTemporary = true;
                break;
            }
        }
        boolean runsTask = this.taskIds.containsKey(user.getUuid());

        if (containsTemporary && !this.taskIds.containsKey(user.getUuid())) {
            // start thread
            UserCache userCache = plugin.getServiceRegistry().get(CacheService.class)
                    .get(UserCache.class);
            UserRepository userRepository = plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(UserRepository.class);

            int taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
                    new PermissionCheckingThread(user.getUuid(), userCache, userRepository),
                    20L,
                    20L).getTaskId();

            this.taskIds.put(user.getUuid(), taskId);
        } else if (!containsTemporary && runsTask) {
            // stop thread
            Bukkit.getScheduler().cancelTask(this.taskIds.get(user.getUuid()));
            this.taskIds.remove(user.getUuid());
        }
    }

    public void removeGroupFromAllOnlineUsers(Group group) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // use try-catch here as we don't want to interrupt looping
            try {
                User user = plugin.getServiceRegistry().get(CacheService.class)
                        .get(UserCache.class)
                        .get(player.getUniqueId())
                        .orElseThrow();

                user.removeGroup(group);
            } catch (ExecutionException | NoSuchElementException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateGroup(Group group) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // use try-catch here as we don't want to interrupt looping
            try {
                User user = plugin.getServiceRegistry().get(CacheService.class)
                        .get(UserCache.class)
                        .get(player.getUniqueId())
                        .orElseThrow();

                if (!user.hasGroup(group)) continue;
                updateUser(user);
            } catch (ExecutionException | NoSuchElementException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateUser(User user) throws ExecutionException {
        // fetch attachment
        PermissionAttachment attachment = plugin.getServiceRegistry().get(CacheService.class)
                .get(PermissionCache.class)
                .get(user.getUuid());

        Map<String, Boolean> permissions = attachment.getPermissions();
        List<String> donePermissions = new ArrayList<>(permissions.size());

        // go through all permissions of user and apply them to the player object
        for (Group group : user.getGroups().keySet()) {
            for (Permission permission : group.getPermissions()) {
                if (donePermissions.contains(permission.getNode())) continue;

                Boolean modeOfCurrentPermission = permissions.get(permission.getNode());
                if (modeOfCurrentPermission == null || modeOfCurrentPermission != permission.getMode()) {
                    attachment.setPermission(permission.getNode(), permission.getMode());
                }
                permissions.remove(permission.getNode());
                donePermissions.add(permission.getNode());
            }
        }

        // any permissions, which are set on the player object, but are not in the groups of the
        //  user anymore: remove them
        for (String permissionNode : permissions.keySet()) {
            attachment.unsetPermission(permissionNode);
        }
    }

}
