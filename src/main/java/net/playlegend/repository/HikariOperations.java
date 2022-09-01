package net.playlegend.repository;

import com.zaxxer.hikari.HikariConfig;

public class HikariOperations {

    private static HikariConfig HIKARI_CONFIG;

    public static HikariConfig getHikariConfig() {
        return HIKARI_CONFIG;
    }

    public static HikariConfig loadHikariConfig() {
        HIKARI_CONFIG = new HikariConfig();
        HIKARI_CONFIG.setJdbcUrl("jdbc:mysql://5.189.160.8:3306/legendperm?autoReconnect=true");
        HIKARI_CONFIG.setUsername("root");
        HIKARI_CONFIG.setPassword("supersecret");
        HIKARI_CONFIG.addDataSourceProperty("cachePrepStmts", "true");
        HIKARI_CONFIG.addDataSourceProperty("prepStmtCacheSize", "250");
        HIKARI_CONFIG.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return HIKARI_CONFIG;
    }

}
