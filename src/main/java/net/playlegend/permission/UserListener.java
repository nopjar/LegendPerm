package net.playlegend.permission;

import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.domain.User;
import net.playlegend.misc.Subscriber;

public class UserListener implements Subscriber<User, User.Operation> {

    private final LegendPerm plugin;

    public UserListener(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @Override
    public void update(User.Operation type, User data) {
        switch (type) {
            case GROUP_CHANGE -> {
                try {
                    plugin.getServiceRegistry().get(PermissionService.class)
                            .updateUser(data);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
