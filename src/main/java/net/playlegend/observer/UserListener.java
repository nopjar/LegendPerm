package net.playlegend.observer;

import net.playlegend.domain.User;
import net.playlegend.misc.Subscriber;

public class UserListener implements Subscriber<User, User.Operation> {

    @Override
    public void update(User.Operation type, User data) {
        System.out.println("UPDATE: " + type.name());
    }

}
