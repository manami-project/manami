package io.github.manami.cache.strategies.headlessbrowser;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.MDC;

import io.github.manami.dto.entities.InfoLink;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JavaUrlConnection implements HeadlessBrowser {

    private static final int HTTP_TOO_MANY_CONNECTIONS = 429;
    private static final long MIN_WAITING_TIME = 4000L;
    private static final long MAX_WAITING_TIME = 8000L;


    /**
     * Downloads the site and returns it.
     *
     * @since 2.12.9
     * @param infoLink
     *            URL
     * @return Plain xml text of the website.
     */
    public String pageAsString(final InfoLink infoLink) {
        MDC.put("infoLink", infoLink.getUrl());
        if (!infoLink.isValid()) {
            log.warn("Seems not be a valid URL");
            return null;
        }

        final CloseableHttpClient hc = HttpClients.custom().setHttpProcessor(HttpProcessorBuilder.create().build()).build();
        final HttpGet request = new HttpGet(infoLink.getUrl());

        newArrayList(request.getAllHeaders()).forEach(request::removeHeader);

        request.setProtocolVersion(HTTP_1_1);
        request.setHeader("Host", "myanimelist.net");
        request.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:66.0) Gecko/20100101 Firefox/66.0");
        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

        String ret = null;

        try {
            final CloseableHttpResponse execute = hc.execute(request);

            if (execute.getStatusLine().getStatusCode() == HTTP_TOO_MANY_CONNECTIONS) {
                log.warn("Too many connections");
                final long waitingTime = ThreadLocalRandom.current().nextLong(MIN_WAITING_TIME, MAX_WAITING_TIME);
                log.warn("Waiting [{}]ms then retry.", waitingTime);
                Thread.sleep(waitingTime);
                return pageAsString(infoLink);
            }

            if (execute.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.error("Other error, status code [{}]", execute.getStatusLine().getStatusCode());
            }

            ret = EntityUtils.toString(execute.getEntity());
        } catch (final IOException | InterruptedException e) {
            log.error("An error occurred during download of infosite: ", e);
        }

        return ret;
    }
}