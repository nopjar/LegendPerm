package net.playlegend.repository;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.service.Service;

public class RepositoryService implements Service {

    private final Map<Class<? extends Repository>, Repository> repositories;

    public RepositoryService() {
        this.repositories = new ConcurrentHashMap<>();

        // we do not put TableSetupRepository in here on purpose as it should not be used outside
        this.repositories.put(GroupRepository.class, new GroupRepository());
        this.repositories.put(UserRepository.class, new UserRepository());
    }

    @Override
    public void initialize() throws ServiceInitializeException {
        TableSetupRepository tableSetupRepository = new TableSetupRepository();
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
