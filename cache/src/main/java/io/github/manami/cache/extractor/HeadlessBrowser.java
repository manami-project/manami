package io.github.manami.cache.extractor;

import javafx.application.Platform;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ui4j.api.browser.BrowserEngine;
import com.ui4j.api.browser.BrowserFactory;
import com.ui4j.api.browser.Page;

/**
 * A Browser which lets you download the various anime sites. It is necessary to
 * use this approach, because DDoS prevention services require the site be
 * rendered.
 *
 * @author manami-project
 * @since 2.0.0
 */
@Named
public class HeadlessBrowser {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(HeadlessBrowser.class);

    private BrowserEngine browserEngine;


    public HeadlessBrowser() {
        Platform.runLater(() -> browserEngine = BrowserFactory.getWebKit());
    }


    /**
     * Downloads the site and returns it.
     *
     * @since 2.0.0
     * @param url
     *            URL
     * @return Plain xml text of the website.
     */
    public String pageAsString(final String url) {
        if (!url.startsWith("http")) {
            LOG.warn("Seems not be a valid URL: {}", url);
            return null;
        }

        String ret = null;

        final Page page = browserEngine.navigate(url);
        ret = page.getDocument().getBody().getInnerHTML();

        return ret;
    }
}
