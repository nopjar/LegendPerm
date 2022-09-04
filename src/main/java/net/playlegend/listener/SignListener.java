package net.playlegend.listener;

import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import net.kyori.adventure.text.Component;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.UserCache;
import net.playlegend.domain.Sign;
import net.playlegend.domain.User;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.SignRepository;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener implements Listener {

    private final LegendPerm plugin;

    public SignListener(LegendPerm plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onSignCreate(SignChangeEvent event) {
        if (!Objects.equals(event.line(0), Component.text("[LegendPerm]"))) {
            return;
        }

        try {
            User user = plugin.getServiceRegistry().get(CacheService.class)
                    .get(UserCache.class)
                    .get(event.getPlayer().getUniqueId())
                    .orElseThrow();

            Sign sign = plugin.getServiceRegistry().get(RepositoryService.class)
                    .get(SignRepository.class)
                    .createSign(user, event.getBlock().getLocation());

            event.line(1, Component.text(user.getName()));
            event.line(2, Component.text(user.getMainGroup().getPrefix()));
        } catch (ExecutionException | SQLException e) {
            event.getPlayer().sendMessage("Error!");
            e.printStackTrace();
        }
    }

}
