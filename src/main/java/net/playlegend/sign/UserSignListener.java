package net.playlegend.sign;

import java.sql.SQLException;
import net.playlegend.LegendPerm;
import net.playlegend.domain.User;
import net.playlegend.misc.Subscriber;

public class UserSignListener implements Subscriber<User, User.Operation> {

    private final LegendPerm plugin;

    public UserSignListener(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @Override
    public void update(User.Operation type, User data) {
        switch (type) {
            case GROUP_ADD:
            case GROUP_REMOVE:
            case GROUP_ADD_TEMPORARY:
            case GROUP_REMOVE_TEMPORARY:
                try {
                    plugin.getServiceRegistry().get(SignService.class)
                            .updateAllSignsForUser(data);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

}
