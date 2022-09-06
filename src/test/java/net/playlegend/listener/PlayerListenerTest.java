package net.playlegend.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.UserCache;
import net.playlegend.configuration.MessageConfig;
import net.playlegend.domain.Group;
import net.playlegend.domain.User;
import net.playlegend.permission.PermissionService;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.UserRepository;
import net.playlegend.service.ServiceRegistry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class PlayerListenerTest {

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
    void insertPlayerIntoTableOnFirstJoin() throws ExecutionException, SQLException {
        LegendPerm plugin = mock(LegendPerm.class);
        Player player = mock(Player.class);
        PlayerJoinEvent event = mock(PlayerJoinEvent.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        RepositoryService repositoryService = mock(RepositoryService.class);
        PermissionService permissionService = mock(PermissionService.class);
        CacheService cacheService = mock(CacheService.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserCache userCache = mock(UserCache.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        User user = mock(User.class);
        UUID uuid = UUID.randomUUID();
        Group group = mock(Group.class);
        YamlConfiguration yamlConfiguration = mock(YamlConfiguration.class);

        when(player.getUniqueId())
                .thenReturn(uuid);
        when(event.getPlayer())
                .thenReturn(player);
        when(serviceRegistry.get(RepositoryService.class))
                .thenReturn(repositoryService);
        when(serviceRegistry.get(CacheService.class))
                .thenReturn(cacheService);
        when(serviceRegistry.get(PermissionService.class))
                .thenReturn(permissionService);
        when(cacheService.get(UserCache.class))
                .thenReturn(userCache);
        when(repositoryService.get(UserRepository.class))
                .thenReturn(userRepository);
        when(userCache.get(uuid))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(user));
        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(user.getMainGroup())
                .thenReturn(group);
        when(group.getPrefix())
                .thenReturn("");
        when(yamlConfiguration.getString(any(), any()))
                .thenReturn("");

        bukkitMock.when(Bukkit::getScheduler)
                .thenReturn(scheduler);

        MessageConfig config = new MessageConfig(yamlConfiguration);

        new PlayerListener(plugin, config).onPlayerJoin(event);

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTaskAsynchronously(any(Plugin.class), captor.capture());
        captor.getValue().run();
        verify(userRepository).updateUser(any(), any());
        verify(userRepository).addUserToGroup(eq(uuid), eq("default"), eq(0L));
        verify(userCache).refresh(any());
        verify(userCache, times(2)).get(uuid);
    }

    @Test
    void updatePlayerAfterChangingName() throws ExecutionException, SQLException {
        LegendPerm plugin = mock(LegendPerm.class);
        Player player = mock(Player.class);
        PlayerJoinEvent event = mock(PlayerJoinEvent.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        RepositoryService repositoryService = mock(RepositoryService.class);
        PermissionService permissionService = mock(PermissionService.class);
        CacheService cacheService = mock(CacheService.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserCache userCache = mock(UserCache.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        User user = mock(User.class);
        UUID uuid = UUID.randomUUID();
        Group group = mock(Group.class);
        YamlConfiguration yamlConfiguration = mock(YamlConfiguration.class);

        when(player.getUniqueId())
                .thenReturn(uuid);
        when(player.getName())
                .thenReturn("different");
        when(event.getPlayer())
                .thenReturn(player);
        when(serviceRegistry.get(RepositoryService.class))
                .thenReturn(repositoryService);
        when(serviceRegistry.get(CacheService.class))
                .thenReturn(cacheService);
        when(serviceRegistry.get(PermissionService.class))
                .thenReturn(permissionService);
        when(cacheService.get(UserCache.class))
                .thenReturn(userCache);
        when(repositoryService.get(UserRepository.class))
                .thenReturn(userRepository);
        when(userCache.get(uuid))
                .thenReturn(Optional.of(user));
        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(user.getMainGroup())
                .thenReturn(group);
        when(user.getUuid())
                .thenReturn(uuid);
        when(user.getName())
                .thenReturn("test");
        when(group.getPrefix())
                .thenReturn("");
        when(yamlConfiguration.getString(any(), any()))
                .thenReturn("");

        bukkitMock.when(Bukkit::getScheduler)
                .thenReturn(scheduler);

        MessageConfig config = new MessageConfig(yamlConfiguration);

        new PlayerListener(plugin, config).onPlayerJoin(event);

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTaskAsynchronously(any(Plugin.class), captor.capture());
        captor.getValue().run();
        verify(userRepository).updateUser(any());
        verify(userCache).refresh(any());
        verify(userCache, times(2)).get(uuid);
    }

    @Test
    void releaseOnPlayerLeave() throws ExecutionException {
        LegendPerm plugin = mock(LegendPerm.class);
        Player player = mock(Player.class);
        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        PermissionService permissionService = mock(PermissionService.class);
        CacheService cacheService = mock(CacheService.class);
        UserCache userCache = mock(UserCache.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        User user = mock(User.class);
        UUID uuid = UUID.randomUUID();
        Group group = mock(Group.class);
        YamlConfiguration yamlConfiguration = mock(YamlConfiguration.class);

        when(event.getPlayer())
                .thenReturn(player);
        when(player.getUniqueId())
                .thenReturn(uuid);
        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(serviceRegistry.get(CacheService.class))
                .thenReturn(cacheService);
        when(serviceRegistry.get(PermissionService.class))
                .thenReturn(permissionService);
        when(cacheService.get(UserCache.class))
                .thenReturn(userCache);
        when(userCache.get(uuid))
                .thenReturn(Optional.of(user));
        when(user.getMainGroup())
                .thenReturn(group);
        when(group.getPrefix())
                .thenReturn("");
        when(yamlConfiguration.getString(any(), any()))
                .thenReturn("");

        bukkitMock.when(Bukkit::getScheduler)
                .thenReturn(scheduler);

        MessageConfig config = new MessageConfig(yamlConfiguration);

        new PlayerListener(plugin, config).onPlayerLeave(event);

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTaskAsynchronously(any(Plugin.class), captor.capture());
        captor.getValue().run();
        verify(userCache).release(uuid);
    }

    @Test
    void onPlayerChat() throws ExecutionException {
        LegendPerm plugin = mock(LegendPerm.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CacheService cacheService = mock(CacheService.class);
        UserCache userCache = mock(UserCache.class);
        User user = mock(User.class);
        AsyncChatEvent event = mock(AsyncChatEvent.class);
        UUID uuid = UUID.randomUUID();
        Player player = mock(Player.class);
        Group group = mock(Group.class);
        YamlConfiguration yamlConfiguration = mock(YamlConfiguration.class);

        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(serviceRegistry.get(CacheService.class))
                .thenReturn(cacheService);
        when(cacheService.get(UserCache.class))
                .thenReturn(userCache);
        when(userCache.get(uuid))
                .thenReturn(Optional.of(user));
        when(event.getPlayer())
                .thenReturn(player);
        when(player.getUniqueId())
                .thenReturn(uuid);
        when(user.getMainGroup())
                .thenReturn(group);
        when(yamlConfiguration.getString(any(), any()))
                .thenReturn("");

        MessageConfig config = new MessageConfig(yamlConfiguration);

        new PlayerListener(plugin, config).onPlayerChat(event);

        verify(event).renderer(any());
    }

    @AfterAll
    static void afterAll() {
        bukkitMock.close();
        usedStaticMocks.forEach(MockedStatic::close);
    }

}