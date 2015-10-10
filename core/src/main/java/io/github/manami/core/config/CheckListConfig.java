package io.github.manami.core.config;

/**
 * @author manami-project
 * @since 2.6.0
 */
public class CheckListConfig {

    private final boolean checkLocations;
    private final boolean checkCrc;
    private final boolean checkMetaData;


    public CheckListConfig(final boolean checkLocations, final boolean checkCrc, final boolean checkMetaData) {
        this.checkLocations = checkLocations;
        this.checkCrc = checkCrc;
        this.checkMetaData = checkMetaData;
    }


    /**
     * @return the checkLocations
     */
    public boolean isCheckLocations() {
        return checkLocations;
    }


    /**
     * @return the checkCrc
     */
    public boolean isCheckCrc() {
        return checkCrc;
    }


    /**
     * @return the checkMetaData
     */
    public boolean isCheckMetaData() {
        return checkMetaData;
    }
}
