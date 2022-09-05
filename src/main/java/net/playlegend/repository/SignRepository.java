package net.playlegend.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.playlegend.LegendPerm;
import net.playlegend.domain.Sign;
import net.playlegend.domain.User;
import org.bukkit.Location;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

public class SignRepository extends Repository {

    @Language("MariaDB")
    private static final String SELECT_SIGNS_BY = """
            SELECT id, user_id, `group`, world, x, y, z
            FROM sign
            """;

    @Language("MariaDB")
    private static final String SELECT_SIGNS_BY_USER_UUID = SELECT_SIGNS_BY + """
            WHERE user_id = ?;
            """;

    @Language("MariaDB")
    private static final String SELECT_SIGNS_BY_WORLD = SELECT_SIGNS_BY + """
            WHERE world = ?;
            """;

    @Language("MariaDB")
    private static final String SELECT_SIGNS_BY_GROUP = SELECT_SIGNS_BY + """
            WHERE `group` = ?;
            """;

    @Language("MariaDB")
    private static final String DELETE_SIGN = """
            DELETE FROM sign
            WHERE id = ?;
            """;

    @Language("MariaDB")
    private static final String CREATE_SIGN = """
            INSERT INTO sign
            (user_id, `group`, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?);
            """;

    public SignRepository(LegendPerm plugin, DataSource dataSource) {
        super(plugin, dataSource);
    }

    public List<Sign> selectSignsByUser(@NotNull UUID uuid) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_SIGNS_BY_USER_UUID)) {

            statement.setString(1, uuid.toString());

            List<Sign> result = new ArrayList<>();
            try (ResultSet set = statement.executeQuery()) {
                if (!set.next())
                    return result;

                int id;
                String worldName;
                String group;
                int x;
                int y;
                int z;
                do {
                    id = set.getInt("id");
                    worldName = set.getString("world");
                    group = set.getString("group");
                    x = set.getInt("x");
                    y = set.getInt("y");
                    z = set.getInt("z");

                    result.add(new Sign(id, uuid, worldName, group, x, y, z));
                } while (set.next());

                return result;
            }
        }
    }

    public List<Sign> selectSignsByWorld(@NotNull String worldName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_SIGNS_BY_WORLD)) {

            statement.setString(1, worldName);

            List<Sign> result = new ArrayList<>();
            try (ResultSet set = statement.executeQuery()) {
                if (!set.next())
                    return result;

                // TODO: 04/09/2022 select world one time to ensure proper case
                int id;
                String uuidAsString;
                String group;
                int x;
                int y;
                int z;
                do {
                    id = set.getInt("id");
                    uuidAsString = set.getString("user_id");
                    group = set.getString("group");
                    x = set.getInt("x");
                    y = set.getInt("y");
                    z = set.getInt("z");

                    result.add(new Sign(id, (uuidAsString == null ? null : UUID.fromString(uuidAsString)), group, worldName, x, y, z));
                } while (set.next());

                return result;
            }
        }
    }

    public List<Sign> selectSignsByGroup(@NotNull String groupName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_SIGNS_BY_GROUP)) {

            statement.setString(1, groupName);

            List<Sign> result = new ArrayList<>();
            try (ResultSet set = statement.executeQuery()) {
                if (!set.next())
                    return result;
                // TODO: 04/09/2022 select group one time to ensure proper case

                int id;
                String uuidAsString;
                String worldName;
                int x;
                int y;
                int z;
                do {
                    id = set.getInt("id");
                    uuidAsString = set.getString("user_id");
                    worldName = set.getString("world");
                    x = set.getInt("x");
                    y = set.getInt("y");
                    z = set.getInt("z");

                    result.add(new Sign(id, (uuidAsString == null ? null : UUID.fromString(uuidAsString)), groupName, worldName, x, y, z));
                } while (set.next());

                return result;
            }
        }
    }

    public void deleteSign(Sign sign) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SIGN)) {

            statement.setInt(1, sign.getId());

            statement.executeUpdate();
        }
    }

    public Sign createSign(User owner, Location location) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_SIGN, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, owner.getUuid().toString());
            statement.setString(2, owner.getMainGroup().getName());
            statement.setString(3, location.getWorld().getName());
            statement.setInt(4, location.getBlockX());
            statement.setInt(5, location.getBlockY());
            statement.setInt(6, location.getBlockZ());

            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()){
                if (!generatedKeys.next())
                    throw new SQLException("Unable to fetch new Key for sign at location " + location);
                int key = generatedKeys.getInt(1);
                if (key == 0) {
                    throw new SQLException("Invalid Key for sign at location " + location);
                }

                return new Sign(key,
                        owner.getUuid(),
                        owner.getMainGroup().getName(),
                        location.getWorld().getName(),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ());
            }
        }
    }

}
