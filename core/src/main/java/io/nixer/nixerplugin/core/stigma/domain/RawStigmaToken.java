package io.nixer.nixerplugin.core.stigma.domain;

import javax.annotation.Nonnull;

import com.google.common.base.Objects;
import io.nixer.nixerplugin.core.stigma.token.StigmaExtractor;
import io.nixer.nixerplugin.core.stigma.token.validation.StigmaTokenValidator;
import org.springframework.util.Assert;

/**
 * Represents raw, not parsed, Stigma Token, i.e. a string that is kept by a browser as Cookie.
 * <br>
 * <br>
 * Stigma Token wraps {@link Stigma} providing confidentiality and integrity.
 * <pre>
 * +---------------------------+
 * | Stigma Token              |
 * |                           |
 * |            +-----------+  |
 * |            | Stigma    |  |
 * |            |           |  |
 * |            +-----------+  |
 * +---------------------------+
 * </pre>
 *
 * Stigma Token is implemented as encrypted <a href="https://en.wikipedia.org/wiki/JSON_Web_Token">JWT</a>.
 * <br>
 * <br>
 * See also {@link StigmaExtractor} and {@link StigmaTokenValidator}.
 */
public class RawStigmaToken {

    @Nonnull
    private final String value;

    public RawStigmaToken(final String value) {
        Assert.notNull(value, "value must not be null");
        this.value = value;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RawStigmaToken that = (RawStigmaToken) o;
        return Objects.equal(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
