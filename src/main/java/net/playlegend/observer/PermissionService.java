package net.playlegend.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
import net.playlegend.service.Service;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class PermissionService extends Service {

    public PermissionService(LegendPerm plugin) {
        super(plugin);
    }

    @Override
    public void initialize() throws ServiceInitializeException {

    }

    @Override
    public void shutdown() throws ServiceShutdownException {

    }

    public void removeGroup(Group group) {
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
        PermissionAttachment attachment = plugin.getServiceRegistry().get(CacheService.class)
                .get(PermissionCache.class)
                .get(user.getUuid());

        Map<String, Boolean> permissions = attachment.getPermissions();
        List<String> donePermissions = new ArrayList<>(permissions.size());

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

        for (String permissionNode : permissions.keySet()) {
            attachment.unsetPermission(permissionNode);
        }
    }

}
