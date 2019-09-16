package eu.xword.nixer.nixerplugin.ip;

import javax.servlet.http.HttpServletRequest;

import eu.xword.nixer.nixerplugin.filter.NixerFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IpFilter extends NixerFilter {

    private final Log logger = LogFactory.getLog(getClass());

    private IpLookup ipLookup;

    public IpFilter(final IpLookup ipLookup) {
        this.ipLookup = ipLookup;
    }

    //TODO should be apply only for login request or should it be generic and applied with request matcher.

    @Override
    protected boolean applies(final HttpServletRequest request) {
        final String ip = request.getRemoteAddr();
        final IpAddress address = ipLookup.lookup(ip);

        request.setAttribute(RequestAugmentation.IP_METADATA, new IpMetadata(address != null));
        return address != null;
    }
}
