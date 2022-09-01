package net.playlegend.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

final class DataSource {

    private final HikariDataSource dataSource;

    public DataSource() {
        // TODO: 01/09/2022 move config creation to somewhere else
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://5.189.160.8:3306/legendperm?autoReconnect=true");
        config.setUsername("root");
        config.setPassword("supersecret");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (!dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
