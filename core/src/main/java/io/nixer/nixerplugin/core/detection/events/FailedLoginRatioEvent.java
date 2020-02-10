package io.nixer.nixerplugin.core.detection.events;

/**
 * Needed in registry to capture both activation and deactivation events
 */
public abstract class FailedLoginRatioEvent extends AnomalyEvent {

    public static final String FAILED_LOGIN_RATIO_ACTIVATION = "FAILED_LOGIN_RATIO_ACTIVATION";
    public static final String FAILED_LOGIN_RATIO_DEACTIVATION = "FAILED_LOGIN_RATIO_DEACTIVATION";

    private final String ratio;

    public FailedLoginRatioEvent(double ratio) {
        super(ratio);
        this.ratio = String.valueOf(ratio);
    }

    public String getRatio() {
        return ratio;
    }
}
