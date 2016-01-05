package io.github.manami.gui.components;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

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


    /**
     * @return the pictureComponent
     */
    public ImageView getPictureComponent() {
        return pictureComponent;
    }


    /**
     * @param pictureComponent
     *            the pictureComponent to set
     */
    public void setPictureComponent(final ImageView pictureComponent) {
        this.pictureComponent = pictureComponent;
    }


    /**
     * @return the titleComponent
     */
    public Node getTitleComponent() {
        return titleComponent;
    }


    /**
     * @param titleComponent
     *            the titleComponent to set
     */
    public void setTitleComponent(final Node titleComponent) {
        this.titleComponent = titleComponent;
    }


    /**
     * @return the messageComponent
     */
    public Label getMessageComponent() {
        return messageComponent;
    }


    /**
     * @param messageComponent
     *            the messageComponent to set
     */
    public void setMessageComponent(final Label messageComponent) {
        this.messageComponent = messageComponent;
    }


    /**
     * @return the updateButton
     */
    public Button getUpdateButton() {
        return updateButton;
    }


    /**
     * @param updateButton
     *            the deletionButton to set
     */
    public void setUpdateButton(final Button updateButton) {
        this.updateButton = updateButton;
    }
}
