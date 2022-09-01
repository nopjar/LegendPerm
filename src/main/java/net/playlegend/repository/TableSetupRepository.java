package net.playlegend.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.intellij.lang.annotations.Language;

class TableSetupRepository extends Repository {

    // TODO: 01/09/2022 Those creation queries may need a rework on their actions regarding their foreign keys

    @Language("MariaDB")
    @TableSetup
    private static final String CREATE_GROUP_TABLE = """
            CREATE TABLE IF NOT EXISTS `group` (
                `id` INT(11) NOT NULL AUTO_INCREMENT,
                `name` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_general_ci',
                `weight` INT(11) NOT NULL DEFAULT '0',
                `prefix` VARCHAR(50) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
                `suffix` VARCHAR(50) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `name` (`name`) USING BTREE
            );
            """;

    @Language("MariaDB")
    @TableSetup
    private static final String CREATE_GROUP_PERMISSIONS_TABLE = """
            CREATE TABLE IF NOT EXISTS `group_permissions` (
            	`group_id` INT(11) NOT NULL,
            	`permission` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_general_ci',
            	`type` TINYINT(4) NOT NULL DEFAULT '0',
            	INDEX `FK__group_2` (`group_id`) USING BTREE,
            	CONSTRAINT `FK__group_2` FOREIGN KEY (`group_id`) REFERENCES `group` (`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
            );
            """;

    @Language("MariaDB")
    @TableSetup
    private static final String CREATE_USER_TABLE = """
            CREATE TABLE IF NOT EXISTS `user` (
            	`uuid` VARCHAR(36) NOT NULL COLLATE 'utf8mb4_general_ci',
            	`name` VARCHAR(16) NOT NULL COLLATE 'utf8mb4_general_ci',
            	PRIMARY KEY (`uuid`) USING BTREE
            );
            """;

    @Language("MariaDB")
    @TableSetup
    private static final String CREATE_USERS_GROUPS_TABLE = """
            CREATE TABLE IF NOT EXISTS `users_groups` (
            	`user_id` VARCHAR(36) NOT NULL COLLATE 'utf8mb4_general_ci',
            	`group_id` INT(11) NOT NULL,
            	`valid_until` BIGINT(20) NOT NULL DEFAULT '0',
            	UNIQUE INDEX `user_id` (`user_id`, `group_id`) USING BTREE,
            	INDEX `FK__group` (`group_id`) USING BTREE,
            	CONSTRAINT `FK__group` FOREIGN KEY (`group_id`) REFERENCES `group` (`id`) ON UPDATE NO ACTION ON DELETE NO ACTION,
            	CONSTRAINT `FK__user` FOREIGN KEY (`user_id`) REFERENCES `user` (`uuid`) ON UPDATE NO ACTION ON DELETE NO ACTION
            );
            """;

    public TableSetupRepository() {

    }

    public void setupTables() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {

            for (Field field : getClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(TableSetup.class) || !String.class.isAssignableFrom(field.getType()))
                    continue;

                try {
                    String query = (String) field.get(this);
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.executeUpdate();
                    }
                } catch (IllegalAccessException e) {
                    // TODO: 01/09/2022 really?
                    throw new RuntimeException(e);
                }
            }

        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    private @interface TableSetup {

    }

}
