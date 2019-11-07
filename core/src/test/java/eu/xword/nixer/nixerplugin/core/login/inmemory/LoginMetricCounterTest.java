package eu.xword.nixer.nixerplugin.core.login.inmemory;

import java.time.Duration;

import eu.xword.nixer.nixerplugin.core.login.LoginContext;
import eu.xword.nixer.nixerplugin.core.login.LoginResult;
import org.junit.jupiter.api.Test;

import static eu.xword.nixer.nixerplugin.core.login.LoginFailureType.BAD_PASSWORD;
import static eu.xword.nixer.nixerplugin.core.login.inmemory.FeatureKey.Features.IP;
import static eu.xword.nixer.nixerplugin.core.login.inmemory.LoginCounterBuilder.counter;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginMetricCounterTest {

    private static final String IP_1 = "127.0.0.1";
    private static final String IP_2 = "127.0.0.2";

    private final ClockStub clock = new ClockStub();

    private final LoginCounter counter = counter(IP)
            .window(Duration.ofMinutes(1))
            .clock(clock)
            .build();

    @Test
    void should_increment_counter() {
        counter.onLogin(LoginResult.failure(BAD_PASSWORD), ipContext(IP_1));

        final int count = counter.value(IP_1);

        assertEquals(1, count);
    }

    @Test
    void should_return_zero_count_for_unseen_key() {
        final int count = counter.value(IP_1);

        assertEquals(0, count);
    }

    @Test
    void should_track_multiple_keys() {
        counter.onLogin(LoginResult.failure(BAD_PASSWORD), ipContext(IP_1));
        counter.onLogin(LoginResult.failure(BAD_PASSWORD), ipContext(IP_2));
        counter.onLogin(LoginResult.failure(BAD_PASSWORD), ipContext(IP_2));

        assertEquals(1, counter.value(IP_1));
        assertEquals(2, counter.value(IP_2));
    }

    @Test
    void should_reset_counter_on_successful_login() {
        counter.onLogin(LoginResult.failure(BAD_PASSWORD), ipContext(IP_1));
        counter.onLogin(LoginResult.success(), ipContext(IP_1));

        final int count = counter.value(IP_1);

        assertEquals(0, count);
    }

    @Test
    void should_discard_count_after_expiration() {
        counter.onLogin(LoginResult.failure(BAD_PASSWORD), ipContext(IP_1));

        clock.tick(Duration.ofMinutes(5));

        final int count = counter.value(IP_1);

        assertEquals(0, count);
    }


    private LoginContext ipContext(final String ip) {
        final LoginContext loginContext = new LoginContext();
        loginContext.setIpAddress(ip);
        return loginContext;
    }

}