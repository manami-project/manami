package io.github.manami.cache.strategies.headlessbrowser.extractor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.inject.Named;

import org.apache.commons.validator.routines.UrlValidator;

import com.moodysalem.phantomjs.wrapper.PhantomJS;
import com.moodysalem.phantomjs.wrapper.beans.PhantomJSExecutionResponse;

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


    public HeadlessBrowser() {
        urlValidator = new UrlValidator(VALID_SCHEMES);
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

        final StringBuilder command = new StringBuilder("var page = require('webpage').create();page.open('").append(url).append(
                "', function (status) { if (status !== 'success') { console.log('Unable to access network'); } else { var p = page.evaluate(function () { return document.getElementsByTagName('html')[0].innerHTML }); console.log(p); } phantom.exit(); });");
        final InputStream stream = new ByteArrayInputStream(command.toString().getBytes(StandardCharsets.UTF_8));

        try {
            final PhantomJSExecutionResponse resp = PhantomJS.exec(stream);
            ret = resp.getStdOut();
        } catch (final IOException e) {
            log.error("Unable to download webpage [{}] as String", url, e);
        }

        return ret;
    }
}
