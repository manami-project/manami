package io.github.manami.dto.comparator;

import java.util.Comparator;

import io.github.manami.dto.entities.MinimalEntry;

import org.apache.commons.lang3.StringUtils;

/**
 * @author manami project
 * @since 2.7.2
 */
public class MinimalEntryComByTitleAsc implements Comparator<MinimalEntry> {

    @Override
    public int compare(final MinimalEntry objA, final MinimalEntry objB) {
        int ret = 0;

        if (objA != null && StringUtils.isNotBlank(objA.getTitle()) && objB != null && StringUtils.isNotBlank(objB.getTitle())) {
            ret = objA.getTitle().compareToIgnoreCase(objB.getTitle());
        }

        return ret;
    }
}
