package net.playlegend.listener;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import net.kyori.adventure.text.Component;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.UserCache;
import net.playlegend.domain.Sign;
import net.playlegend.domain.User;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.SignRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class SignListener implements Listener {

    private final LegendPerm plugin;
    private final NamespacedKey namespacedKey;
    private final List<Location> signs;

    public SignListener(LegendPerm plugin) {
        this.plugin = plugin;
        this.namespacedKey = new NamespacedKey(plugin, "LegendPermSign");
        this.signs = new CopyOnWriteArrayList<>();
    }

    @EventHandler
    private void onSignCreate(SignChangeEvent event) {
        // check if first line is for us
        if (!Objects.equals(event.line(0), Component.text("[LegendPerm]"))) {
            System.out.println("Do not equal!");
            return;
        }
        // set loading, so if things break -> user knows he has nothing done wrong
        event.line(0, Component.text("Loading..."));
        org.bukkit.block.Sign signBlock = (org.bukkit.block.Sign) event.getBlock().getState();
        UUID uuid = event.getPlayer().getUniqueId();
        // add to signs list to prevent player from breaking the sign BEFORE sign is added to database
        this.signs.add(signBlock.getLocation());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // fetch user
                User user = plugin.getServiceRegistry().get(CacheService.class)
                        .get(UserCache.class)
                        .get(uuid)
                        .orElseThrow();

                // create sign in db
                plugin.getServiceRegistry().get(RepositoryService.class)
                        .get(SignRepository.class)
                        .createSign(user, signBlock.getLocation());

                // update lines on sign (needs to be sync)
                Bukkit.getScheduler().runTask(plugin, () -> {
                    saveData(signBlock);

                    signBlock.line(0, Component.text(""));
                    signBlock.line(1, Component.text(user.getMainGroup().getPrefix()));
                    signBlock.line(2, Component.text(user.getName()));
                    signBlock.line(3, Component.text(""));
                    signBlock.update();
                });
            } catch (ExecutionException | SQLException e) {
                e.printStackTrace();
            }
            this.signs.remove(signBlock.getLocation());
        });
    }

    private void saveData(org.bukkit.block.Sign sign) {
        // saves kind of a flag into the persistent data container of a sign
        sign.getPersistentDataContainer().set(namespacedKey, PersistentDataType.BYTE, (byte) 1);
    }

    @EventHandler
    private void onSignDestroy(@NotNull BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof final org.bukkit.block.Sign blockSign)) return;
        // check if sign is still inserting into database
        if (signs.contains(event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }

        // if sign contains flag in persistent data container, remove from db
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!isLegendPermSign(blockSign)) return;

            try {
                SignRepository signRepository = plugin.getServiceRegistry().get(RepositoryService.class)
                        .get(SignRepository.class);
                Sign sign = signRepository.selectSignByLocation(blockSign.getLocation());
                if (sign == null)
                    return;

                signRepository.deleteSign(sign);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean isLegendPermSign(org.bukkit.block.Sign sign) {
        return sign.getPersistentDataContainer().has(namespacedKey);
    }

}
