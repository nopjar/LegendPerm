package net.playlegend;

import java.io.IOException;
import net.playlegend.configuration.Config;
import net.playlegend.configuration.ConfigurationOperation;
import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.service.ServiceRegistry;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class LegendPerm extends JavaPlugin {

    private static LegendPerm instance;
    private ServiceRegistry serviceRegistry;
    private Config customConfig;
    private boolean enabled;

    public LegendPerm() {
        instance = this;
        this.enabled = true;
    }

    @Override
    public void onLoad() {
        // load configuration
        try {
            ConfigurationOperation co = new ConfigurationOperation(getDataFolder().getAbsolutePath(), "config.yml");
            co.copyIfNotExist();
            YamlConfiguration yamlConfiguration = co.loadAsYaml();
            this.customConfig = new Config(yamlConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
            disable("Failed to load configuration!");
            return;
        }

        this.serviceRegistry = new ServiceRegistry(customConfig);
        getLogger().info("LegendPerm successfully loaded.");
    }

    @Override
    public void onEnable() {
        if (!enabled) return;

        try {
            serviceRegistry.start();
        } catch (ServiceInitializeException e) {
            disable("Failed to start services!");
            e.printStackTrace();
            return;
        }

        getLogger().info("LegendPerm successfully enabled and ready to use!");
    }

    @Override
    public void onDisable() {
        if (this.serviceRegistry != null)
            this.serviceRegistry.shutdown();

        getLogger().info("LegendPerm successfully shutdown.");
    }

    private void disable(String message) {
        if (!enabled) return; // no need to disable twice

        this.enabled = false;
        getLogger().severe(message);
        getServer().getPluginManager().disablePlugin(this);
    }

    public static LegendPerm getInstance() {
        return instance;
    }

    public Config getCustomConfig() {
        return customConfig;
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

}