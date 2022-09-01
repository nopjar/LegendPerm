package net.playlegend.repository;

import com.zaxxer.hikari.HikariConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.playlegend.domain.Group;
import net.playlegend.domain.Permission;
import net.playlegend.domain.TemporaryGroup;
import net.playlegend.domain.User;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserRepository extends Repository {

    @Language("MariaDB")
    private static final String SELECT_USER = """
            SELECT user.uuid as user_id, user.name as user_name, g.id as group_id, g.name as group_name, g.weight as group_weight, g.prefix as group_prefix, g.suffix as group_suffix, ug.valid_until, gp.permission, gp.type
            FROM user
            INNER JOIN users_groups ug on user.uuid = ug.user_id
            INNER JOIN `group` g on ug.group_id = g.id
            INNER JOIN group_permissions gp on g.id = gp.group_id
            WHERE user.uuid = ?;
            """;

    @Language("MariaDB")
    private static final String GRANT_USER_AUTHORITY = """
            INSERT INTO users_groups
            (user_id, group_id, valid_until) VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE valid_until = ?;
            """;

    @Language("MariaDB")
    private static final String REVOKE_USER_AUTHORITY = """
            DELETE FROM users_groups
            WHERE user_id = ? AND group_id = ?;
            """;

    @Language("MariaDB")
    private static final String UPDATE_USER = """
            INSERT INTO user
            (uuid, name) VALUES (?, ?)
            ON DUPLICATE KEY UPDATE name = ?;
            """;

    public UserRepository(HikariConfig config) {
        super(config);
    }

    @Nullable
    public User selectUser(@NotNull UUID uuid) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER)) {

            statement.setString(1, uuid.toString());

            try (ResultSet set = statement.executeQuery()) {
                if (!set.next()) {
                    return null;
                }

                String userName = "";
                List<Group> groups = new ArrayList<>();
                boolean firstRun = true;
                // using do-while, so we won't miss the first row in set
                do {
                    // load overall information which change not (typically user information)
                    if (firstRun) {
                        userName = set.getString("user_name");
                        firstRun = false;
                    }

                    // load information about group
                    Group group;
                    int groupId = set.getInt("group_id");
                    // check if group was already loaded once, if not -> fetch overall group information
                    // otherwise use already fetched data
                    if ((group = fetchFirst(groups, group1 -> group1.getId() == groupId)) == null) {
                        String groupName = set.getString("group_name");
                        int groupWeight = set.getInt("group_weight");
                        String groupPrefix = set.getString("group_prefix");
                        String groupSuffix = set.getString("group_suffix");

                        // if validUntil == 0, then it is a permanent group
                        long validUntil = set.getLong("valid_until");
                        if (validUntil == 0) {
                            group = new Group(groupId, groupName, groupWeight, groupPrefix, groupSuffix, new HashSet<>());
                        } else {
                            group = new TemporaryGroup(groupId, groupName, groupWeight, groupPrefix, groupSuffix, new HashSet<>(), validUntil);
                        }

                        groups.add(group);
                    }

                    Permission permission = new Permission(set.getString("permission"), set.getBoolean("type"));
                    group.getPermissions().add(permission);
                } while (set.next());

                return new User(uuid, userName, groups);
            }
        }
    }

    // finds the first item, if present, which fulfills the predicate
    private <T> T fetchFirst(Collection<T> list, Predicate<T> predicate) {
        return list.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    public void addUserToGroup(@NotNull UUID uuid, int groupId, long validUntil) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(GRANT_USER_AUTHORITY)) {

            statement.setString(1, uuid.toString());
            statement.setInt(2, groupId);
            statement.setLong(3, validUntil);
            statement.setLong(4, validUntil);

            statement.executeUpdate();
        }
    }

    public void removeUserFromGroup(@NotNull UUID uuid, int groupId) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(REVOKE_USER_AUTHORITY)) {

            statement.setString(1, uuid.toString());
            statement.setInt(2, groupId);

            statement.executeUpdate();
        }
    }

    public void updateUser(UUID uuid, String name) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_USER)) {

            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.setString(3, name);

            statement.executeUpdate();
        }
    }

}
