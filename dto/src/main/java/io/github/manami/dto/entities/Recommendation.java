package io.github.manami.dto.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Recommendation {

    @Getter
    private InfoLink infoLink = null;

    @Getter
    @Setter
    private Integer amount = null;


    public Recommendation(final InfoLink infoLink, final Integer amount) {
        this.infoLink = infoLink;
        this.amount = amount;
    }
}
