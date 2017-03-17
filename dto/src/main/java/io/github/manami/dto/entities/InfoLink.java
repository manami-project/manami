package io.github.manami.dto.entities;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.validator.routines.UrlValidator;

import lombok.Getter;

public class InfoLink {

    private static final String[] VALID_SCHEMES = new String[] { "HTTP", "HTTPS" };

    @Getter
    private final String url;


    public InfoLink(final String url) {
        this.url = url;
    }


    public static UrlValidator getUrlValidator() {
        return new UrlValidator(VALID_SCHEMES);
    }


    /**
     * First checks a value is present and then checks if the given value is
     * vlaid.
     *
     * @return
     */
    public boolean isValid() {
        if (!isPresent()) {
            return false;
        }

        return getUrlValidator().isValid(url);
    }


    /**
     * Checks if a value is actually present
     *
     * @return
     */
    public boolean isPresent() {
        return isNotBlank(url);
    }


    @Override
    public String toString() {
        return url;
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof InfoLink)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        final InfoLink otherInfoLink = (InfoLink) obj;

        if (isPresent() && otherInfoLink.isPresent()) {
            return url.equals(otherInfoLink.getUrl());
        }

        return false;
    }


    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
