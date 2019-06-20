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
 */
public class Icons {

    private static final Color DEFAULT_ICON_COLOR = Color.DARKGRAY;
    private static final Color ICON_COLOR_RED = Color.DARKRED;
    private static final Color ICON_COLOR_DIMGRAY = Color.DIMGREY;
    private static final GlyphFont FONT_AWESOME = GlyphFontRegistry.font("FontAwesome");


    public static Node createIconFileText() {
        return FONT_AWESOME.create(FontAwesome.Glyph.FILE_TEXT_ALT).color(DEFAULT_ICON_COLOR);
    }

    public static Node createIconFile() {
        return FONT_AWESOME.create(FontAwesome.Glyph.FILE_ALT).color(DEFAULT_ICON_COLOR);
    }

    public static Node createIconFolderOpen() {
        return FONT_AWESOME.create(FontAwesome.Glyph.FOLDER_OPEN_ALT).color(DEFAULT_ICON_COLOR);
    }

    public static Node createIconSave() {
        return FONT_AWESOME.create(FontAwesome.Glyph.SAVE).color(DEFAULT_ICON_COLOR);
    }

    public static Node createIconImport() {
        return FONT_AWESOME.create(FontAwesome.Glyph.SIGN_IN).color(DEFAULT_ICON_COLOR);
    }

    public static Node createIconExport() {
        return FONT_AWESOME.create(FontAwesome.Glyph.SIGN_OUT).color(DEFAULT_ICON_COLOR);
    }

    public static Node createIconExit() {
        return FONT_AWESOME.create(FontAwesome.Glyph.POWER_OFF).color(DEFAULT_ICON_COLOR);
    }

    public static Node createIconUndo() {
        return FONT_AWESOME.create(FontAwesome.Glyph.UNDO).color(DEFAULT_ICON_COLOR);
    }

    public static Node createIconRedo() {
        return FONT_AWESOME.create(FontAwesome.Glyph.REPEAT).color(DEFAULT_ICON_COLOR);
    }

    public static Node createIconDelete() {
        return FONT_AWESOME.create(FontAwesome.Glyph.TRASH_ALT).color(ICON_COLOR_DIMGRAY);
    }

    public static Node createIconQuestion() {
        return FONT_AWESOME.create(FontAwesome.Glyph.QUESTION).color(DEFAULT_ICON_COLOR);
    }

    public static Node createIconFilterList() {
        return FONT_AWESOME.create(FontAwesome.Glyph.BAN).color(ICON_COLOR_RED);
    }

    public static Node createIconWatchList() {
        return FONT_AWESOME.create(FontAwesome.Glyph.EYE).color(Color.DARKGREEN);
    }

    public static Node createIconRemove() {
        return FONT_AWESOME.create(FontAwesome.Glyph.REMOVE).color(DEFAULT_ICON_COLOR);
    }

    public static Node createIconEdit() {
        return FONT_AWESOME.create(FontAwesome.Glyph.EDIT).color(ICON_COLOR_DIMGRAY);
    }
}
