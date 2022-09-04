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

        for (Sign sign : signs) {
            Location location = sign.getAsBukkitLocation();
            Block block = location.getBlock();
            if (!(block instanceof org.bukkit.block.Sign blockSign)) {
                signRepository.deleteSign(sign);
                continue;
            }

            blockSign.line(1, Component.text(group.getPrefix()));
            // TODO: 04/09/2022 maybe add blockSign.update()
        }
    }

    public void updateAllSignsForUser(@NotNull User user) throws SQLException {
        SignRepository signRepository = plugin.getServiceRegistry().get(RepositoryService.class)
                .get(SignRepository.class);
        List<Sign> signs = signRepository.selectSignsByUser(user.getUuid());

        for (Sign sign : signs) {
            Location location = sign.getAsBukkitLocation();
            if (location == null) {
                // TODO: 04/09/2022 do something
                continue;
            }
            Block block = location.getBlock();
            if (!(block instanceof org.bukkit.block.Sign blockSign)) {
                signRepository.deleteSign(sign);
                continue;
            }

            blockSign.line(1, Component.text(user.getMainGroup().getPrefix()));
            blockSign.line(2, Component.text(user.getName()));
            // TODO: 04/09/2022 maybe add blockSign.update()
        }
    }

}
