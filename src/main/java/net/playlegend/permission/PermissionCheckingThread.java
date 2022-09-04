package net.playlegend.permission;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import net.playlegend.cache.UserCache;
import net.playlegend.domain.Group;
import net.playlegend.domain.User;
import net.playlegend.repository.UserRepository;

class PermissionCheckingThread implements Runnable {

    private final UUID userId;
    private final UserCache userCache;
    private final UserRepository userRepository;

    public PermissionCheckingThread(UUID userId, UserCache userCache, UserRepository userRepository) {
        this.userId = userId;
        this.userCache = userCache;
        this.userRepository = userRepository;
    }

    @Override
    public void run() {
        try {
            User user = userCache.get(userId)
                    .orElseThrow();

            long epochSeconds = ZonedDateTime.now().toEpochSecond();
            for (Map.Entry<Group, Long> entry : user.getGroups().entrySet()) {
                if (entry.getValue() == 0 || entry.getValue() >= epochSeconds)
                    continue;

                try {
                    userRepository.removeUserFromGroup(userId, entry.getKey().getName());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                // do not include in try-block as we want to remove the group from the user
                //   nevertheless if the database-transactions went through
                user.removeGroup(entry.getKey());
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
