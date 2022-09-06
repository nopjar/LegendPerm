package net.playlegend.sign;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.domain.Group;
import net.playlegend.domain.Sign;
import net.playlegend.domain.User;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.SignRepository;
import net.playlegend.service.ServiceRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class SignServiceTest {

    static MockedStatic<Bukkit> bukkitMock;
    static List<MockedStatic<?>> usedStaticMocks;

    @BeforeAll
    static void beforeAll() {
        bukkitMock = mockStatic(Bukkit.class);
    }

    @BeforeEach
    void setUp() {
        bukkitMock.reset();
        usedStaticMocks = new ArrayList<>();
    }

    @Test
    void updateAllSignsForGroup() throws ExecutionException, SQLException {
        LegendPerm plugin = mock(LegendPerm.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        RepositoryService repositoryService = mock(RepositoryService.class);
        SignRepository signRepository = mock(SignRepository.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        Group group = mock(Group.class);
        Sign sign = mock(Sign.class);
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        org.bukkit.block.Sign bukkitSign = mock(org.bukkit.block.Sign.class);

        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(serviceRegistry.get(RepositoryService.class))
                .thenReturn(repositoryService);
        when(repositoryService.get(SignRepository.class))
                .thenReturn(signRepository);
        when(group.getPrefix())
                .thenReturn("");
        when(signRepository.selectSignsByGroup(any()))
                .thenReturn(List.of(sign));
        when(sign.getAsBukkitLocation())
                .thenReturn(location);
        when(location.getBlock())
                .thenReturn(block);
        when(block.getState())
                .thenReturn(bukkitSign);

        bukkitMock.when(Bukkit::getScheduler)
                .thenReturn(scheduler);

        new SignService(plugin).updateAllSignsForGroup(group);

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTask(any(Plugin.class), captor.capture());
        captor.getValue().run();
        verify(bukkitSign).update();
    }

    @Test
    void failUpdateAllSignsForGroup() throws ExecutionException, SQLException {
        LegendPerm plugin = mock(LegendPerm.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        RepositoryService repositoryService = mock(RepositoryService.class);
        SignRepository signRepository = mock(SignRepository.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        Group group = mock(Group.class);
        Sign sign = mock(Sign.class);
        Location location = mock(Location.class);
        Block block = mock(Block.class);

        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(serviceRegistry.get(RepositoryService.class))
                .thenReturn(repositoryService);
        when(repositoryService.get(SignRepository.class))
                .thenReturn(signRepository);
        when(group.getPrefix())
                .thenReturn("");
        when(signRepository.selectSignsByGroup(any()))
                .thenReturn(List.of(sign));
        when(sign.getAsBukkitLocation())
                .thenReturn(location);
        when(location.getBlock())
                .thenReturn(block);
        when(block.getState())
                .thenReturn(null);

        bukkitMock.when(Bukkit::getScheduler)
                .thenReturn(scheduler);

        new SignService(plugin).updateAllSignsForGroup(group);

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTask(any(Plugin.class), captor.capture());
        captor.getValue().run();
        verify(scheduler).runTaskAsynchronously(any(Plugin.class), captor.capture());
        captor.getValue().run();
        verify(signRepository).deleteSign(sign);
    }

    @Test
    void updateAllSignsForUser() throws ExecutionException, SQLException {
        LegendPerm plugin = mock(LegendPerm.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        RepositoryService repositoryService = mock(RepositoryService.class);
        SignRepository signRepository = mock(SignRepository.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        User user = mock(User.class);
        Group group = mock(Group.class);
        Sign sign = mock(Sign.class);
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        org.bukkit.block.Sign bukkitSign = mock(org.bukkit.block.Sign.class);

        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(serviceRegistry.get(RepositoryService.class))
                .thenReturn(repositoryService);
        when(repositoryService.get(SignRepository.class))
                .thenReturn(signRepository);
        when(user.getName())
                .thenReturn("");
        when(user.getMainGroup())
                .thenReturn(group);
        when(group.getPrefix())
                .thenReturn("");
        when(signRepository.selectSignsByUser(any()))
                .thenReturn(List.of(sign));
        when(sign.getAsBukkitLocation())
                .thenReturn(location);
        when(location.getBlock())
                .thenReturn(block);
        when(block.getState())
                .thenReturn(bukkitSign);

        bukkitMock.when(Bukkit::getScheduler)
                .thenReturn(scheduler);

        new SignService(plugin).updateAllSignsForUser(user);

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTask(any(Plugin.class), captor.capture());
        captor.getValue().run();
        verify(bukkitSign).update();
    }

    @Test
    void failUpdateAllSignsForUser() throws ExecutionException, SQLException {
        LegendPerm plugin = mock(LegendPerm.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        RepositoryService repositoryService = mock(RepositoryService.class);
        SignRepository signRepository = mock(SignRepository.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        User user = mock(User.class);
        Group group = mock(Group.class);
        Sign sign = mock(Sign.class);
        Location location = mock(Location.class);
        Block block = mock(Block.class);

        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(serviceRegistry.get(RepositoryService.class))
                .thenReturn(repositoryService);
        when(repositoryService.get(SignRepository.class))
                .thenReturn(signRepository);
        when(group.getPrefix())
                .thenReturn("");
        when(signRepository.selectSignsByUser(any()))
                .thenReturn(List.of(sign));
        when(sign.getAsBukkitLocation())
                .thenReturn(location);
        when(location.getBlock())
                .thenReturn(block);
        when(block.getState())
                .thenReturn(null);

        bukkitMock.when(Bukkit::getScheduler)
                .thenReturn(scheduler);

        new SignService(plugin).updateAllSignsForUser(user);

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTask(any(Plugin.class), captor.capture());
        captor.getValue().run();
        verify(scheduler).runTaskAsynchronously(any(Plugin.class), captor.capture());
        captor.getValue().run();
        verify(signRepository).deleteSign(sign);
    }

    @AfterAll
    static void afterAll() {
        bukkitMock.close();
        usedStaticMocks.forEach(MockedStatic::close);
    }

}