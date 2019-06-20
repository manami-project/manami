package io.github.manami.dto.comparator;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Comparator;

import io.github.manami.dto.entities.MinimalEntry;

public class MinimalEntryComByTitleAsc implements Comparator<MinimalEntry> {

    @Override
    public int compare(final MinimalEntry objA, final MinimalEntry objB) {
        int ret = 0;

        if (objA != null && isNotBlank(objA.getTitle()) && objB != null && isNotBlank(objB.getTitle())) {
            ret = objA.getTitle().compareToIgnoreCase(objB.getTitle());
        }

        return ret;
    }
}
