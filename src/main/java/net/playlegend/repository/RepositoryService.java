package net.playlegend.repository;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.playlegend.LegendPerm;
import net.playlegend.configuration.Config;
import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.service.Service;

public class RepositoryService extends Service {

    private final Map<Class<? extends Repository>, Repository> repositories;
    private final DataSource dataSource;

    public RepositoryService(LegendPerm plugin, Config config) {
        super(plugin);
        this.repositories = new ConcurrentHashMap<>();

        // we do not put TableSetupRepository in here on purpose as it should not be used outside
        this.dataSource = new DataSource(HikariOperations.loadHikariConfig(config));
        this.repositories.put(GroupRepository.class, new GroupRepository(plugin, dataSource));
        this.repositories.put(UserRepository.class, new UserRepository(plugin, dataSource));
        this.repositories.put(SignRepository.class, new SignRepository(plugin, dataSource));
    }

    @Override
    public void initialize() throws ServiceInitializeException {
        TableSetupRepository tableSetupRepository = new TableSetupRepository(plugin, this.dataSource);
        try {
            tableSetupRepository.setupTables();
        } catch (SQLException e) {
            throw new ServiceInitializeException(e);
        }
    }

    @Override
    public void shutdown() {
        this.dataSource.shutdown();
    }

    public <T extends Repository> T get(Class<T> clazz) {
        return (T) repositories.get(clazz);
    }

}
