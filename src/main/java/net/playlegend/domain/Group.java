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

}
