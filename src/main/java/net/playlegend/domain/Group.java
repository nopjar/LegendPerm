package net.playlegend.domain;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Group {

    private String name;
    private int weight;
    private String prefix;
    private String suffix;
    private Set<Permission> permissions;

    public boolean hasPermission(@NotNull Permission permission) {
        return hasPermission(permission.node());
    }

    public boolean hasPermission(@NotNull String permission) {
        for (Permission perm : this.permissions) {
            if (perm.node().equals(permission)) {
                return perm.mode();
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

}
