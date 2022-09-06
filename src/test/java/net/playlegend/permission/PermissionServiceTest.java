package net.playlegend.permission;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;
import net.playlegend.cache.CacheService;
import net.playlegend.cache.PermissionCache;
import net.playlegend.cache.UserCache;
import net.playlegend.domain.Group;
import net.playlegend.domain.Permission;
import net.playlegend.domain.User;
import net.playlegend.repository.RepositoryService;
import net.playlegend.repository.UserRepository;
import net.playlegend.service.ServiceRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class PermissionServiceTest {

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
    void processPlayerJoin() throws ExecutionException {
        LegendPerm plugin = mock(LegendPerm.class);
        Player player = mock(Player.class);
        Map<Group, Long> groups = getGroupMap();
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        RepositoryService repositoryService = mock(RepositoryService.class);
        CacheService cacheService = mock(CacheService.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserCache userCache = mock(UserCache.class);
        PermissionCache permissionCache = mock(PermissionCache.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        UUID uuid = UUID.randomUUID();
        PermissionAttachment permissionAttachment = mock(PermissionAttachment.class);
        BukkitTask task = mock(BukkitTask.class);

        when(player.getUniqueId())
                .thenReturn(uuid);
        when(serviceRegistry.get(RepositoryService.class))
                .thenReturn(repositoryService);
        when(serviceRegistry.get(CacheService.class))
                .thenReturn(cacheService);
        when(cacheService.get(UserCache.class))
                .thenReturn(userCache);
        when(cacheService.get(PermissionCache.class))
                .thenReturn(permissionCache);
        when(repositoryService.get(UserRepository.class))
                .thenReturn(userRepository);
        when(permissionCache.get(uuid))
                .thenReturn(permissionAttachment);
        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(scheduler.runTaskTimerAsynchronously(any(), any(PermissionCheckingThread.class), eq(20L), eq(20L)))
                .thenReturn(task);
        when(permissionAttachment.getPermissions())
                .thenReturn(new HashMap<>());

        bukkitMock.when(Bukkit::getScheduler)
                .thenReturn(scheduler);

        new PermissionService(plugin).processPlayerJoin(player, groups);

        verify(scheduler).runTaskTimerAsynchronously(any(), any(PermissionCheckingThread.class), eq(20L), eq(20L));
    }

    @Test
    void processPlayerLeave() throws ExecutionException {
        LegendPerm plugin = mock(LegendPerm.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CacheService cacheService = mock(CacheService.class);
        PermissionCache permissionCache = mock(PermissionCache.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        UUID uuid = UUID.randomUUID();
        PermissionAttachment permissionAttachment = mock(PermissionAttachment.class);

        when(serviceRegistry.get(CacheService.class))
                .thenReturn(cacheService);
        when(cacheService.get(PermissionCache.class))
                .thenReturn(permissionCache);
        when(permissionCache.get(uuid))
                .thenReturn(permissionAttachment);
        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);

        bukkitMock.when(Bukkit::getScheduler)
                .thenReturn(scheduler);

        new PermissionService(plugin).processPlayerLeave(uuid);

        verify(permissionAttachment).remove();
        verify(permissionCache).release(uuid);
    }

    @Test
    void processTemporaryGroupChange() throws ExecutionException {
        LegendPerm plugin = mock(LegendPerm.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        RepositoryService repositoryService = mock(RepositoryService.class);
        CacheService cacheService = mock(CacheService.class);
        PermissionCache permissionCache = mock(PermissionCache.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        UUID uuid = UUID.randomUUID();
        User user = mock(User.class);
        PermissionAttachment permissionAttachment = mock(PermissionAttachment.class);
        BukkitTask task = mock(BukkitTask.class);

        when(serviceRegistry.get(RepositoryService.class))
                .thenReturn(repositoryService);
        when(serviceRegistry.get(CacheService.class))
                .thenReturn(cacheService);
        when(cacheService.get(PermissionCache.class))
                .thenReturn(permissionCache);
        when(permissionCache.get(uuid))
                .thenReturn(permissionAttachment);
        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(user.getGroups())
                .thenReturn(ImmutableMap.copyOf(getGroupMap()));
        when(user.getUuid())
                .thenReturn(uuid);
        when(scheduler.runTaskTimerAsynchronously(any(), any(PermissionCheckingThread.class), eq(20L), eq(20L)))
                .thenReturn(task);

        bukkitMock.when(Bukkit::getScheduler)
                .thenReturn(scheduler);

        new PermissionService(plugin).processTemporaryGroupChange(user);

        verify(scheduler).runTaskTimerAsynchronously(any(), any(PermissionCheckingThread.class), eq(20L), eq(20L));
    }

    @Test
    void removeGroupFromAllOnlineUsers() throws ExecutionException {
        LegendPerm plugin = mock(LegendPerm.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CacheService cacheService = mock(CacheService.class);
        UserCache userCache = mock(UserCache.class);
        UUID uuid = UUID.randomUUID();
        User user = mock(User.class);
        Collection<Player> players = new ArrayList<>();
        Player player = mock(Player.class);
        players.add(player);
        Group group = mock(Group.class);

        when(serviceRegistry.get(CacheService.class))
                .thenReturn(cacheService);
        when(cacheService.get(UserCache.class))
                .thenReturn(userCache);
        when(userCache.get(uuid))
                .thenReturn(Optional.of(user));
        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(player.getUniqueId())
                .thenReturn(uuid);

        bukkitMock.when(Bukkit::getOnlinePlayers)
                .thenReturn(players);

        new PermissionService(plugin).removeGroupFromAllOnlineUsers(group);

        verify(user).removeGroup(group);
    }

    @Test
    void updateUser() throws ExecutionException {
        LegendPerm plugin = mock(LegendPerm.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CacheService cacheService = mock(CacheService.class);
        PermissionCache permissionCache = mock(PermissionCache.class);
        UUID uuid = UUID.randomUUID();
        User user = mock(User.class);
        Collection<Player> players = new ArrayList<>();
        Player player = mock(Player.class);
        players.add(player);
        Group group = mock(Group.class);
        PermissionAttachment attachment = mock(PermissionAttachment.class);

        when(serviceRegistry.get(CacheService.class))
                .thenReturn(cacheService);
        when(cacheService.get(PermissionCache.class))
                .thenReturn(permissionCache);
        when(permissionCache.get(uuid))
                .thenReturn(attachment);
        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(player.getUniqueId())
                .thenReturn(uuid);
        when(user.getUuid())
                .thenReturn(uuid);
        when(user.getGroups())
                .thenReturn(ImmutableMap.copyOf(getGroupMap()));
        when(attachment.getPermissions())
                .thenReturn(mutableMapOf("a", true));

        bukkitMock.when(Bukkit::getOnlinePlayers)
                .thenReturn(players);

        new PermissionService(plugin).updateUser(user);

        verify(attachment, atLeastOnce()).setPermission(anyString(), anyBoolean());
        verify(attachment).unsetPermission(anyString());
    }

    private <K, V> Map<K, V> mutableMapOf(K k, V v) {
        Map<K, V> map = new HashMap<>();
        map.put(k, v);
        return map;
    }

    @Test
    void updateGroup() throws ExecutionException {
        LegendPerm plugin = mock(LegendPerm.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CacheService cacheService = mock(CacheService.class);
        UserCache userCache = mock(UserCache.class);
        UUID uuid = UUID.randomUUID();
        User user = mock(User.class);
        Collection<Player> players = new ArrayList<>();
        Player player = mock(Player.class);
        players.add(player);
        Group group = mock(Group.class);

        when(serviceRegistry.get(CacheService.class))
                .thenReturn(cacheService);
        when(cacheService.get(UserCache.class))
                .thenReturn(userCache);
        when(userCache.get(uuid))
                .thenReturn(Optional.of(user));
        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(player.getUniqueId())
                .thenReturn(uuid);
        when(user.hasGroup(any(Group.class)))
                .thenReturn(true);

        bukkitMock.when(Bukkit::getOnlinePlayers)
                .thenReturn(players);

        try {
            new PermissionService(plugin).updateGroup(group);
            throw new IllegalStateException("Should not reach this!");
        } catch (NullPointerException e) {
            if (!e.getMessage().equals("Cannot invoke \"net.playlegend.cache.PermissionCache.get(java.util.UUID)\" because the return value of \"net.playlegend.cache.CacheService.get(java.lang.Class)\" is null")) {
                throw new IllegalStateException("Should match!");
            }
        }

        verify(user).hasGroup(group);
        verify(user).getUuid(); // is only called in #updateUser
    }

    private Map<Group, Long> getGroupMap() {
        Map<Group, Long> map = new HashMap<>();
        map.put(new Group("islands", 856, "roland", "continuing", Set.of(new Permission("hey", true))), 0L);
        map.put(new Group("burden", 296, "spokesman", "", new HashSet<>()), 3L);
        return map;
    }

    @AfterAll
    static void afterAll() {
        bukkitMock.close();
        usedStaticMocks.forEach(MockedStatic::close);
    }

}