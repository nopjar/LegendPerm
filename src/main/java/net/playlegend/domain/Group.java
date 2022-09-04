package net.playlegend.domain;

import java.util.Iterator;
import java.util.Set;
import net.playlegend.misc.Publisher;
import org.jetbrains.annotations.NotNull;

public class Group extends Publisher<Group.Operation, Group> {

    private String name;
    private int weight;
    private String prefix;
    private String suffix;
    private Set<Permission> permissions;

    public Group(String name, int weight, String prefix, String suffix, Set<Permission> permissions) {
        super(Operation.values());
        this.name = name;
        this.weight = weight;
        this.prefix = prefix;
        this.suffix = suffix;
        this.permissions = permissions;
    }

    public boolean hasPermission(@NotNull Permission permission) {
        return hasPermission(permission.getNode());
    }

    public boolean hasPermission(@NotNull String permission) {
        for (Permission perm : this.permissions) {
            if (perm.getNode().equals(permission)) {
                return perm.getMode();
            }
        }

        return false;
    }

    public void changeProperty(@NotNull Property property, String value) {
        switch (property) {
            case WEIGHT -> weight = Integer.parseInt(value);
            case PREFIX -> prefix = value;
            case SUFFIX -> suffix = value;
            default -> throw new IllegalArgumentException("Unknown property: " + property);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.notifySubscribers(Operation.PROPERTY_CHANGE, this);
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
        this.notifySubscribers(Operation.PROPERTY_CHANGE, this);
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
        this.notifySubscribers(Operation.PROPERTY_CHANGE, this);
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void addPermission(Permission permission) {
        boolean contains = false;
        boolean update = false;
        for (Permission groupPerm : this.permissions) {
            if (groupPerm.getNode().equalsIgnoreCase(permission.getNode())) {
                contains = true;
                if (groupPerm.getMode() != permission.getMode()) {
                    groupPerm.setMode(permission.getMode());
                    update = true;
                }
                break;
            }
        }
        if (!contains)
            update = this.permissions.add(permission);

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
    protected void finalize() throws Throwable {
        System.out.println(name + " collected!");
        super.finalize();
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
        PROPERTY_CHANGE,
        PERMISSION_CHANGE,
        WEIGHT_CHANGE,
        DELETE
        ;
    }

}
