package io.github.manami.dto.entities;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.UUID;

import io.github.manami.dto.AnimeType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents an Anime with all it's saved meta information.
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
    private String location = EMPTY;

    /** Url for a picture. */
    @Getter
    @Setter
    private String picture = EMPTY;


    private Anime(final UUID id) {
        this.id = id;
    }


    public Anime(final String title, final InfoLink infoLink) {
        id = randomUUID();
        super.setTitle(title);
        super.setInfoLink(infoLink);
    }


    public Anime(final UUID id, final String title, final InfoLink infoLink) {
        this.id = id;
        super.setTitle(title);
        super.setInfoLink(infoLink);
    }


    public Anime type(final AnimeType animeType) {
        type = animeType;
        return this;
    }


    public Anime episodes(final int episodes) {
        setEpisodes(episodes);
        return this;
    }


    public Anime location(final String location) {
        this.location = location;
        return this;
    }


    public static Anime copyAnime(final Anime source) {
        final Anime ret = new Anime(source.getId());
        ret.setEpisodes(source.getEpisodes());
        ret.setInfoLink(source.getInfoLink());
        ret.setLocation(source.getLocation());
        ret.setPicture(source.getPicture());
        ret.setThumbnail(source.getThumbnail());
        ret.setTitle(source.getTitle());
        ret.setType(source.getType());
        return ret;
    }


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
