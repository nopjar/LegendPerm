package net.playlegend.repository;

import com.zaxxer.hikari.HikariConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Predicate;
import net.playlegend.domain.Group;
import net.playlegend.domain.Permission;
import net.playlegend.domain.TemporaryGroup;
import net.playlegend.domain.User;
import net.playlegend.misc.GroupWeightComparator;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserRepository extends Repository {

    @Language("MariaDB")
    private static final String BASE_SELECT_USER = """
            SELECT user.uuid as user_id, user.name as user_name, g.name as group_name, g.weight as group_weight, g.prefix as group_prefix, g.suffix as group_suffix, ug.valid_until, gp.permission, gp.type
            FROM user
            LEFT JOIN users_groups ug on user.uuid = ug.user_id
            LEFT JOIN `group` g on ug.group_id = g.name
            LEFT JOIN group_permissions gp on g.name = gp.group_id
            """;

    @Language("MariaDB")
    private static final String SELECT_USER_BY_UUID = BASE_SELECT_USER + """
            WHERE user.uuid = ?
            ORDER BY group_weight DESC;
            """;

    @Language("MariaDB")
    private static final String SELECT_USER_BY_NAME = BASE_SELECT_USER + """
            WHERE user.name = ?
            ORDER BY group_weight DESC;
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
    public User selectUserByUUID(@NotNull UUID uuid) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_UUID)) {

            statement.setString(1, uuid.toString());

            return selectUser(statement);
        }
    }

    @Nullable
    public User selectUserByName(@NotNull String name) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_NAME)) {

            statement.setString(1, name);

            return selectUser(statement);
        }
    }

    private User selectUser(PreparedStatement statement) throws SQLException {
        try (ResultSet set = statement.executeQuery()) {
            if (!set.next()) {
                return null;
            }

            UUID uuid = null;
            String userName = "";
            Set<Group> groups = new TreeSet<>(new GroupWeightComparator());
            boolean firstRun = true;
            // using do-while, so we won't miss the first row in set
            do {
                // load overall information which change not (typically user information)
                if (firstRun) {
                    uuid = UUID.fromString(set.getString("user_id"));
                    userName = set.getString("user_name");
                    firstRun = false;
                }

                // load information about group
                Group group;
                String groupName = set.getString("group_name");
                if (groupName == null) break; // break out of loop as user does not have any groups!

                // check if group was already loaded once, if not -> fetch overall group information
                // otherwise use already fetched data
                if ((group = fetchFirst(groups, g -> g.getName().equals(groupName))) == null) {
                    int groupWeight = set.getInt("group_weight");
                    String groupPrefix = set.getString("group_prefix");
                    String groupSuffix = set.getString("group_suffix");

                    // if validUntil == 0, then it is a permanent group
                    long validUntil = set.getLong("valid_until");
                    if (validUntil == 0) {
                        group = new Group(groupName, groupWeight, groupPrefix, groupSuffix, new HashSet<>());
                    } else {
                        group = new TemporaryGroup(groupName, groupWeight, groupPrefix, groupSuffix, new HashSet<>(), validUntil);
                    }

                    groups.add(group);
                }

                Permission permission = new Permission(set.getString("permission"), set.getBoolean("type"));
                group.getPermissions().add(permission);
            } while (set.next());

            return new User(uuid, userName, groups);
        }
    }

    // finds the first item, if present, which fulfills the predicate
    private <T> T fetchFirst(Collection<T> list, Predicate<T> predicate) {
        return list.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    public void addUserToGroup(@NotNull UUID uuid, String groupName, long validUntil) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(GRANT_USER_AUTHORITY)) {

            statement.setString(1, uuid.toString());
            statement.setString(2, groupName);
            statement.setLong(3, validUntil);
            statement.setLong(4, validUntil);

            statement.executeUpdate();
        }
    }

    public void removeUserFromGroup(@NotNull UUID uuid, String groupName) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(REVOKE_USER_AUTHORITY)) {

            statement.setString(1, uuid.toString());
            statement.setString(2, groupName);

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
