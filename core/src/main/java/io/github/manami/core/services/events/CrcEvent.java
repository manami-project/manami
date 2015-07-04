package io.github.manami.core.services.events;

import java.nio.file.Path;

/**
 * @author manami project
 *
 */
public class CrcEvent extends AbstractEvent {

    private Path path;

    private String crcSum;


    /**
     * @return the path
     */
    public Path getPath() {
        return path;
    }


    /**
     * @param path
     *            the path to set
     */
    public void setPath(final Path path) {
        this.path = path;
    }


    /**
     * @return the crcSum
     */
    public String getCrcSum() {
        return crcSum;
    }


    /**
     * @param crcSum
     *            the crcSum to set
     */
    public void setCrcSum(final String crcSum) {
        this.crcSum = crcSum;
    }
}
