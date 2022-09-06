package net.playlegend.sign;

import java.sql.SQLException;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.playlegend.LegendPerm;
import net.playlegend.domain.Group;
import net.playlegend.domain.Sign;
import net.playlegend.domain.User;
import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.exception.ServiceShutdownException;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.SignRepository;
import net.playlegend.service.Service;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public class SignService extends Service {

    public SignService(LegendPerm plugin) {
        super(plugin);
    }

    @Override
    public void initialize() throws ServiceInitializeException {

    }

    @Override
    public void shutdown() throws ServiceShutdownException {

    }

    public void updateAllSignsForGroup(Group group) throws SQLException {
        SignRepository signRepository = plugin.getServiceRegistry().get(RepositoryService.class)
                .get(SignRepository.class);
        List<Sign> signs = signRepository.selectSignsByGroup(group.getName());

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Sign sign : signs) {
                Location location = sign.getAsBukkitLocation();
                // world does not exist (anymore), doing nothing as the world may be added to the server
                //  later again
                if (location == null)
                    continue;
                Block block = location.getBlock();
                if (!(block.getState() instanceof org.bukkit.block.Sign blockSign)) {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            signRepository.deleteSign(sign);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    continue;
                }

                blockSign.line(1, Component.text(group.getPrefix()));
                blockSign.update();
            }
        });
    }

    public void updateAllSignsForUser(@NotNull User user) throws SQLException {
        SignRepository signRepository = plugin.getServiceRegistry().get(RepositoryService.class)
                .get(SignRepository.class);
        List<Sign> signs = signRepository.selectSignsByUser(user.getUuid());

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Sign sign : signs) {
                Location location = sign.getAsBukkitLocation();
                // world does not exist (anymore), doing nothing as the world may be added to the server
                //  later again
                if (location == null)
                    continue;
                Block block = location.getBlock();
                if (!(block.getState() instanceof org.bukkit.block.Sign blockSign)) {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            signRepository.deleteSign(sign);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    continue;
                }

                blockSign.line(1, Component.text(user.getMainGroup().getPrefix()));
                blockSign.line(2, Component.text(user.getName()));
                blockSign.update();
            }
        });
    }

}
