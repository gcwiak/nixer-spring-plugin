package io.nixer.nixerplugin.core.stigma.evaluate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import io.nixer.nixerplugin.core.stigma.domain.RawStigmaToken;
import io.nixer.nixerplugin.core.stigma.domain.Stigma;
import io.nixer.nixerplugin.core.stigma.domain.StigmaStatus;
import io.nixer.nixerplugin.core.stigma.storage.StigmaData;
import io.nixer.nixerplugin.core.stigma.storage.StigmaTokenStorage;
import io.nixer.nixerplugin.core.stigma.token.StigmaTokenProvider;
import io.nixer.nixerplugin.core.stigma.token.StigmaValuesGenerator;
import io.nixer.nixerplugin.core.stigma.token.validation.StigmaTokenValidator;
import io.nixer.nixerplugin.core.stigma.token.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Created on 2019-04-29.
 *
 * @author gcwiak
 */
public class StigmaTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StigmaTokenService.class);

    @Nonnull
    private final StigmaTokenProvider stigmaTokenProvider;

    @Nonnull
    private final StigmaTokenStorage stigmaTokenStorage;

    @Nonnull
    private final StigmaValuesGenerator stigmaValuesGenerator;

    @Nonnull
    private final StigmaTokenValidator stigmaTokenValidator;

    public StigmaTokenService(@Nonnull final StigmaTokenProvider stigmaTokenProvider,
                              @Nonnull final StigmaTokenStorage stigmaTokenStorage,
                              @Nonnull final StigmaValuesGenerator stigmaValuesGenerator,
                              @Nonnull final StigmaTokenValidator stigmaTokenValidator) {
        this.stigmaTokenProvider = Preconditions.checkNotNull(stigmaTokenProvider, "stigmaTokenProvider");
        this.stigmaTokenStorage = Preconditions.checkNotNull(stigmaTokenStorage, "stigmaTokenStorage");
        this.stigmaValuesGenerator = Preconditions.checkNotNull(stigmaValuesGenerator, "stigmaValuesGenerator");
        this.stigmaTokenValidator = Preconditions.checkNotNull(stigmaTokenValidator, "stigmaTokenValidator");
    }

    /**
     * To be called after successful login attempt.
     * Consumes the currently used raw stigma token (might be null or empty) and returns a new token for further usage
     * with information about validity of the original token.
     */
    @Nonnull
    public StigmaTokenFetchResult fetchTokenOnLoginSuccess(@Nullable final RawStigmaToken originalToken) {

        @Nullable final StigmaData stigmaData = tryObtainingStigmaData(originalToken);

        if (isStigmaActive(stigmaData)) {
            // TODO do we need this recording?
            // covered by observed: active->active
            // stigmaTokenStorage.recordLoginSuccessTokenValid(stigmaData.getStigmaValue());

            return new StigmaTokenFetchResult(originalToken, true);

        } else {
            // TODO do we need this recording?
            // covered by observed event or recording incoming_unreadable_token
            // stigmaTokenStorage.recordLoginSuccessTokenInvalid(originalRawToken, stigmaValueData.toString());

            return new StigmaTokenFetchResult(newStigmaToken(), false);
        }
    }

    /**
     * To be called after failed login attempt.
     * Consumes the currently used raw stigma token (might be null or empty) and returns a new token for further usage
     * with information about validity of the original token.
     */
    @Nonnull
    public StigmaTokenFetchResult fetchTokenOnLoginFail(@Nullable final RawStigmaToken originalToken) {

        @Nullable final StigmaData stigmaData = tryObtainingStigmaData(originalToken);

        if (isStigmaActive(stigmaData)) {

            revokeStigma(stigmaData);

            return new StigmaTokenFetchResult(newStigmaToken(), true);

        } else {
            // TODO do we need this recording?
            // covered by observe event or recording incoming_unreadable_token
            // stigmaTokenStorage.recordLoginFailTokenInvalid(originalRawToken, stigmaTokenCheckResult.toString());

            return new StigmaTokenFetchResult(newStigmaToken(), false);
        }
    }

    @Nullable
    private StigmaData tryObtainingStigmaData(@Nullable final RawStigmaToken originalToken) {

        try {
            return obtainStigmaData(originalToken);

        } catch (Exception e) {
            LOGGER.error("Could not obtain stigma data for raw token: '{}'", originalToken, e);
            return null;
        }
    }

    @Nullable
    private StigmaData obtainStigmaData(@Nullable final RawStigmaToken originalToken) {

        final ValidationResult tokenValidationResult = stigmaTokenValidator.validate(originalToken);

        if (tokenValidationResult.isValid() || tokenValidationResult.isReadable()) {

            return findStigmaDataInStorage(tokenValidationResult.getStigmaValue());

        } else {

            if (hasAnyValue(originalToken)) {
                // TODO record validation result as well
                stigmaTokenStorage.recordUnreadableToken(originalToken);
            } // TODO record missing token????

            return null;
        }
    }

    private boolean hasAnyValue(final RawStigmaToken originalToken) {
        return originalToken != null && StringUtils.hasText(originalToken.getValue());
    }

    private StigmaData findStigmaDataInStorage(final String stigmaValue) {

        final Stigma stigma = new Stigma(stigmaValue);

        final StigmaData stigmaValueData = stigmaTokenStorage.findStigmaData(stigma);

        if (stigmaValueData != null) {
            stigmaTokenStorage.recordStigmaObservation(stigmaValueData);
        } else {
            stigmaTokenStorage.recordSpottingUnknownStigma(stigma);
        }

        return stigmaValueData;
    }

    private boolean isStigmaActive(@Nullable final StigmaData stigmaData) {
        return stigmaData != null
                && stigmaData.getStatus() == StigmaStatus.ACTIVE;
    }

    private void revokeStigma(final StigmaData stigmaData) {
        try {
            stigmaTokenStorage.revokeStigma(stigmaData.getStigmaValue());
        } catch (Exception e) {
            LOGGER.error("Could not revoke stigma for stigma value data: '{}'", stigmaData, e);
        }
    }

    @Nonnull
    private RawStigmaToken newStigmaToken() {

        final String newStigmaValue = stigmaValuesGenerator.newStigma();

        final String stigmaValue = stigmaTokenStorage.createStigma(newStigmaValue, StigmaStatus.ACTIVE).getValue();

        return new RawStigmaToken(stigmaTokenProvider.getToken(stigmaValue).serialize());
    }
}
