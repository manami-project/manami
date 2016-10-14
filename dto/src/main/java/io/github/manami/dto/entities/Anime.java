package io.github.manami.dto.entities;

import io.github.manami.dto.AnimeType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Represents an Anime with all it's saved meta information.
 * 
 * @author manami-project
 * @since 1.0.0
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Anime extends AbstractMinimalEntry {

    /** Internal identifier for this anime. */
    @Getter
    private final UUID id;

    /** Type of the Anime (e.g.: TV, Special, OVA, ONA, etc.). */
    @Getter
    @Setter
    private AnimeType type;

    /** Number of Episodes. */
    @Getter
    private int episodes = 0;

    /** Location on the HDD. */
    @Getter
    @Setter
    private String location = "";

    /** Url for a picture. */
    @Getter
    @Setter
    private String picture = "";


    /**
     * Constructor awaiting all Attributes.
     * 
     * @since 2.0.0
     * @param title
     *            Title of the anime.
     * @param type
     *            Type.
     * @param episodes
     *            Number of episodes.
     * @param infoLink
     *            Link to a website with more information.
     * @param location
     *            Location on the HDD.
     */
    public Anime(final String title, final AnimeType type, final int episodes, final InfoLink infoLink, final String location) {
        super.setTitle(title);
        super.setInfoLink(infoLink);
        this.type = type;
        this.episodes = episodes;
        this.location = location;
        id = randomUUID();
    }


    /**
     * Empty constructor.
     * 
     * @since 1.0.0
     */
    public Anime() {
        id = randomUUID();
    }


    /**
     * Constructor.
     * 
     * @since 2.7.0
     */
    public Anime(final UUID id) {
        this.id = id;
    }


    /**
     * Fills every attribute in target which is null.
     * 
     * @since 2.7.0
     * @param source
     *            {@link Anime} which is being copied.
     * @param target
     *            Instance of an {@link Anime} to which the attributes are
     *            copied to.
     */
    public static void copyNullTarget(final Anime source, final Anime target) {
        if (target.getEpisodes() == 0) {
            target.setEpisodes(source.getEpisodes());
        }

        if (!target.getInfoLink().isPresent()) {
            target.setInfoLink(source.getInfoLink());
        }

        if (isBlank(target.getLocation())) {
            target.setLocation(source.getLocation());
        }

        if (isBlank(target.getPicture())) {
            target.setPicture(source.getPicture());
        }

        if (AbstractMinimalEntry.NO_IMG_THUMB.equals(target.getThumbnail())) {
            target.setThumbnail(source.getThumbnail());
        }

        if (isBlank(target.getTitle())) {
            target.setTitle(source.getTitle());
        }

        if (target.getType() == null) {
            target.setType(source.getType());
        }
    }


    /**
     * Copies every attribute of an anime.
     * 
     * @since 2.6.2
     * @param source
     *            {@link Anime} which is being copied.
     * @param target
     *            Instance of an {@link Anime} to which the attributes are
     *            copied to.
     */
    public static void copyAnime(final Anime source, final Anime target) {
        target.setEpisodes(source.getEpisodes());
        target.setInfoLink(source.getInfoLink());
        target.setLocation(source.getLocation());
        target.setPicture(source.getPicture());
        target.setThumbnail(source.getThumbnail());
        target.setTitle(source.getTitle());
        target.setType(source.getType());
    }


    /**
     * @since 2.0.0 @return the type
     */
    public String getTypeAsString() {
        return (type != null) ? type.getValue() : null;
    }


    public void setEpisodes(final int episodes) {
        if (episodes >= 0) {
            this.episodes = episodes;
        }
    }


    /**
     * Checks if an anime entry is valid.
     * 
     * @param anime
     * @return
     */
    public static boolean isValidAnime(final Anime anime) {
        boolean ret = anime != null;

        if (!ret) {
            return ret;
        }

        ret &= anime.getId() != null;
        ret &= anime.getType() != null;
        ret &= anime.getEpisodes() >= 0;
        ret &= isNotBlank(anime.getTitle());
        ret &= isNotBlank(anime.getLocation());

        return ret;
    }
}
