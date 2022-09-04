package net.playlegend.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.GroupCache;
import net.playlegend.domain.Group;
import net.playlegend.domain.User;
import net.playlegend.misc.GroupWeightComparator;
import net.playlegend.permission.UserListener;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserRepository extends Repository {

    @Language("MariaDB")
    private static final String BASE_SELECT_USER = """
            SELECT user.uuid as user_id, user.name as user_name, ug.group_id as group_name, ug.valid_until
            FROM user
            LEFT JOIN users_groups ug on user.uuid = ug.user_id
            LEFT JOIN `group` g on ug.group_id = g.name
            """;

    @Language("MariaDB")
    private static final String SELECT_USER_BY_UUID = BASE_SELECT_USER + """
            WHERE user.uuid = ?
            ORDER BY g.weight DESC;
            """;

    @Language("MariaDB")
    private static final String SELECT_USER_BY_NAME = BASE_SELECT_USER + """
            WHERE user.name = ?
            ORDER BY g.weight DESC;
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

    public UserRepository(LegendPerm plugin, DataSource dataSource) {
        super(plugin, dataSource);
    }

    @Nullable
    public User selectUserByUUID(@NotNull UUID uuid) throws SQLException, ExecutionException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_UUID)) {

            statement.setString(1, uuid.toString());

            return selectUser(statement);
        }
    }

    @Nullable
    public User selectUserByName(@NotNull String name) throws SQLException, ExecutionException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_NAME)) {

            statement.setString(1, name);

            return selectUser(statement);
        }
    }

    private User selectUser(PreparedStatement statement) throws SQLException, ExecutionException {
        try (ResultSet set = statement.executeQuery()) {
            if (!set.next()) {
                return null;
            }

            UUID uuid = null;
            String userName = "";
            Map<Group, Long> groups = Collections.synchronizedMap(new TreeMap<>(new GroupWeightComparator()));
            boolean firstRun = true;
            GroupCache groupCache = plugin.getServiceRegistry().get(CacheService.class)
                    .get(GroupCache.class);
            long epochSeconds = ZonedDateTime.now().toEpochSecond();
            // using do-while, so we won't miss the first row in set
            do {
                // load overall information which change not (typically user information)
                if (firstRun) {
                    uuid = UUID.fromString(set.getString("user_id"));
                    userName = set.getString("user_name");
                    firstRun = false;
                }

                // load information about group
                String groupName = set.getString("group_name");
                if (groupName == null) break; // break out of loop as user does not have any groups!
                long validUntil = set.getLong("valid_until");
                // check if user still has the group, remove it if expired
                if (validUntil != 0 && validUntil < epochSeconds) {
                    removeUserFromGroup(uuid, groupName);
                    continue;
                }
                Group group = groupCache.get(groupName)
                        .orElseThrow();
                groups.put(group, validUntil);
            } while (set.next());

            User user = new User(uuid, userName, groups);
            user.subscribe(new UserListener(plugin), User.Operation.GROUP_CHANGE);
            return user;
        }
    }

    public void addUserToGroup(@NotNull UUID uuid, String groupName, long validUntil) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(GRANT_USER_AUTHORITY)) {

            statement.setString(1, uuid.toString());
            statement.setString(2, groupName);
            statement.setLong(3, validUntil);
            statement.setLong(4, validUntil);

            statement.executeUpdate();
        }
    }

    public void removeUserFromGroup(@NotNull UUID uuid, String groupName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(REVOKE_USER_AUTHORITY)) {

            statement.setString(1, uuid.toString());
            statement.setString(2, groupName);

            statement.executeUpdate();
        }
    }

    public void updateUser(@NotNull User user) throws SQLException {
        updateUser(user.getUuid(), user.getName());
    }

    public void updateUser(UUID uuid, String name) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_USER)) {

            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.setString(3, name);

            statement.executeUpdate();
        }
    }

}
