package io.github.manami.cache.extractor;

import java.io.IOException;
import java.util.List;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.UrlFetchWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

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

    /** Instance of the {@link WebClient} which represents the Browser. */
    private final WebClient webClient;

    private HtmlPage page;


    /**
     * Constructor.
     *
     * @since 2.0.0
     */
    public HeadlessBrowser() {
        webClient = new WebClient(BrowserVersion.FIREFOX_38);
        webClient.getOptions().setActiveXNative(false);
        webClient.getOptions().setAppletEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setDoNotTrackEnabled(true);
        webClient.getOptions().setGeolocationEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setPopupBlockerEnabled(true);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setUseInsecureSSL(false);
        webClient.setWebConnection(new UrlFetchWebConnection(webClient)); // experimental
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
            LOG.warn("Seems not be valid URL: {}", url);
            return null;
        }

        String ret = null;
        try {
            page = webClient.getPage(url);
            ret = page.getWebResponse().getContentAsString();
            closePageExplicitly();
        } catch (FailingHttpStatusCodeException | IOException e) {
            LOG.error("Failed to download the following url: {}", url, e);
            webClient.close();
        }

        return ret;
    }


    /**
     * Needed in order to fix the out-of-memory problem. MAL probably introduced
     * new heavy javascripts. Thus manami resulted in an out-of-memory. As a
     * consequence it's necessary to clean up properly.
     * 
     * @since 2.9.1
     */
    private void closePageExplicitly() {
        page.cleanUp();
        final List<WebWindow> windows = webClient.getWebWindows();

        for (final WebWindow wd : windows) {
            wd.getJobManager().removeAllJobs();
            webClient.deregisterWebWindow(wd);
        }

        page = null;
        System.gc(); // FIXME: temp fix. test later without
    }
}
