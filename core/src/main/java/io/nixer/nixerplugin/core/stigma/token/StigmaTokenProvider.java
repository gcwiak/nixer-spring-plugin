package io.nixer.nixerplugin.core.stigma.token;


import com.nimbusds.jwt.JWT;
import io.nixer.nixerplugin.core.stigma.domain.Stigma;

/**
 * Created on 2019-05-20.
 *
 * @author gcwiak
 */
public interface StigmaTokenProvider {

    JWT getToken(Stigma stigma);
}
