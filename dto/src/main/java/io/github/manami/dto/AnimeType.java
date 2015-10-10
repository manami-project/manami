package io.github.manami.dto;

/**
 * Types of animes which are known.
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
    private final String type;


    /**
     * @since 2.0.0
     * @param type
     *            Type as String value.
     */
    AnimeType(final String type) {
        this.type = type;
    }


    /**
     * @since 2.0.0
     * @return the type
     */
    public String getValue() {
        return type;
    }


    /**
     * Returns a type by a String comparison. It's not case sensitive.
     *
     * @since 2.0.0
     * @param name
     *            Type as String value.
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
