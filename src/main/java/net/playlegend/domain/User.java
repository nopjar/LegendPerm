package net.playlegend.domain;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.UUID;
import net.playlegend.misc.Publisher;

public class User extends Publisher<User.Operation, User> {

    private final UUID uuid;
    private final String name;
    private Map<Group, Long> groups;

    public User(UUID uuid, String name, Map<Group, Long> groups) {
        super(Operation.GROUP_CHANGE);
        this.uuid = uuid;
        this.name = name;
        this.groups = groups;
    }

    public Group getMainGroup() {
        if (groups.isEmpty())
            throw new IllegalStateException("player has no groups");

        return groups.keySet().iterator().next();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void addGroup(Group group, long validUntil) {
        if (this.groups.put(group, validUntil) == null) {
            this.notifySubscribers(Operation.GROUP_CHANGE, this);
        }
    }

    public void updateValidUntil(Group group, long validUntil) {
        if (this.groups.replace(group, validUntil) == null) {
            this.notifySubscribers(Operation.GROUP_CHANGE, this);
        }
    }

    public void removeGroup(Group group) {
        if (this.groups.remove(group) != null)
            this.notifySubscribers(Operation.GROUP_CHANGE, this);
    }

    public ImmutableMap<Group, Long> getGroups() {
        return ImmutableMap.copyOf(groups);
    }

    public void setGroups(Map<Group, Long> groups) {
        this.groups = groups;
        this.notifySubscribers(Operation.GROUP_CHANGE, this);
    }

    public boolean hasGroup(Group group) {
        return this.groups.containsKey(group);
    }

    public boolean hasGroup(String groupName) {
        for (Group group : this.groups.keySet()) {
            if (group.getName().equalsIgnoreCase(groupName))
                return true;
        }

        return false;
    }

    public boolean hasGroupPermanent(Group group) {
        Long validUntil;
        return (((validUntil = groups.get(group)) != null) && validUntil == 0);
    }

    public boolean hasGroupPermanent(String groupName) {
        for (Map.Entry<Group, Long> entry : this.groups.entrySet()) {
            if (entry.getKey().getName().equalsIgnoreCase(groupName)) {
                return (entry.getValue() == 0);
            }
        }

        return false;
    }

    public long getGroupValidUntil(Group group) {
        if (!hasGroup(group))
            throw new IllegalArgumentException("player does not have group!");

        return groups.get(group);
    }

    public long getGroupValidUntil(String groupName) {
        for (Map.Entry<Group, Long> entry : this.groups.entrySet()) {
            if (entry.getKey().getName().equalsIgnoreCase(groupName)) {
                return entry.getValue();
            }
        }

        throw new IllegalArgumentException("user does not have group " + groupName);
    }

    public enum Operation {
        GROUP_CHANGE,
        ;
    }

}
