package io.github.manami.cache.strategies.headlessbrowser.extractor;

import com.moodysalem.phantomjs.wrapper.PhantomJS;
import com.moodysalem.phantomjs.wrapper.beans.PhantomJSExecutionResponse;
import io.github.manami.dto.entities.InfoLink;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A Browser which lets you download the various anime sites. It is necessary to
 * use this approach, because DDoS prevention services require the site be
 * rendered.
 *
 * @author manami-project
 * @since 2.0.0
 */
@Slf4j
public class HeadlessBrowser {

    /**
     * Downloads the site and returns it.
     *
     * @since 2.0.0
     * @param infoLink
     *            URL
     * @return Plain xml text of the website.
     */
    public String pageAsString(final InfoLink infoLink) {
        if (!infoLink.isValid()) {
            log.warn("Seems not be a valid URL: {}", infoLink);
            return null;
        }

        String ret = null;

        final StringBuilder command = new StringBuilder("var page = require('webpage').create();page.open('").append(infoLink).append(
                "', function (status) { if (status !== 'success') { console.log('Unable to access network'); } else { var p = page.evaluate(function () { return document.getElementsByTagName('html')[0].innerHTML }); console.log(p); } phantom.exit(); });");
        final InputStream stream = new ByteArrayInputStream(command.toString().getBytes(StandardCharsets.UTF_8));

        try {
            final PhantomJSExecutionResponse resp = PhantomJS.exec(stream);
            ret = resp.getStdOut();
        } catch (final IOException e) {
            log.error("Unable to download webpage [{}] as String", infoLink, e);
        }

        return ret;
    }
}
