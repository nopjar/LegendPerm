package net.playlegend.domain;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Group {

    private int id;
    private String name;
    private int weight;
    private String prefix;
    private String suffix;
    private Set<Permission> permissions;

    /**
     * @param granted is either granted or denied
     */
    public record Permission(String permissionString, boolean granted) {
    }

}
