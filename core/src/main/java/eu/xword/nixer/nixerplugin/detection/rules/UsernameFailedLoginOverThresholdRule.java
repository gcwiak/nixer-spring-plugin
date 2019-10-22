package eu.xword.nixer.nixerplugin.detection.rules;

import eu.xword.nixer.nixerplugin.events.IpFailedLoginOverThresholdEvent;
import eu.xword.nixer.nixerplugin.events.UsernameFailedLoginOverThresholdEvent;
import eu.xword.nixer.nixerplugin.login.LoginContext;
import eu.xword.nixer.nixerplugin.login.inmemory.LoginMetric;
import org.springframework.util.Assert;

/**
 * Rule that checks if number of consecutive login failures for username exceeds threshold and emits {@link IpFailedLoginOverThresholdEvent} event if it does.
 */
public class UsernameFailedLoginOverThresholdRule extends ThresholdAnomalyRule {

    private static final int THRESHOLD_VALUE = 5;

    private final LoginMetric failedLoginMetric;

    public UsernameFailedLoginOverThresholdRule(final LoginMetric failedLoginMetric) {
        super(THRESHOLD_VALUE);
        Assert.notNull(failedLoginMetric, "LoginMetric must not be null");
        this.failedLoginMetric = failedLoginMetric;
    }

    @Override
    public void execute(final LoginContext loginContext, final EventEmitter eventEmitter) {
        final String username = loginContext.getUsername();
        final int failedLogin = failedLoginMetric.value(username); //todo login Metric api not symmetrical.

        if (isOverThreshold(failedLogin)) {
            eventEmitter.accept(new UsernameFailedLoginOverThresholdEvent(username));
        }
    }

}
