package io.github.manami.cache.extractor;

import javax.inject.Named;

import org.apache.commons.validator.routines.UrlValidator;

import com.ui4j.api.browser.BrowserEngine;
import com.ui4j.api.browser.BrowserFactory;
import com.ui4j.api.browser.Page;

import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

/**
 * A Browser which lets you download the various anime sites. It is necessary to
 * use this approach, because DDoS prevention services require the site be
 * rendered.
 * 
 * @author manami-project
 * @since 2.0.0
 */
@Named
@Slf4j
public class HeadlessBrowser {

    private static final String[] VALID_SCHEMES = new String[] { "HTTP", "HTTPS" };
    private final UrlValidator urlValidator;
    private BrowserEngine browserEngine;


    public HeadlessBrowser() {
        urlValidator = new UrlValidator(VALID_SCHEMES);
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
        if (!urlValidator.isValid(url)) {
            log.warn("Seems not be a valid URL: {}", url);
            return null;
        }

        String ret = null;

        final Page page = browserEngine.navigate(url);
        ret = page.getDocument().getBody().getInnerHTML();

        return ret;
    }
}
