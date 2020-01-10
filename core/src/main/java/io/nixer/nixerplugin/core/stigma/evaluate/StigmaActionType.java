package io.nixer.nixerplugin.core.stigma.evaluate;

/**
 * Represents action related result of login attempt and current stigma token value.
 *
 * Created on 2019-05-13.
 *
 * @author gcwiak
 */
public enum StigmaActionType {

    TOKEN_GOOD_LOGIN_SUCCESS(false),
    SKIP_ACTION(false), // reserved for exceptional cases, e.g. 5xx server errors

    TOKEN_GOOD_LOGIN_FAIL(true),
    TOKEN_BAD_LOGIN_SUCCESS(true),
    TOKEN_BAD_LOGIN_FAIL(true);

    public final boolean isTokenRefreshRequired;

    StigmaActionType(final boolean isTokenRefreshRequired) {
        this.isTokenRefreshRequired = isTokenRefreshRequired;
    }
}
