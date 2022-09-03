package net.playlegend.repository;

import com.zaxxer.hikari.HikariConfig;
import net.playlegend.LegendPerm;

abstract class Repository {

    protected final LegendPerm plugin;
    private final DataSource dataSource;

    public Repository(LegendPerm plugin, HikariConfig config) {
        this.plugin = plugin;
        this.dataSource = new DataSource(config);
    }

    DataSource getDataSource() {
        return this.dataSource;
    }

}
