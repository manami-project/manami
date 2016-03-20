package io.github.manami.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * @author manami-project
 * @since 2.6.0
 */
public class CheckListEntry {

    /** Image of the anime. */
    private ImageView pictureComponent;

    /** Link containing the title and the info link. */
    private Node titleComponent;

    private Label messageComponent;

    /** HBox containing additional Buttons */
    private final HBox additionalButtons;

    private Button removeButton;


    public CheckListEntry() {
        additionalButtons = new HBox(10);
        additionalButtons.setAlignment(Pos.CENTER);
    }


    public ImageView getPictureComponent() {
        return pictureComponent;
    }


    public void setPictureComponent(final ImageView pictureComponent) {
        this.pictureComponent = pictureComponent;
    }


    public Node getTitleComponent() {
        return titleComponent;
    }


    public void setTitleComponent(final Node titleComponent) {
        this.titleComponent = titleComponent;
    }


    public Label getMessageComponent() {
        return messageComponent;
    }


    public void setMessageComponent(final Label messageComponent) {
        this.messageComponent = messageComponent;
    }


    public HBox getAdditionalButtons() {
        return additionalButtons;
    }


    public void addAdditionalButtons(final Button additionalButton) {
        if (additionalButton != null) {
            additionalButtons.getChildren().add(additionalButton);
            HBox.setMargin(additionalButtons, new Insets(40.0, 0.0, 0.0, 10.0));
        }
    }


    public Button getRemoveButton() {
        return removeButton;
    }


    public void setRemoveButton(final Button removeButton) {
        this.removeButton = removeButton;
    }
}
