package net.playlegend.observer;

import net.playlegend.LegendPerm;
import net.playlegend.domain.Group;
import net.playlegend.domain.User;
import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.exception.ServiceShutdownException;
import net.playlegend.service.Service;
import org.jetbrains.annotations.NotNull;

public class PermissionService extends Service {

    public PermissionService(LegendPerm plugin) {
        super(plugin);
    }

    public void register(@NotNull Group group) {
        group.subscribe(new GroupListener(), Group.Operation.PERMISSION_CHANGE, Group.Operation.WEIGHT_CHANGE);
    }

    public void register(@NotNull User user) {
        user.subscribe(new UserListener(), User.Operation.GROUP_CHANGE);
    }

    @Override
    public void initialize() throws ServiceInitializeException {

    }

    @Override
    public void shutdown() throws ServiceShutdownException {

    }

}
