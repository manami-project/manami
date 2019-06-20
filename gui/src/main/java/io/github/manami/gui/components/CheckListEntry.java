package io.github.manami.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;

public class CheckListEntry {

    /** Image of the anime. */
    @Getter
    @Setter
    private ImageView pictureComponent;

    /** Link containing the title and the info link. */
    @Getter
    @Setter
    private Node titleComponent;

    @Getter
    @Setter
    private Label messageComponent;

    /** HBox containing additional Buttons */
    @Getter
    private final HBox additionalButtons;

    @Getter
    @Setter
    private Button removeButton;


    public CheckListEntry() {
        additionalButtons = new HBox(10);
        additionalButtons.setAlignment(Pos.CENTER);
    }


    public void addAdditionalButtons(final Button additionalButton) {
        if (additionalButton != null) {
            additionalButtons.getChildren().add(additionalButton);
            HBox.setMargin(additionalButtons, new Insets(40.0, 0.0, 0.0, 10.0));
        }
    }
}
