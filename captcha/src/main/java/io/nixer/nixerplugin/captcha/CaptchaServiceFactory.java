package io.nixer.nixerplugin.captcha;

/**
 * Factory for creating {@link CaptchaService} for given action
 */
public interface CaptchaServiceFactory {

    CaptchaService createCaptchaService(final String action);
}
