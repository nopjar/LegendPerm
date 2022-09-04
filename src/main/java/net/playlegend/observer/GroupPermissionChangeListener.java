package net.playlegend.observer;

import net.playlegend.LegendPerm;
import net.playlegend.domain.Group;
import net.playlegend.misc.Subscriber;

public class GroupPermissionChangeListener implements Subscriber<Group, Group.Operation> {

    private final LegendPerm plugin;

    public GroupPermissionChangeListener(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @Override
    public void update(Group.Operation type, Group data) {
        switch (type) {
            case WEIGHT_CHANGE:
            case PERMISSION_CHANGE:
                plugin.getServiceRegistry().get(PermissionService.class)
                        .updateGroup(data);
                break;
            case DELETE:
                plugin.getServiceRegistry().get(PermissionService.class)
                        .removeGroup(data);
            case PROPERTY_CHANGE:
                break;
        }
    }

}
