package net.playlegend;

import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.service.ServiceRegistry;
import org.bukkit.plugin.java.JavaPlugin;

public class LegendPerm extends JavaPlugin {

    private static LegendPerm instance;
    private ServiceRegistry serviceRegistry;

    public LegendPerm() {
        instance = this;
    }

    @Override
    public void onLoad() {
        this.serviceRegistry = new ServiceRegistry();
        getLogger().info("LegendPerm successfully loaded.");
    }

    @Override
    public void onEnable() {
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
        this.serviceRegistry.shutdown();
        getLogger().info("LegendPerm successfully shutdown.");
    }

    private void disable(String message) {
        getLogger().severe(message);
        getServer().getPluginManager().disablePlugin(this);
    }

    public static LegendPerm getInstance() {
        return instance;
    }

}