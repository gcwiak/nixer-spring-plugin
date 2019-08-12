package eu.xword.nixer.nixerplugin.captcha;

import eu.xword.nixer.nixerplugin.NixerProperties;
import eu.xword.nixer.nixerplugin.blocking.policies.AutomaticCaptchaStrategy;
import eu.xword.nixer.nixerplugin.captcha.endpoint.CaptchaEndpoint;
import eu.xword.nixer.nixerplugin.captcha.metrics.MetricsReporterFactory;
import eu.xword.nixer.nixerplugin.captcha.metrics.MicrometerMetricsReporterFactory;
import eu.xword.nixer.nixerplugin.captcha.metrics.NOPMetricsReporter;
import eu.xword.nixer.nixerplugin.captcha.strategy.CaptchaStrategy;
import eu.xword.nixer.nixerplugin.captcha.strategy.StrategyRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnProperty(value = "nixer.login.captcha.enabled", havingValue = "true", matchIfMissing = false)
//@ConditionalOnExpression(value = "#{@nixerProperties.getCaptcha().isEnabled()}")
public class CaptchaConfiguration {

    @Bean
    @ConditionalOnClass(HttpClient.class)
    public ClientHttpRequestFactory apacheClientHttpRequestFactory(RecaptchaProperties recaptcha) {
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(20); // TODO make configurable

        final RecaptchaProperties.Timeout timeout = recaptcha.getTimeout();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(timeout.getConnectionRequest())
                .setConnectTimeout(timeout.getConnect())
                .setSocketTimeout(timeout.getRead())
                .build();

        final CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }


    @Bean("captchaRestTemplate")
    @ConditionalOnMissingBean(name = "captchaRestTemplate")
    public RestTemplate restTemplate(ClientHttpRequestFactory requestFactory) {
        return new RestTemplate(requestFactory);
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    public MetricsReporterFactory metricsReporterFactory(MeterRegistry meterRegistry) {
        return new MicrometerMetricsReporterFactory(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricsReporterFactory nopMetricsReporterFactory() {
        return action -> new NOPMetricsReporter();
    }

    @Bean
//    @ConditionalOnEnabledEndpoint(endpoint = CaptchaEndpoint.class) // TODO consider if needed
    @ConditionalOnBean(CaptchaChecker.class)
    @ConditionalOnMissingBean
    public CaptchaEndpoint captchaEndpoint(CaptchaChecker captchaChecker, StrategyRegistry strategyRegistry) {
        return new CaptchaEndpoint(captchaChecker, strategyRegistry);
    }

    @Bean
    public StrategyRegistry strategyRegistry(AutomaticCaptchaStrategy automaticCaptchaStrategy) {
        final StrategyRegistry strategyRegistry = new StrategyRegistry();
        strategyRegistry.registerStrategy(automaticCaptchaStrategy);
        return strategyRegistry;
    }

    @Bean
    public CaptchaChecker captchaChecker(CaptchaServiceFactory captchaServiceFactory, NixerProperties properties, StrategyRegistry strategyRegistry) {
        final CaptchaLoginProperties captchaLoginProperties = properties.getCaptcha();
        final CaptchaStrategy captchaStrategy = strategyRegistry.valueOf(captchaLoginProperties.getStrategy());
        final CaptchaChecker captchaChecker = new CaptchaChecker(captchaServiceFactory, captchaLoginProperties);
        captchaChecker.setCaptchaStrategy(captchaStrategy);

        return captchaChecker;
    }

    @Bean
    public AutomaticCaptchaStrategy automaticCaptchaStrategy() {
        return new AutomaticCaptchaStrategy();
    }
}
