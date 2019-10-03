package eu.xword.nixer.nixerplugin.captcha.config;

import eu.xword.nixer.nixerplugin.captcha.CaptchaService;
import eu.xword.nixer.nixerplugin.captcha.CaptchaServiceFactory;
import eu.xword.nixer.nixerplugin.captcha.endpoint.CaptchaEndpoint;
import eu.xword.nixer.nixerplugin.captcha.security.CaptchaChecker;
import eu.xword.nixer.nixerplugin.captcha.security.CaptchaCondition;
import eu.xword.nixer.nixerplugin.captcha.validation.CaptchaValidator;
import eu.xword.nixer.nixerplugin.login.LoginFailureTypeRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static eu.xword.nixer.nixerplugin.captcha.metrics.CaptchaMetrics.LOGIN_ACTION;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto-configuration} for Captcha.
 *
 */
@Configuration
@EnableConfigurationProperties(value = {LoginCaptchaProperties.class})
//@ConditionalOnProperty(value = "nixer.login.captcha.enabled", havingValue = "true", matchIfMissing = LoginCaptchaProperties.DEFAULT)
public class CaptchaConfiguration {

    @Bean
    public CaptchaEndpoint captchaEndpoint(CaptchaChecker captchaChecker) {
        return new CaptchaEndpoint(captchaChecker);
    }

    @Bean
    public CaptchaChecker captchaChecker(CaptchaServiceFactory captchaServiceFactory,
                                         LoginCaptchaProperties loginCaptchaProperties,
                                         LoginFailureTypeRegistry loginFailureTypeRegistry) {
        final CaptchaCondition captchaCondition = CaptchaCondition.valueOf(loginCaptchaProperties.getCondition());
        final CaptchaService captchaService = captchaServiceFactory.createCaptchaService(LOGIN_ACTION);

        final CaptchaChecker captchaChecker = new CaptchaChecker(captchaService, loginFailureTypeRegistry);
        captchaChecker.setCaptchaParam(loginCaptchaProperties.getParam());
        captchaChecker.setCaptchaCondition(captchaCondition);
        return captchaChecker;
    }

    @Bean
    public CaptchaValidator captchaValidator(CaptchaServiceFactory captchaServiceFactory) {
        return new CaptchaValidator(captchaServiceFactory);
    }
}
