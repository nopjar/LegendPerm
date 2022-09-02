package net.playlegend.domain;

import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class User {

    private final UUID uuid;
    private final String name;
    private Set<Group> groups;

    public String getPrefix() {
        if (groups.isEmpty()) return "";

        return groups.iterator().next().getPrefix();
    }

}
