package io.github.manami.gui.components;

import javafx.scene.Node;
import javafx.scene.paint.Color;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

/**
 * It's not possible to use constants for the glyphs, because if you try it a
 * glyph is only shown once in the entire GUI. Thats why you have to instantiate
 * each glyph every time.
 *
 * @author manami-project
 * @since 2.9.1
 */
public class Icons {

    private static final Color DEFAULT_ICON_COLOR = Color.DARKGRAY;
    private static final Color ICON_COLOR_RED = Color.DARKRED;
    private static final Color ICON_COLOR_DIMGRAY = Color.DIMGREY;
    private static final GlyphFont FONT_AWESOME = GlyphFontRegistry.font("FontAwesome");


    /**
     * @return the iconFileText
     */
    public static Node createIconFileText() {
        return FONT_AWESOME.create(FontAwesome.Glyph.FILE_TEXT_ALT).color(DEFAULT_ICON_COLOR);
    }


    /**
     * @return the iconFile
     */
    public static Node createIconFile() {
        return FONT_AWESOME.create(FontAwesome.Glyph.FILE_ALT).color(DEFAULT_ICON_COLOR);
    }


    /**
     * @return the iconFolderOpen
     */
    public static Node createIconFolderOpen() {
        return FONT_AWESOME.create(FontAwesome.Glyph.FOLDER_OPEN_ALT).color(DEFAULT_ICON_COLOR);
    }


    /**
     * @return the iconSave
     */
    public static Node createIconSave() {
        return FONT_AWESOME.create(FontAwesome.Glyph.SAVE).color(DEFAULT_ICON_COLOR);
    }


    /**
     * @return the iconImport
     */
    public static Node createIconImport() {
        return FONT_AWESOME.create(FontAwesome.Glyph.SIGN_IN).color(DEFAULT_ICON_COLOR);
    }


    /**
     * @return the iconExport
     */
    public static Node createIconExport() {
        return FONT_AWESOME.create(FontAwesome.Glyph.SIGN_OUT).color(DEFAULT_ICON_COLOR);
    }


    /**
     * @return the iconExit
     */
    public static Node createIconExit() {
        return FONT_AWESOME.create(FontAwesome.Glyph.POWER_OFF).color(DEFAULT_ICON_COLOR);
    }


    /**
     * @return the iconUndo
     */
    public static Node createIconUndo() {
        return FONT_AWESOME.create(FontAwesome.Glyph.UNDO).color(DEFAULT_ICON_COLOR);
    }


    /**
     * @return the iconRedo
     */
    public static Node createIconRedo() {
        return FONT_AWESOME.create(FontAwesome.Glyph.REPEAT).color(DEFAULT_ICON_COLOR);
    }


    /**
     * @return the iconDelete
     */
    public static Node createIconDelete() {
        return FONT_AWESOME.create(FontAwesome.Glyph.TRASH_ALT).color(ICON_COLOR_DIMGRAY);
    }


    /**
     * @return the iconQuestion
     */
    public static Node createIconQuestion() {
        return FONT_AWESOME.create(FontAwesome.Glyph.QUESTION).color(DEFAULT_ICON_COLOR);
    }


    /**
     * @return the iconFilterList
     */
    public static Node createIconFilterList() {
        return FONT_AWESOME.create(FontAwesome.Glyph.BAN).color(ICON_COLOR_RED);
    }


    /**
     * @return the iconWatchList
     */
    public static Node createIconWatchList() {
        return FONT_AWESOME.create(FontAwesome.Glyph.EYE).color(Color.DARKGREEN);
    }


    /**
     * @return the iconRemove
     */
    public static Node createIconRemove() {
        return FONT_AWESOME.create(FontAwesome.Glyph.REMOVE).color(DEFAULT_ICON_COLOR);
    }


    /**
     * @return the iconRemove
     */
    public static Node createIconCancel() {
        return FONT_AWESOME.create(FontAwesome.Glyph.STOP).color(ICON_COLOR_RED);
    }


    /**
     * @return the iconRemove
     */
    public static Node createIconEdit() {
        return FONT_AWESOME.create(FontAwesome.Glyph.EDIT).color(ICON_COLOR_DIMGRAY);
    }
}
