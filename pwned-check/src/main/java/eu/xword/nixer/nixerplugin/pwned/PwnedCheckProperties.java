package eu.xword.nixer.nixerplugin.pwned;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created on 24/09/2019.
 *
 * @author gcwiak
 */
@ConfigurationProperties(prefix = "nixer.pwned.check")
public class PwnedCheckProperties {

    /**
     * Indicates pwned-check functionality is enabled.
     * Used by {@link PwnedCheckAutoConfiguration}, kept here for documentation purposes.
     */
    private boolean enabled;

    /**
     * Location of leaked credentials data file
     */
    private String pwnedFilePath;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getPwnedFilePath() {
        return pwnedFilePath;
    }

    public void setPwnedFilePath(final String pwnedFilePath) {
        this.pwnedFilePath = pwnedFilePath;
    }

}
