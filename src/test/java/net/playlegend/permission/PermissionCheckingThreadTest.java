package net.playlegend.permission;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import net.playlegend.cache.UserCache;
import net.playlegend.domain.Group;
import net.playlegend.domain.Permission;
import net.playlegend.domain.User;
import net.playlegend.repository.UserRepository;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class PermissionCheckingThreadTest {

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
    void run() throws ExecutionException {
        UUID uuid = UUID.randomUUID();
        UserCache userCache = mock(UserCache.class);
        UserRepository userRepository = mock(UserRepository.class);
        User user = mock(User.class);

        when(userCache.get(uuid))
                .thenReturn(Optional.of(user));
        when(user.getGroups())
                .thenReturn(ImmutableMap.copyOf(getGroupMap()));

        new PermissionCheckingThread(uuid, userCache, userRepository).run();

        verify(user).removeGroup(any());
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