package net.playlegend.repository;

import com.zaxxer.hikari.HikariConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import net.playlegend.domain.Group;
import net.playlegend.domain.Permission;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GroupRepository extends Repository {

    @Language("MariaDB")
    private static final String CREATE_GROUP = """
            INSERT INTO `group`
            (name, weight, prefix, suffix) VALUES (?, ?, ?, ?);
            """;

    @Language("MariaDB")
    private static final String SELECT_GROUP_BY_NAME = """
            SELECT `group`.name as group_name, `group`.weight as group_weight, `group`.prefix as group_prefix, `group`.suffix as group_suffix, gp.permission, gp.type
            FROM `group`
            INNER JOIN group_permissions gp on `group`.name = gp.group_id
            WHERE `group`.name = ?;
            """;

    // TODO: 01/09/2022 update prefix and suffix
    @Language("MariaDB")
    private static final String UPDATE_GROUP = """
            UPDATE `group`
            SET weight = ?
            WHERE name = ?;
            """;

    @Language("MariaDB")
    private static final String DELETE_GROUP = """
            DELETE FROM `group`
            WHERE name = ?;
            """;

    @Language("MariaDB")
    private static final String ADD_PERM_TO_GROUP = """
            INSERT INTO group_permissions
            (group_id, permission, type) VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE type = ?;
            """;

    @Language("MariaDB")
    private static final String REVOKE_PERM_FROM_GROUP = """
            DELETE FROM group_permissions
            WHERE group_id = ? AND permission = ?;
            """;

    public GroupRepository(HikariConfig config) {
        super(config);
    }

    public Group createGroup(String name, int weight, String prefix, String suffix) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_GROUP)) {

            statement.setString(1, name);
            statement.setInt(2, weight);
            statement.setString(3, prefix);
            statement.setString(4, suffix);

            statement.executeUpdate();
            return new Group(name, weight, prefix, suffix, new HashSet<>());
        }
    }

    @Nullable
    public Group selectGroupByName(String name) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_GROUP_BY_NAME)) {

            statement.setString(1, name);

            try (ResultSet set = statement.executeQuery()) {
                if (!set.next()) {
                    return null;
                }

                String groupName = "";
                int groupWeight = 0;
                String groupPrefix = "";
                String groupSuffix = "";
                Set<Permission> permissions = new HashSet<>();
                boolean firstRun = true;
                // using do-while, so we won't miss the first row in set
                do {
                    // load overall information which not change in select (e.g. name of group)
                    if (firstRun) {
                        groupName = set.getString("group_name"); // load name again to ensure case matching
                        groupWeight = set.getInt("group_weight");
                        groupPrefix = set.getString("group_prefix");
                        groupSuffix = set.getString("group_suffix");
                        firstRun = false;
                    }

                    // load information about group permissions
                    String permissionString = set.getString("permission");
                    boolean permissionType = set.getBoolean("type");
                    permissions.add(new Permission(permissionString, permissionType));
                } while (set.next());

                return new Group(groupName, groupWeight, groupPrefix, groupSuffix, permissions);
            }
        }
    }

    public void updateGroup(@NotNull Group group) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_GROUP)) {

            statement.setInt(1, group.getWeight());
            statement.setString(2, group.getName());

            statement.executeUpdate();
        }
    }

    public void deleteGroup(@NotNull Group group) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_GROUP)) {

            statement.setString(1, group.getName());

            statement.executeUpdate();
        }
    }

    public void updatePermissionInGroup(@NotNull Group group, String permission, boolean mode) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(ADD_PERM_TO_GROUP)) {

            statement.setString(1, group.getName());
            statement.setString(2, permission);
            statement.setBoolean(3, mode);
            statement.setBoolean(4, mode);

            statement.executeUpdate();
        }
    }

    public void revokePermissionFromGroup(@NotNull Group group, String permission) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(REVOKE_PERM_FROM_GROUP)) {

            statement.setString(1, group.getName());
            statement.setString(2, permission);

            statement.executeUpdate();
        }
    }

}
