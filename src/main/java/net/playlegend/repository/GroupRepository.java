package net.playlegend.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import net.playlegend.domain.Group;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GroupRepository extends Repository {

    @Language("MariaDB")
    private static final String CREATE_GROUP = """
            INSERT INTO `group`
            (name, weight) VALUES (?, ?);
            """;

    @Language("MariaDB")
    private static final String SELECT_GROUP_BY_NAME = """
            SELECT `group`.id as group_id, `group`.name as group_name, `group`.weight as group_weight, gp.permission, gp.type
            FROM `group`
            INNER JOIN group_permissions gp on `group`.id = gp.group_id
            WHERE group_id = ?;
            """;

    @Language("MariaDB")
    private static final String UPDATE_GROUP = """
            UPDATE `group`
            SET name = ?, weight = ?
            WHERE id = ?;
            """;

    @Language("MariaDB")
    private static final String DELETE_GROUP = """
            DELETE FROM `group`
            WHERE id = ?;
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

    public GroupRepository() {
    }

    public Group createGroup(String name, int weight) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_GROUP, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, name);
            statement.setInt(2, weight);

            statement.executeUpdate();
            try (ResultSet set = statement.getGeneratedKeys()) {
                if (!set.next())
                    throw new SQLException("Failed to retrieve auto generated key for group: " + name + ":" + weight);

                return new Group(set.getInt(1), name, weight, new HashSet<>());
            }
        }
    }

    @Nullable
    public Group selectGroup(int id) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_GROUP_BY_NAME)) {

            statement.setInt(1, id);


            try (ResultSet set = statement.executeQuery()) {
                if (!set.next()) {
                    return null;
                }

                String groupName = "";
                int groupWeight = 0;
                Set<Group.Permission> permissions = new HashSet<>();
                boolean firstRun = true;
                // using do-while, so we won't miss the first row in set
                do {
                    // load overall information which not change in select (e.g. name of group)
                    if (firstRun) {
                        groupName = set.getString("group_name");
                        groupWeight = set.getInt("group_weight");
                        firstRun = false;
                    }

                    // load information about group permissions
                    String permissionString = set.getString("permission");
                    boolean permissionType = set.getBoolean("type");
                    permissions.add(new Group.Permission(permissionString, permissionType));
                } while (set.next());

                return new Group(id, groupName, groupWeight, permissions);
            }
        }
    }

    public void updateGroup(@NotNull Group group) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_GROUP)) {

            statement.setString(1, group.getName());
            statement.setInt(2, group.getWeight());
            statement.setInt(3, group.getId());

            statement.executeUpdate();
        }
    }

    public void deleteGroup(@NotNull Group group) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_GROUP)) {

            statement.setInt(1, group.getId());

            statement.executeUpdate();
        }
    }

    public void updatePermissionInGroup(@NotNull Group group, String permission, boolean mode) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(ADD_PERM_TO_GROUP)) {

            statement.setInt(1, group.getId());
            statement.setString(2, permission);
            statement.setBoolean(3, mode);
            statement.setBoolean(4, mode);

            statement.executeUpdate();
        }
    }

    public void revokePermissionFromGroup(@NotNull Group group, String permission) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(REVOKE_PERM_FROM_GROUP)) {

            statement.setInt(1, group.getId());
            statement.setString(2, permission);

            statement.executeUpdate();
        }
    }

}
