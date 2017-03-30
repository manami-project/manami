package io.github.manami.cache.strategies.headlessbrowser.extractor;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.util.EntityUtils;

import io.github.manami.dto.entities.InfoLink;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JavaUrlConnection implements HeadlessBrowser {

    /**
     * Downloads the site and returns it.
     *
     * @since 2.12.9
     * @param infoLink
     *            URL
     * @return Plain xml text of the website.
     */
    public String pageAsString(final InfoLink infoLink) {
        if (!infoLink.isValid()) {
            log.warn("Seems not be a valid URL: [{}]", infoLink);
            return null;
        }

        final CloseableHttpClient hc = HttpClients.custom().setHttpProcessor(HttpProcessorBuilder.create().build()).build();

        final HttpGet request = new HttpGet(infoLink.getUrl());
        newArrayList(request.getAllHeaders()).forEach(header -> {
            request.removeHeader(header);
        });
        request.setProtocolVersion(HttpVersion.HTTP_1_1);

        request.setHeader("Host", "myanimelist.net");
        request.setHeader("User-Agent", "curl/7.53.0");
        request.setHeader("Accept", "*/*");

        String ret = null;
        try {
            final CloseableHttpResponse execute = hc.execute(request);

            if (execute.getStatusLine().getStatusCode() == 429) {
                log.error("TOO MANY CONNECTIONS!");
                final long waitingTime = ThreadLocalRandom.current().nextLong(4000L, 8000L);
                log.warn("Waiting [{}]ms then retry \n\n\n", waitingTime);
                Thread.sleep(waitingTime);
                return pageAsString(infoLink);
            }
            if (execute.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.error("OTHER ERROR, status code [{}] for entry [{}]", execute.getStatusLine().getStatusCode(), infoLink.getUrl());
            }

            ret = EntityUtils.toString(execute.getEntity());
        } catch (final ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }
}
