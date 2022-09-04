package net.playlegend.permission;

import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.domain.User;
import net.playlegend.misc.Subscriber;

public class UserPermissionListener implements Subscriber<User, User.Operation> {

    private final LegendPerm plugin;

    public UserPermissionListener(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @Override
    public void update(User.Operation type, User user) {
        PermissionService permissionService = plugin.getServiceRegistry().get(PermissionService.class);
        switch (type) {
            case GROUP_ADD_TEMPORARY:
            case GROUP_CHANGE_TO_PERMANENT:
            case GROUP_REMOVE_TEMPORARY:
                permissionService.processTemporaryGroupChange(user);
            case GROUP_ADD:
            case GROUP_REMOVE:
            case GROUP_EXPIRATION_EXTENDED:
            case GROUP_EXPIRATION_REDUCED:
                try {
                    permissionService.updateUser(user);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

}
