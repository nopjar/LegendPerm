package net.playlegend.repository;

import com.zaxxer.hikari.HikariConfig;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.playlegend.configuration.Config;
import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.service.Service;

public class RepositoryService implements Service {

    private final Map<Class<? extends Repository>, Repository> repositories;

    public RepositoryService(Config config) {
        this.repositories = new ConcurrentHashMap<>();

        // we do not put TableSetupRepository in here on purpose as it should not be used outside
        HikariConfig hikariConfig = HikariOperations.loadHikariConfig(config);
        this.repositories.put(GroupRepository.class, new GroupRepository(hikariConfig));
        this.repositories.put(UserRepository.class, new UserRepository(hikariConfig));
    }

    @Override
    public void initialize() throws ServiceInitializeException {
        TableSetupRepository tableSetupRepository = new TableSetupRepository(HikariOperations.getHikariConfig());
        try {
            tableSetupRepository.setupTables();
            tableSetupRepository.getDataSource().shutdown();
        } catch (SQLException e) {
            throw new ServiceInitializeException(e);
        }
    }

    @Override
    public void shutdown() {
        for (Repository repository : this.repositories.values()) {
            repository.getDataSource().shutdown();
        }
    }

    public <T extends Repository> T get(Class<T> clazz) {
        return (T) repositories.get(clazz);
    }

}
