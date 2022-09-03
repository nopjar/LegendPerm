package net.playlegend.observer;

import net.playlegend.domain.Group;
import net.playlegend.misc.Subscriber;

public class GroupListener implements Subscriber<Group, Group.Operation> {

    @Override
    public void update(Group.Operation type, Group data) {
        System.out.println("UPDATE: " + type.name());
    }

}
