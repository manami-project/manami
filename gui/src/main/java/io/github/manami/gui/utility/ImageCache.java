package io.github.manami.gui.utility;

import org.springframework.stereotype.Component;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;
import javafx.scene.image.Image;

@Component
public class ImageCache {

    private Map<InfoLink, SoftReference<Image>> pictureCache = new ConcurrentHashMap<>();
    private Map<InfoLink, SoftReference<Image>> thumbnailCache = new ConcurrentHashMap<>();

    public Image loadPicture(Anime anime) {
        if (pictureCache.containsKey(anime.getInfoLink())) {
            return pictureCache.get(anime.getInfoLink()).get();
        }

        Image image = new Image(anime.getPicture(), true);
        pictureCache.put(anime.getInfoLink(), new SoftReference<>(image));

        return image;
    }

    public Image loadThumbnail(MinimalEntry anime) {
        if (thumbnailCache.containsKey(anime.getInfoLink())) {
            return thumbnailCache.get(anime.getInfoLink()).get();
        }

        Image image = new Image(anime.getThumbnail(), true);
        thumbnailCache.put(anime.getInfoLink(), new SoftReference<>(image));

        return image;
    }
}
