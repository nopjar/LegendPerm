package net.playlegend.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class Config {

    private static final String PATH_MYSQL_HOSTNAME = "mysql.hostname";
    private static final String PATH_MYSQL_PORT = "mysql.port";
    private static final String PATH_MYSQL_USERNAME = "mysql.username";
    private static final String PATH_MYSQL_PASSWORD = "mysql.password";
    private static final String PATH_MYSQL_DATABASE = "mysql.database";
    private static final String PATH_MYSQL_MAX_POOL_SIZE = "mysql.max_pool_size";
    private static final String PATH_MYSQL_MIN_IDLE_CONNECTIONS = "mysql.min_idle_connections";
    private static final String PATH_MYSQL_MAX_LIFETIME = "mysql.max_lifetime";

    public final String mysqlHostname;
    public final int mysqlPort;
    public final String mysqlUsername;
    public final String mysqlPassword;
    public final String mysqlDatabase;
    public final int mysqlMaxPoolSize;
    public final int mysqlMinIdleConnections;
    public final int mysqlMaxLifetime;

    public Config(@NotNull YamlConfiguration yamlConfiguration) {
        this.mysqlHostname = yamlConfiguration.getString(PATH_MYSQL_HOSTNAME, "localhost");
        this.mysqlPort = yamlConfiguration.getInt(PATH_MYSQL_PORT, 3306);
        this.mysqlUsername = yamlConfiguration.getString(PATH_MYSQL_USERNAME, "root");
        this.mysqlPassword = yamlConfiguration.getString(PATH_MYSQL_PASSWORD, "password");
        this.mysqlDatabase = yamlConfiguration.getString(PATH_MYSQL_DATABASE, "legendperm");
        this.mysqlMaxPoolSize = yamlConfiguration.getInt(PATH_MYSQL_MAX_POOL_SIZE, 10);
        this.mysqlMinIdleConnections = yamlConfiguration.getInt(PATH_MYSQL_MIN_IDLE_CONNECTIONS, 0);
        this.mysqlMaxLifetime = yamlConfiguration.getInt(PATH_MYSQL_MAX_LIFETIME, 30);
    }

}
