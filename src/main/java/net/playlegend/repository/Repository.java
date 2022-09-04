package net.playlegend.repository;

import net.playlegend.LegendPerm;

abstract class Repository {

    protected final LegendPerm plugin;
    protected final DataSource dataSource;

    public Repository(LegendPerm plugin, DataSource dataSource) {
        this.plugin = plugin;
        this.dataSource = dataSource;
    }

}
