package io.nixer.nixerplugin.core.domain.ip.net;

import java.net.Inet6Address;

public class Ipv6Address extends IpAddress {
    public Ipv6Address(final Inet6Address inetAddress) {
        super(inetAddress);
    }
}
