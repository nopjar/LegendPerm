package net.playlegend.repository;

import com.zaxxer.hikari.HikariConfig;
import java.util.concurrent.TimeUnit;
import net.playlegend.configuration.Config;
import org.jetbrains.annotations.NotNull;

public class HikariOperations {

    private static HikariConfig HIKARI_CONFIG;

    public static HikariConfig getHikariConfig() {
        return HIKARI_CONFIG;
    }

    public static HikariConfig loadHikariConfig(@NotNull Config config) {
        HIKARI_CONFIG = new HikariConfig();
        HIKARI_CONFIG.setJdbcUrl("jdbc:mysql://" + config.mysqlHostname + ":" + config.mysqlPort + "/" + config.mysqlDatabase + "?autoReconnect=true");
        HIKARI_CONFIG.setUsername(config.mysqlUsername);
        HIKARI_CONFIG.setPassword(config.mysqlPassword);
        HIKARI_CONFIG.setMaximumPoolSize(config.mysqlMaxPoolSize);
        HIKARI_CONFIG.setMinimumIdle(config.mysqlMinIdleConnections);
        HIKARI_CONFIG.setMaxLifetime(TimeUnit.MINUTES.toMillis(config.mysqlMaxLifetime));
        HIKARI_CONFIG.addDataSourceProperty("cachePrepStmts", "true");
        HIKARI_CONFIG.addDataSourceProperty("prepStmtCacheSize", "250");
        HIKARI_CONFIG.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return HIKARI_CONFIG;
    }

}
