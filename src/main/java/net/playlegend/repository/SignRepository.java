package net.playlegend.repository;

import com.zaxxer.hikari.HikariConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.playlegend.LegendPerm;
import net.playlegend.domain.Sign;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

public class SignRepository extends Repository {

    @Language("MariaDB")
    private static final String SELECT_SIGNS_BY_USER_UUID = """
            SELECT id, user_id, world, x, y, z
            FROM sign
            WHERE user_id = ?;
            """;

    @Language("MariaDB")
    private static final String SELECT_SIGNS_BY_WORLD = """
            SELECT id, user_id, world, x, y, z
            FROM sign
            WHERE world = ?;
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
                int x;
                int y;
                int z;
                do {
                    id = set.getInt("id");
                    worldName = set.getString("world");
                    x = set.getInt("x");
                    y = set.getInt("y");
                    z = set.getInt("z");

                    result.add(new Sign(id, uuid, worldName, x, y, z));
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

                int id;
                String uuidAsString;
                int x;
                int y;
                int z;
                do {
                    id = set.getInt("id");
                    uuidAsString = set.getString("user_id");
                    x = set.getInt("x");
                    y = set.getInt("y");
                    z = set.getInt("z");

                    result.add(new Sign(id, (uuidAsString == null ? null : UUID.fromString(uuidAsString)), worldName, x, y, z));
                } while (set.next());

                return result;
            }
        }
    }

}
