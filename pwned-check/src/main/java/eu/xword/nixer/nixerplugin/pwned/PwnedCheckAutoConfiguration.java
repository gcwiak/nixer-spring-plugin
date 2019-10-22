package eu.xword.nixer.nixerplugin.pwned;

import java.io.FileNotFoundException;

import eu.xword.nixer.bloom.check.BloomFilterCheck;
import eu.xword.nixer.nixerplugin.pwned.check.PwnedCredentialsChecker;
import eu.xword.nixer.nixerplugin.pwned.filter.PwnedCredentialsFilter;
import eu.xword.nixer.nixerplugin.pwned.metrics.PwnedCheckMetricsConfiguration;
import eu.xword.nixer.nixerplugin.pwned.metrics.PwnedPasswordMetricsReporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ResourceUtils;

/**
 * Created on 18/09/2019.
 *
 * @author gcwiak
 */
@Configuration
@EnableConfigurationProperties(value = {PwnedCheckProperties.class})
@ConditionalOnProperty(value = "nixer.pwned.check.enabled")
@Import(value = {PwnedCheckMetricsConfiguration.class})
public class PwnedCheckAutoConfiguration {

    @Bean
    public PwnedCredentialsFilter pwnedCredentialsFilter(final PwnedCheckProperties pwnedCheckProperties,
                                                         final PwnedCredentialsChecker pwnedCredentialsChecker) {

        return new PwnedCredentialsFilter(pwnedCheckProperties.getPasswordParameter(), pwnedCredentialsChecker);
    }

    @Bean
    public PwnedCredentialsChecker pwnedCredentialsChecker(final BloomFilterCheck pwnedFilter,
                                                           final PwnedCheckProperties pwnedCheckProperties,
                                                           final PwnedPasswordMetricsReporter pwnedPasswordMetricsReporter) {
        return new PwnedCredentialsChecker(
                pwnedFilter,
                pwnedCheckProperties.getMaxPasswordLength(),
                pwnedPasswordMetricsReporter
        );
    }

    @Bean
    public BloomFilterCheck bloomFilter(final PwnedCheckProperties pwnedCheckProperties) throws FileNotFoundException {
        return BloomFilterCheck.hashingBeforeCheck(
                ResourceUtils.getFile(pwnedCheckProperties.getPwnedFilePath()).toPath()
        );
    }
}
