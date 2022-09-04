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
import net.playlegend.LegendPerm;
import net.playlegend.domain.Group;
import net.playlegend.domain.Permission;
import net.playlegend.observer.GroupPermissionChangeListener;
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
            SELECT `group`.name as group_name, `group`.weight as group_weight, `group`.prefix as group_prefix, `group`.suffix as group_suffix, gp.permission, gp.mode
            FROM `group`
            LEFT JOIN group_permissions gp on `group`.name = gp.group_id
            WHERE `group`.name = ?;
            """;

    @Language("MariaDB")
    private static final String SELECT_ALL_GROUPS = """
            SELECT `group`.name as group_name, `group`.weight as group_weight, `group`.prefix as group_prefix, `group`.suffix as group_suffix, gp.permission, gp.mode
            FROM `group`
            LEFT JOIN group_permissions gp on `group`.name = gp.group_id
            ORDER BY `group`.name
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
            (group_id, permission, mode) VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE mode = ?;
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

    public GroupRepository(LegendPerm plugin, HikariConfig config) {
        super(plugin, config);
    }

    // no group returned on purpose
    //  reason: we do not want to clutter up things with subscribers and the cache
    public void createGroup(String name, int weight, String prefix, String suffix) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_GROUP)) {

            statement.setString(1, name);
            statement.setInt(2, weight);
            statement.setString(3, prefix);
            statement.setString(4, suffix);

            statement.executeUpdate();
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
                    if (permissionString == null) break; // break out of loop as group does not have any permissions!
                    boolean permissionMode = set.getBoolean("mode");
                    permissions.add(new Permission(permissionString, permissionMode));
                } while (set.next());

                return getGroup(groupName, groupWeight, groupPrefix, groupSuffix, permissions);
            }
        }
    }

    public List<Group> selectAllGroups() throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_GROUPS)) {

            List<Group> list = new ArrayList<>();
            try (ResultSet set = statement.executeQuery()) {
                String groupName = "";
                int groupWeight = 0;
                String groupPrefix = "";
                String groupSuffix = "";
                Set<Permission> permissions = new HashSet<>();
                boolean firstRun = true;
                while (set.next()) {
                    String newName = set.getString("group_name");
                    if (!groupName.equals(newName) || firstRun) {
                        if (!firstRun) {
                            // add previous data to list
                            list.add(getGroup(groupName, groupWeight, groupPrefix, groupSuffix, new HashSet<>(permissions)));
                            permissions.clear();
                        } else {
                            firstRun = false;
                        }

                        // load new data
                        groupName = newName;
                        groupWeight = set.getInt("group_weight");
                        groupPrefix = set.getString("group_prefix");
                        groupSuffix = set.getString("group_suffix");
                    }

                    // load information about group permissions
                    String permissionString = set.getString("permission");
                    if (permissionString == null) continue;
                    boolean permissionMode = set.getBoolean("mode");
                    permissions.add(new Permission(permissionString, permissionMode));
                }

                // adding last collected group data
                list.add(getGroup(groupName, groupWeight, groupPrefix, groupSuffix, new HashSet<>(permissions)));
            }
            return list;
        }
    }

    private Group getGroup(String groupName, int groupWeight, String groupPrefix, String groupSuffix, Set<Permission> permissions) {
        Group group = new Group(groupName, groupWeight, groupPrefix, groupSuffix, permissions);
        group.subscribe(new GroupPermissionChangeListener(plugin), Group.Operation.WEIGHT_CHANGE, Group.Operation.PERMISSION_CHANGE, Group.Operation.DELETE);
        return group;
    }

    public List<String> selectAllGroupNames() throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_GROUP_NAMES)) {

            List<String> list = new ArrayList<>();
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    list.add(set.getString("name"));
                }
            }

            return list;
        }
    }

    public void updateGroup(@NotNull Group group) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_GROUP)) {

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

    public void updatePermissionInGroup(@NotNull Group group, Permission permission) throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(ADD_PERM_TO_GROUP)) {

            statement.setString(1, group.getName());
            statement.setString(2, permission.getNode());
            statement.setBoolean(3, permission.getMode());
            statement.setBoolean(4, permission.getMode());

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
