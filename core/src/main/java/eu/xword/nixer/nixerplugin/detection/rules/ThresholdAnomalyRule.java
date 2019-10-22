package eu.xword.nixer.nixerplugin.detection.rules;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.util.Assert;

public abstract class ThresholdAnomalyRule implements AnomalyRule {

    private final AtomicInteger threshold;

    public ThresholdAnomalyRule(final int threshold) {
        this.threshold = new AtomicInteger(threshold);
    }

    protected boolean isOverThreshold(int value) {
        return value > threshold.get();
    }

    //todo add actuator endpoint to read/update it
    public void setThreshold(final int threshold) {
        Assert.isTrue(threshold > 1, "Threshold must be greater than 1");
        this.threshold.set(threshold);
    }
}
