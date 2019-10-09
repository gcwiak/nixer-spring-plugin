package eu.xword.nixer.nixerplugin.registry;

import java.time.Duration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import eu.xword.nixer.nixerplugin.events.LockUserEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class BlockedUserRegistry implements ApplicationListener<LockUserEvent> {

    private Duration expirationTime = Duration.ofMinutes(5);

    private final Cache<String, String> blockedUsers = CacheBuilder.newBuilder()
            .expireAfterWrite(expirationTime)
            .build();


    public boolean contains(final String username) {
        return blockedUsers.getIfPresent(username) != null;
    }

    @Override
    public void onApplicationEvent(final LockUserEvent event) {
        // TODO block for time
        blockedUsers.put(event.getUsername(), event.getUsername());
    }

    public void setExpirationTime(final Duration expirationTime) {
        this.expirationTime = expirationTime;
    }
}
