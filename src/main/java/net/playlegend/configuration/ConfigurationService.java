package net.playlegend.configuration;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.playlegend.LegendPerm;
import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.exception.ServiceShutdownException;
import net.playlegend.service.Service;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigurationService extends Service {

    private final Map<Class<? extends Configuration>, Configuration> configurations;

    public ConfigurationService(LegendPerm plugin) {
        super(plugin);
        this.configurations = new ConcurrentHashMap<>();
    }

    @Override
    public void initialize() throws ServiceInitializeException {

        try {
            Config config = new Config(loadGeneralConfig());
            this.configurations.put(Config.class, config);
            this.configurations.put(MessageConfig.class, new MessageConfig(loadMessageConfig(config.language)));
        } catch (IOException e) {
            throw new ServiceInitializeException(e);
        }
    }

    private YamlConfiguration loadGeneralConfig() throws IOException {
        ConfigurationOperation co = new ConfigurationOperation(plugin.getDataFolder().getAbsolutePath(), "config.yml");
        co.copyIfNotExist();
        return co.loadAsYaml();
    }

    private YamlConfiguration loadMessageConfig(String language) throws IOException {
        ConfigurationOperation co = new ConfigurationOperation(plugin.getDataFolder().toPath().resolve("languages/").toString(), language + ".yml");
        co.copyIfNotExist();
        return co.loadAsYaml();
    }

    @Override
    public void shutdown() throws ServiceShutdownException {

    }

    public <T extends Configuration> T get(Class<T> clazz) {
        return (T) configurations.get(clazz);
    }

}
