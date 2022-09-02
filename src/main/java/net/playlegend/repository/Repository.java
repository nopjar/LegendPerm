package net.playlegend.repository;

import com.zaxxer.hikari.HikariConfig;

abstract class Repository {

    private final DataSource dataSource;

    public Repository(HikariConfig config) {
        this.dataSource = new DataSource(config);
    }

    DataSource getDataSource() {
        return this.dataSource;
    }

}
