package io.github.manami.dto.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Recommendation
 */
@Data
@ToString
@EqualsAndHashCode
public class Recommendation {

    private InfoLink infoLink = null;
    private Integer amount = null;


    public Recommendation infoLink(final InfoLink infoLink) {
        this.infoLink = infoLink;
        return this;
    }


    public Recommendation amount(final Integer amount) {
        this.amount = amount;
        return this;
    }
}
