package io.github.manami.dto.entities;

import io.github.manami.dto.AnimeType;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.Lists;

/**
 * Represents an Anime with all it's saved meta information.
 *
 * @author manami-project
 * @since 1.0.0
 */
public class Anime extends AbstractMinimalEntry {

    /** Internal identifier for this anime. */
    private final UUID id;

    /** Type of the Anime (e.g.: TV, Special, OVA, ONA, etc.). */
    private AnimeType type;

    /** Number of Episodes. */
    private int episodes = 0;

    /** Location on the HDD. */
    private String location = "";

    /** Url for a picture. */
    private String picture = "";

    /** A list of related animes. */
    private final List<String> relatedAnimes;


    /**
     * Copy Constructor
     *
     * @since 2.0.0
     * @param anime
     *            {@link Anime} which will be copied.
     */
    public Anime(final Anime anime) {
        id = anime.getId();
        super.setTitle(anime.getTitle());
        type = AnimeType.findByName(anime.getType().getValue());
        episodes = anime.getEpisodes();
        super.setInfoLink(anime.getInfoLink());
        location = anime.getLocation();
        relatedAnimes = Lists.newArrayList(anime.relatedAnimes);
    }


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
    public Anime(final String title, final AnimeType type, final int episodes, final String infoLink, final String location) {
        id = UUID.randomUUID();
        super.setTitle(title);
        this.type = type;
        this.episodes = episodes;
        super.setInfoLink(infoLink);
        this.location = location;
        relatedAnimes = Lists.newArrayList();
    }


    /**
     * Empty constructor.
     *
     * @since 1.0.0
     */
    public Anime() {
        id = UUID.randomUUID();
        relatedAnimes = Lists.newArrayList();
    }


    /**
     * Constructor.
     *
     * @since 2.7.0
     */
    public Anime(final UUID id) {
        this.id = id;
        relatedAnimes = Lists.newArrayList();
    }


    /**
     * Fills every attribute in target which null.
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
        if (StringUtils.isBlank(target.getInfoLink())) {
            target.setInfoLink(source.getInfoLink());
        }
        if (StringUtils.isBlank(target.getLocation())) {
            target.setLocation(source.getLocation());
        }
        if (StringUtils.isBlank(target.getPicture())) {
            target.setPicture(source.getPicture());
        }
        if (StringUtils.isBlank(target.getThumbnail())) {
            target.setThumbnail(source.getThumbnail());
        }
        if (StringUtils.isBlank(target.getTitle())) {
            target.setTitle(source.getTitle());
        }
        if (target.getType() == null) {
            target.setType(source.getType());
        }
    }


    /**
     * Copies every attribute of an anime which is not null.
     *
     * @since 2.6.2
     * @param source
     *            {@link Anime} which is being copied.
     * @param target
     *            Instance of an {@link Anime} to which the attributes are
     *            copied to.
     */
    public static void copyNonNullSource(final Anime source, final Anime target) {
        if (source.getEpisodes() > 0) {
            target.setEpisodes(source.getEpisodes());
        }
        if (StringUtils.isNotBlank(source.getInfoLink())) {
            target.setInfoLink(source.getInfoLink());
        }
        if (StringUtils.isNotBlank(source.getLocation())) {
            target.setLocation(source.getLocation());
        }
        if (StringUtils.isNotBlank(source.getPicture())) {
            target.setPicture(source.getPicture());
        }
        if (StringUtils.isNotBlank(source.getThumbnail())) {
            target.setThumbnail(source.getThumbnail());
        }
        if (StringUtils.isNotBlank(source.getTitle())) {
            target.setTitle(source.getTitle());
        }
        if (source.getType() != null) {
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
     * @return the id
     */
    public UUID getId() {
        return id;
    }


    /**
     * @since 1.0.0
     * @return the episodes
     */
    public int getEpisodes() {
        return episodes;
    }


    /**
     * @since 1.0.0
     * @param episodes
     *            the episodes to set
     */
    public void setEpisodes(final int episodes) {
        this.episodes = episodes;
    }


    /**
     * @since 1.0.0
     * @return the location
     */
    public String getLocation() {
        return location;
    }


    /**
     * @since 1.0.0
     * @param location
     *            the location to set
     */
    public void setLocation(final String location) {
        this.location = location;
    }


    /**
     * @since 2.0.0
     * @return the type
     */
    public AnimeType getType() {
        return type;
    }


    /**
     * @since 2.0.0
     * @param type
     *            the type to set
     */
    public void setType(final AnimeType type) {
        this.type = type;
    }


    /**
     * @since 2.0.0
     * @return the type
     */
    public String getTypeAsString() {
        return (type != null) ? type.getValue() : null;
    }


    /**
     * @since 2.1.0
     * @return the picture
     */
    public String getPicture() {
        return picture;
    }


    /**
     * @since 2.1.0
     * @param picture
     *            the picture to set
     */
    public void setPicture(final String picture) {
        this.picture = picture;
    }


    /**
     * @since 2.5.1
     * @return the relatedAnimes
     */
    public List<String> getRelatedAnimes() {
        return relatedAnimes;
    }


    @Override
    public String toString() {
        return new StringBuilder().append("Anime [type=").append(type).append(", episodes=").append(episodes).append(", location=").append(location).append(", picture=").append(picture).append(", relatedAnimes=").append(relatedAnimes).append("]")
                .toString();
    }


    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(getTitle()).append(type).append(episodes).append(getInfoLink()).append(getThumbnail()).append(location).append(picture).append(relatedAnimes).toHashCode();
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Anime)) {
            return false;
        }
        final Anime other = (Anime) obj;
        if (episodes != other.episodes) {
            return false;
        }
        if (getInfoLink() == null) {
            if (other.getInfoLink() != null) {
                return false;
            }
        } else if (!getInfoLink().equals(other.getInfoLink())) {
            return false;
        }
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        if (picture == null) {
            if (other.picture != null) {
                return false;
            }
        } else if (!picture.equals(other.picture)) {
            return false;
        }
        if (relatedAnimes == null) {
            if (other.relatedAnimes != null) {
                return false;
            }
        } else if (!relatedAnimes.equals(other.relatedAnimes)) {
            return false;
        }
        if (getTitle() == null) {
            if (other.getTitle() != null) {
                return false;
            }
        } else if (!getTitle().equals(other.getTitle())) {
            return false;
        }
        if (getThumbnail() == null) {
            if (other.getThumbnail() != null) {
                return false;
            }
        } else if (!getThumbnail().equals(other.getThumbnail())) {
            return false;
        }
        return type == other.type;
    }
}
