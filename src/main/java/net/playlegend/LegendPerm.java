package net.playlegend;

import org.bukkit.plugin.java.JavaPlugin;

public class LegendPerm extends JavaPlugin {

    private static LegendPerm instance;

    public LegendPerm() {
        instance = this;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    private void disable(String message) {
        getLogger().severe(message);
        getServer().getPluginManager().disablePlugin(this);
    }

    public static LegendPerm getInstance() {
        return instance;
    }

}