package org.openshapa.util;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;

/**
 * The properties to use for configuration. Access to the configuration
 * properties should be done via org.openshapa.Configuration. This class is a
 * JavaBean so that it can be easily dumped to disk via the XMLEncoder.
 */
public final class ConfigProperties implements Serializable {

    /** The unique ID for this serial version. */
    private static final long serialVersionUID = 1L;

    /** The spreadsheet data font. */
    private Font spreadsheetDataFont;

    /** The spreadsheet background colour. */
    private Color spreadsheetBackgroundColour;

    /** The spreadsheet foreground colour. */
    private Color spreadsheetForegroundColour;

    /** The spreadsheet selection colour. */
    private Color spreadsheetSelectedColour;

    /**
     * Default constructor.
     */
    public ConfigProperties() {
    }

    /**
     * Sets the spreadsheet data font.
     *
     * @param font The new font to use for spreadsheet data.
     */
    public void setSSDataFont(final Font font) {
        spreadsheetDataFont = font;
    }

    /**
     * @return The spreadsheet data font.
     */
    public Font getSSDataFont() {
        return spreadsheetDataFont;
    }

    /**
     * Sets the spreadsheet background colour.
     *
     * @param newColour The new colour to use for the spreadsheet background.
     */
    public void setSSBackgroundColour(final Color newColour) {
        spreadsheetBackgroundColour = newColour;
    }

    /**
     * @return The spreadsheet background colour.
     */
    public Color getSSBackgroundColour() {
        return spreadsheetBackgroundColour;
    }

    /**
     * Sets the spreadsheet foreground colour.
     *
     * @param newColour The new colour to use for the spreadsheet foreground.
     */
    public void setSSForegroundColour(final Color newColour) {
        spreadsheetForegroundColour = newColour;
    }

    /**
     * @return The spreadsheet foreground colour.
     */
    public Color getSSForegroundColour() {
        return spreadsheetForegroundColour;
    }

    /**
     * Sets the spreadsheet selected colour.
     *
     * @param newColour The new colour to use for spreadsheet selections.
     */
    public void setSSSelectedColour(final Color newColour) {
        spreadsheetSelectedColour = newColour;
    }

    /**
     * @return The spreadsheet selections colour.
     */
    public Color getSSSelectedColour() {
        return spreadsheetSelectedColour;
    }
}