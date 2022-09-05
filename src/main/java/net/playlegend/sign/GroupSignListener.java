package net.playlegend.sign;

import java.sql.SQLException;
import net.playlegend.LegendPerm;
import net.playlegend.domain.Group;
import net.playlegend.misc.Subscriber;

public class GroupSignListener implements Subscriber<Group, Group.Operation> {

    private final LegendPerm plugin;

    public GroupSignListener(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @Override
    public void update(Group.Operation type, Group data) {
        if (type != Group.Operation.PREFIX_CHANGE) return;

        try {
            plugin.getServiceRegistry().get(SignService.class)
                    .updateAllSignsForGroup(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
