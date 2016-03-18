package io.github.manami.gui.components;

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

    /** Button to update this entry. */
    private Button updateButton;

    /** HBox containing additional Buttons */
    private HBox additionalButtons;

    private Button removeButton;


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


    public Button getUpdateButton() {
        return updateButton;
    }


    public void setUpdateButton(final Button updateButton) {
        this.updateButton = updateButton;
    }


    public HBox getAdditionalButtons() {
        return additionalButtons;
    }


    public void setAdditionalButtons(final HBox additionalButtons) {
        this.additionalButtons = additionalButtons;
    }


    public Button getRemoveButton() {
        return removeButton;
    }


    public void setRemoveButton(final Button removeButton) {
        this.removeButton = removeButton;
    }
}
