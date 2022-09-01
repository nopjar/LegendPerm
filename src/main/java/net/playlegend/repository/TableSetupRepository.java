package net.playlegend.repository;

import com.zaxxer.hikari.HikariConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.intellij.lang.annotations.Language;

class TableSetupRepository extends Repository {

    @Language("MariaDB")
    private static final String CREATE_GROUP_TABLE = """
            CREATE TABLE IF NOT EXISTS `group` (
            	`name` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_general_ci',
            	`weight` INT(11) NOT NULL DEFAULT '0',
            	`prefix` VARCHAR(50) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
            	`suffix` VARCHAR(50) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
            	PRIMARY KEY (`name`) USING BTREE
            )
            """;

    @Language("MariaDB")
    private static final String CREATE_DEFAULT_GROUP = """
            INSERT INTO `group`
            (name, weight) VALUES ('default', 80)
            ON DUPLICATE KEY UPDATE weight = weight;
            """;

    @Language("MariaDB")
    private static final String CREATE_GROUP_PERMISSIONS_TABLE = """
            CREATE TABLE IF NOT EXISTS `group_permissions` (
            	`group_id` VARCHAR(50) NOT NULL,
            	`permission` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_general_ci',
            	`type` TINYINT(4) NOT NULL DEFAULT '0',
            	UNIQUE INDEX `group_id` (`group_id`, `permission`) USING BTREE,
            	INDEX `FK__group_2` (`group_id`) USING BTREE,
            	CONSTRAINT `FK__group_2` FOREIGN KEY (`group_id`) REFERENCES `group` (`name`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """;

    @Language("MariaDB")
    private static final String CREATE_USER_TABLE = """
            CREATE TABLE IF NOT EXISTS `user` (
            	`uuid` VARCHAR(36) NOT NULL COLLATE 'utf8mb4_general_ci',
            	`name` VARCHAR(16) NOT NULL COLLATE 'utf8mb4_general_ci',
            	PRIMARY KEY (`uuid`) USING BTREE
            );
            """;

    @Language("MariaDB")
    private static final String CREATE_USERS_GROUPS_TABLE = """
            CREATE TABLE IF NOT EXISTS `users_groups` (
            	`user_id` VARCHAR(36) NOT NULL COLLATE 'utf8mb4_general_ci',
            	`group_id` VARCHAR(50) NOT NULL,
            	`valid_until` BIGINT(20) NOT NULL DEFAULT '0',
            	UNIQUE INDEX `user_id` (`user_id`, `group_id`) USING BTREE,
            	INDEX `FK__group` (`group_id`) USING BTREE,
            	CONSTRAINT `FK__group` FOREIGN KEY (`group_id`) REFERENCES `group` (`name`) ON UPDATE NO ACTION ON DELETE CASCADE,
            	CONSTRAINT `FK__user` FOREIGN KEY (`user_id`) REFERENCES `user` (`uuid`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """;

    private final List<String> queries;

    public TableSetupRepository(HikariConfig config) {
        super(config);

        this.queries = new ArrayList<>();
        this.queries.add(CREATE_GROUP_TABLE);
        this.queries.add(CREATE_DEFAULT_GROUP);
        this.queries.add(CREATE_GROUP_PERMISSIONS_TABLE);
        this.queries.add(CREATE_USER_TABLE);
        this.queries.add(CREATE_USERS_GROUPS_TABLE);
    }

    public void setupTables() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {

            for (String query : this.queries)
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.executeUpdate();
                }
        }
    }

}
