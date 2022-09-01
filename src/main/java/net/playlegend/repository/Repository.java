package net.playlegend.repository;

abstract class Repository {

    private final DataSource dataSource;

    public Repository() {
        this.dataSource = new DataSource();
    }

    DataSource getDataSource() {
        return this.dataSource;
    }

}
