package io.github.manami.dto;

import lombok.Getter;

/**
 * Types of anime which are known.
 * 
 * @author manami-project
 * @version 2.0.0
 */
public enum AnimeType {
    /**
     * TV
     */
    TV("TV"),
    /**
     * Movie
     */
    MOVIE("Movie"),
    /**
     * OVA
     */
    OVA("OVA"),
    /**
     * Special
     */
    SPECIAL("Special"),
    /**
     * ONA
     */
    ONA("ONA"),
    /**
     * Music
     */
    MUSIC("Music");

    /** String representation. */
    @Getter
    private final String value;


    /**
     * @param type Type as String value.
     */
    private AnimeType(final String value) {
        this.value = value;
    }


    /**
     * Returns a type by a String comparison. It's not case sensitive.
     * 
     * @param name Type as String value.
     * @return The corresponding AnimeType or null if no type matches.
     */
    public static AnimeType findByName(final String name) {
        for (final AnimeType type : values()) {
            if (type.getValue().equalsIgnoreCase((name))) {
                return type;
            }
        }
        return null;
    }
}
