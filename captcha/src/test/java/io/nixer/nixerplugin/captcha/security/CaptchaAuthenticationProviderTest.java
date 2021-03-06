package io.nixer.nixerplugin.captcha.security;

import java.util.Collections;

import io.nixer.nixerplugin.captcha.error.CaptchaClientException;
import io.nixer.nixerplugin.captcha.events.FailedCaptchaAuthenticationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CaptchaAuthenticationProviderTest {

    @Mock
    private CaptchaChecker captchaChecker;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AbstractAuthenticationToken authenticationToken;

    private CaptchaAuthenticationProvider captchaAuthenticationProvider;

    @BeforeEach
    void setUp() {
        captchaAuthenticationProvider = new CaptchaAuthenticationProvider(captchaChecker, eventPublisher);
    }

    @Test
    void captchaInvalid() {
        authenticationToken = new UsernamePasswordAuthenticationToken(
                new Object(),
                new Object(),
                Collections.singletonList((GrantedAuthority) () -> "test"));
        doThrow(new CaptchaClientException("")).when(captchaChecker).checkCaptcha();


        assertThrows(CaptchaAuthenticationStatusException.class, () -> captchaAuthenticationProvider.authenticate(authenticationToken));
        assertThat(authenticationToken.isAuthenticated()).isFalse();
        verify(eventPublisher).publishEvent(any(FailedCaptchaAuthenticationEvent.class));
    }


    @Test
    void noCaptchaOrCaptchaCorrect() {
        final Authentication result = captchaAuthenticationProvider.authenticate(authenticationToken);

        assertNull(result);
    }
}
