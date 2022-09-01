package net.playlegend.domain;

import java.util.Map;
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
    // value is the valid_until, if not existing -> 0
    private Map<Group, Long> groups;

}
