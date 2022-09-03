package net.playlegend.domain;

import com.google.common.collect.ImmutableMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.playlegend.misc.Publisher;

public class User extends Publisher<User.Operation, User> {

    private final UUID uuid;
    private final String name;
    private Map<String, Long> groups;

    public User(UUID uuid, String name) {
        this(uuid, name, new LinkedHashMap<>());
    }

    public User(UUID uuid, String name, Map<String, Long> groups) {
        super(Operation.GROUP_CHANGE);
        this.uuid = uuid;
        this.name = name;
        this.groups = groups;
    }

    public String getMainGroupName() {
        if (groups.isEmpty()) return "";

        return groups.keySet().iterator().next();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void addGroup(int index, String groupName, long validUntil) {
        if (index > this.groups.size())
            throw new IndexOutOfBoundsException(index + " out of bounds " + this.groups.size());

        if (index == this.groups.size()) {
            this.groups.put(groupName, validUntil);
            this.notifySubscribers(Operation.GROUP_CHANGE, this);
        } else {
            Map<String, Long> updatedGroups = new LinkedHashMap<>(this.groups.size() + 1);
            int i = 0;
            for (Map.Entry<String, Long> entry : this.groups.entrySet()) {
                System.out.println("Entry: " + entry);
                System.out.println("Current index: " + i);
                System.out.println("Needed index: " + index);
                if (i == index)
                    updatedGroups.put(groupName, validUntil);

                updatedGroups.put(entry.getKey(), entry.getValue());
                i++;
            }
            System.out.println("Before: " + groups);
            System.out.println("Now: " + updatedGroups);
            setGroups(updatedGroups);
        }
    }

    public void updateValidUntil(String groupName, long validUntil) {
        if (this.groups.replace(groupName, validUntil) == null) {
            this.notifySubscribers(Operation.GROUP_CHANGE, this);
        }
    }

    public void removeGroup(String group) {
        if (this.groups.remove(group) != null)
            this.notifySubscribers(Operation.GROUP_CHANGE, this);
    }

    public ImmutableMap<String, Long> getGroups() {
        return ImmutableMap.copyOf(groups);
    }

    public void setGroups(Map<String, Long> groups) {
        this.groups = groups;
        this.notifySubscribers(Operation.GROUP_CHANGE, this);
    }

    public boolean hasGroup(String groupName) {
        return this.groups.containsKey(groupName);
    }

    public boolean hasGroupPermanent(String groupName) {
        Long validUntil;
        return (((validUntil = groups.get(groupName)) != null) && validUntil == 0);
    }

    public boolean hasGroupTemporary(String groupName) {
        Long validUntil;
        return (((validUntil = groups.get(groupName)) != null) && validUntil != 0);
    }

    public long getGroupValidUntil(String name) {
        if (!hasGroup(name))
            throw new IllegalArgumentException("player does not have group!");

        return groups.get(name);
    }

    public enum Operation {
        GROUP_CHANGE,
        ;
    }

}
