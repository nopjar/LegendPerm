package net.playlegend.domain;

import java.util.Iterator;
import java.util.Set;
import net.playlegend.misc.Publisher;
import org.jetbrains.annotations.NotNull;

public class Group extends Publisher<Group.Operation, Group> {

    private final String name;
    private int weight;
    private String prefix;
    private String suffix;
    private final Set<Permission> permissions;

    public Group(String name, int weight, String prefix, String suffix, Set<Permission> permissions) {
        this.name = name;
        this.weight = weight;
        this.prefix = prefix;
        this.suffix = suffix;
        this.permissions = permissions;
    }

    public void changeProperty(@NotNull Property property, String value) {
        switch (property) {
            case WEIGHT -> setWeight(Integer.parseInt(value));
            case PREFIX -> setPrefix(value);
            case SUFFIX -> setSuffix(value);
            default -> throw new IllegalArgumentException("Unknown property: " + property);
        }
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
        this.notifySubscribers(Operation.WEIGHT_CHANGE, this);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.notifySubscribers(Operation.PREFIX_CHANGE, this);
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
        this.notifySubscribers(Operation.SUFFIX_CHANGE, this);
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void addPermission(Permission permission) {
        boolean contains = false;
        boolean update = false;
        // go through all permissions to check if group already has permission and if so, check if
        //  mode is the same
        for (Permission groupPerm : this.permissions) {
            if (groupPerm.getNode().equalsIgnoreCase(permission.getNode())) {
                // found permission, checking for mode
                contains = true;
                if (groupPerm.getMode() != permission.getMode()) {
                    groupPerm.setMode(permission.getMode());
                    update = true;
                }
                break;
            }
        }
        // if group does not already contain the permission: add to set
        if (!contains)
            update = this.permissions.add(permission);

        // if set was modified: notify subscribers
        if (update)
            this.notifySubscribers(Operation.PERMISSION_CHANGE, this);
    }

    public void removePermission(String permNode) {
        Iterator<Permission> iterator = this.permissions.iterator();
        while (iterator.hasNext()) {
            String permission = iterator.next().getNode();
            if (permission.equalsIgnoreCase(permNode)) {
                iterator.remove();
                this.notifySubscribers(Operation.PERMISSION_CHANGE, this);
                break;
            }
        }
    }

    public void delete() {
        this.notifySubscribers(Operation.DELETE, this);
    }

    @Override
    public String toString() {
        return "Group{" +
               "name='" + name + '\'' +
               ", weight=" + weight +
               ", prefix='" + prefix + '\'' +
               ", suffix='" + suffix + '\'' +
               ", permissions=" + permissions +
               '}';
    }

    public enum Property {
        PREFIX,
        SUFFIX,
        WEIGHT;

        private static final Property[] VALUES;
        public static final String[] VALUES_AS_STRINGS;

        static {
            VALUES = values();

            VALUES_AS_STRINGS = new String[VALUES.length];
            for (int i = 0; i < VALUES.length; i++) {
                VALUES_AS_STRINGS[i] = VALUES[i].name();
            }
        }

        public static Property find(@NotNull String name) {
            for (Property property : VALUES) {
                if (property.name().equalsIgnoreCase(name)) {
                    return property;
                }
            }

            return null;
        }

    }

    public enum Operation {
        PREFIX_CHANGE,
        SUFFIX_CHANGE,
        PERMISSION_CHANGE,
        WEIGHT_CHANGE,
        DELETE,
        ;
    }

}
