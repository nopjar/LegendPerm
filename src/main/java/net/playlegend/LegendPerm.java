package net.playlegend;

import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.service.ServiceRegistry;
import org.bukkit.plugin.java.JavaPlugin;

public class LegendPerm extends JavaPlugin {

    private ServiceRegistry serviceRegistry;
    private boolean enabled;

    public LegendPerm() {
        this.enabled = true;
    }

    @Override
    public void onLoad() {
        this.serviceRegistry = new ServiceRegistry(this);
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

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

}