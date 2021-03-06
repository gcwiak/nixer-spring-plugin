package io.nixer.nixerplugin.pwned.check;

import io.nixer.bloom.check.BloomFilterCheck;
import io.nixer.nixerplugin.pwned.metrics.PwnedPasswordMetricsReporter;

/**
 * Created on 23/09/2019.
 *
 * @author gcwiak
 */
public class PwnedCredentialsChecker {

    private final BloomFilterCheck pwnedFilter;
    private final int maxPasswordLength;
    private final PwnedPasswordMetricsReporter pwnedPasswordMetrics;

    public PwnedCredentialsChecker(final BloomFilterCheck pwnedFilter,
                                   final int maxPasswordLength,
                                   final PwnedPasswordMetricsReporter pwnedPasswordMetrics) {

        this.pwnedFilter = pwnedFilter;
        this.maxPasswordLength = maxPasswordLength;
        this.pwnedPasswordMetrics = pwnedPasswordMetrics;
    }

    public boolean isPasswordPwned(final String password) {

        return pwnedPasswordMetrics.report(() -> isPwned(password));
    }

    private boolean isPwned(final String password) {
        if (password == null || password.length() > maxPasswordLength) {
            return false;
        }

        return pwnedFilter.test(password);
    }
}
