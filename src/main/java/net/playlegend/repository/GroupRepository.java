package net.playlegend.repository;

import com.zaxxer.hikari.HikariConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.playlegend.domain.Group;
import net.playlegend.domain.Permission;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("SqlResolve")
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
            LEFT JOIN group_permissions gp on `group`.name = gp.group_id
            WHERE `group`.name = ?;
            """;

    @Language("MariaDB")
    private static final String UPDATE_GROUP = """
            UPDATE `group`
            SET weight = ?, prefix = ?, suffix = ?
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

    @Language("MariaDB")
    private static final String SELECT_ALL_GROUP_NAMES = """
            SELECT `name`
            FROM `group`
            ORDER BY `name`;
            """;

    public GroupRepository(HikariConfig config) {
        super(config);
    }

    @Override
    public void prepareStatements() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            String query = prepareStatementBuilder("create_group", CREATE_GROUP) +
                           prepareStatementBuilder("select_group_by_name", SELECT_GROUP_BY_NAME) +
                           prepareStatementBuilder("update_group", UPDATE_GROUP) +
                           prepareStatementBuilder("delete_group", DELETE_GROUP) +
                           prepareStatementBuilder("add_perm_to_group", ADD_PERM_TO_GROUP) +
                           prepareStatementBuilder("revoke_perm_from_group", REVOKE_PERM_FROM_GROUP) +
                           prepareStatementBuilder("select_all_group_names", SELECT_ALL_GROUP_NAMES);

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.executeUpdate();
            }
        }
    }

    private String prepareStatementBuilder(String name, String query) {
        @Language("MariaDB")
        String s = "SET @sql := '" + query + "';" +
                   "PREPARE `" + name + "` FROM @sql;";

        return s;
    }

    public Group createGroup(String name, int weight, String prefix, String suffix) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement("EXECUTE `create_group` USING ?, ?, ?, ?;")) {

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
             PreparedStatement statement = connection.prepareStatement("EXECUTE `select_group_by_name` USING ?;")) {

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
                    if (permissionString == null) break; // break out of loop as group does not have any permissions!
                    boolean permissionType = set.getBoolean("type");
                    permissions.add(new Permission(permissionString, permissionType));
                } while (set.next());

                return new Group(groupName, groupWeight, groupPrefix, groupSuffix, permissions);
            }
        }
    }

    public void updateGroup(@NotNull Group group) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement("EXECUTE `update_group` USING ?, ?, ?, ?;")) {

            statement.setInt(1, group.getWeight());
            statement.setString(2, group.getPrefix());
            statement.setString(3, group.getSuffix());
            statement.setString(4, group.getName());

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
             PreparedStatement statement = connection.prepareStatement("EXECUTE `add_perm_to_group` USING ?, ?, ?, ?;")) {

            statement.setString(1, group.getName());
            statement.setString(2, permission);
            statement.setBoolean(3, mode);
            statement.setBoolean(4, mode);

            statement.executeUpdate();
        }
    }

    public void revokePermissionFromGroup(@NotNull Group group, String permission) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement("EXECUTE `revoke_perm_from_group` USING ?, ?;")) {

            statement.setString(1, group.getName());
            statement.setString(2, permission);

            statement.executeUpdate();
        }
    }

    public List<String> selectAllGroupNames() throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement("EXECUTE `select_all_group_names`;")) {

            List<String> list = new ArrayList<>();
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    list.add(set.getString("name"));
                }
            }

            return list;
        }
    }

}
