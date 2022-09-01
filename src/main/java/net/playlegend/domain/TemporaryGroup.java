package net.playlegend.domain;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class TemporaryGroup extends Group {

    private long validUntil;

    public TemporaryGroup(String name, int weight, String prefix, String suffix, Set<Permission> permissions, long validUntil) {
        super(name, weight, prefix, suffix, permissions);
        this.validUntil = validUntil;
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        if (validUntil < System.currentTimeMillis()) return false;

        return super.hasPermission(permission);
    }

}
