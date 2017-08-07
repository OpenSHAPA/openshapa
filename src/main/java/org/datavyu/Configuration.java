/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.datavyu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.util.ConfigProperties;
import org.jdesktop.application.LocalStorage;

import java.awt.*;
import java.io.File;
import java.io.IOException;


/**
 * Singleton object containing global configuration definitions for the user interface.
 */
// TODO: Cleanup un-used properties
public final class Configuration {

    /** The colour to use for the border */
    public static final Color BORDER_COLOUR = new Color(175, 175, 175);

    /** The name of the configuration file */
    // TODO: This file does not seem to be anymore present in the project?
    private static final String CONFIG_FILE = "settings.xml";

    /** Default font */
    private static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 14);

    /** Default font for labels */
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 12);

    /** Default data font size */
    private static final float DATA_FONT_SIZE = 14;

    /** Default label font size */
    private static final float LABEL_FONT_SIZE = 12;

    /** Default spreadsheet background color */
    private static final Color DEFAULT_BACKGROUND = new Color(249, 249, 249);

    /** Default spreadsheet foreground color */
    private static final Color DEFAULT_FOREGROUND = new Color(58, 58, 58);

    /** Default spreadsheet ordinal foreground colour */
    private static final Color DEFAULT_ORDINAL = new Color(175, 175, 175);

    /** Default spreadsheet time stamp foreground color */
    private static final Color DEFAULT_TIMESTAMP = new Color(90, 90, 90);

    /** Default spreadsheet selected color */
    private static final Color DEFAULT_SELECTED = new Color(176, 197, 227);

    /** Default spreadsheet overlap color */
    private static final Color DEFAULT_OVERLAP = Color.RED;

    /** Fill color of a carriage in the unselected state */
    private static final Color DEFAULT_UNSELECTED_CARRIAGE_COLOR = new Color(169, 218, 248);

    /** Outline color of a carriage in the unselected/normal state */
    private static final Color DEFAULT_UNSELECTED_OUTLINE_COLOR = new Color(129, 167, 188);

    /** Fill color of a carriage in the selected state */
    private static final Color DEFAULT_SELECTED_CARRIAGE_COLOR = new Color(138, 223, 162);

    /** Outline color of a carriage in the selected state */
    private static final Color DEFAULT_SELECTED_OUTLINE_COLOR = new Color(105, 186, 128);

    /** The configuration properties */
    private ConfigProperties properties;

    /** Instance of the configuration object for Datavyu */
    private static Configuration instance = null;

    /** The logger for this class */
    private static Logger logger = LogManager.getLogger(Configuration.class);

    /** Default font type */
    private Font defaultFont = null;

    /**
     * Default constructor.
     */
    private Configuration() {

        // Loading configuration properties
        try {
            LocalStorage ls = Datavyu.getApplication().getContext().getLocalStorage();
            properties = (ConfigProperties) ls.load(CONFIG_FILE);
            logger.info("Configuration loaded from directory " + ls.getDirectory().getAbsolutePath());
            logger.info("Found properties " + properties);
        } catch (IOException e) {
            logger.error("Unable to load configuration file ", e);
        }

        // Set custom font
        String fontFileName = "/fonts/DejaVuSansCondensed.ttf";

        // Properties not loaded from disk - initialize to default and save
        if (properties == null) {
            logger.info("Setting default properties");
            properties = new ConfigProperties();
            properties.setSpreadSheetDataFont(DEFAULT_FONT);
            properties.setSpreadSheetLabelFont(LABEL_FONT);
            properties.setSpreadSheetSelectedColor(DEFAULT_SELECTED);
            properties.setSpreadSheetOverlapColour(DEFAULT_OVERLAP);
            properties.setMixerUnselectedCarriageColor(DEFAULT_UNSELECTED_CARRIAGE_COLOR);
            properties.setMixerUnselectedOutlineColor(DEFAULT_UNSELECTED_OUTLINE_COLOR);
            properties.setMixerSelectedCarriageColor(DEFAULT_SELECTED_CARRIAGE_COLOR);
            properties.setMixerSelectedOutlineColor(DEFAULT_SELECTED_OUTLINE_COLOR);
            properties.setIgnoreVersion("");
            properties.setDoWarnOnColumnNames(true);
            properties.setUsePreRelease(false);
            save();
        }

        try {
            defaultFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream(fontFileName));
            properties.setSpreadSheetDataFont(defaultFont.deriveFont(DATA_FONT_SIZE));
            properties.setSpreadSheetLabelFont(defaultFont.deriveFont(LABEL_FONT_SIZE));
        } catch (Exception e) {
            logger.error("Error, unable to load font " + fontFileName + ". The error is " + e);
        }

        properties.setSpreadSheetOrdinalForegroundColour(DEFAULT_ORDINAL);
        properties.setSpreadSheetTimeStampForegroundColor(DEFAULT_TIMESTAMP);
        properties.setSpreadSheetBackgroundColour(DEFAULT_BACKGROUND);
        properties.setSpreadSheetForegroundColour(DEFAULT_FOREGROUND);

        if (properties.getLastChosenDirectory() == null) {
            properties.setLastChosenDirectory(System.getProperty("user.home"));
            save();
        }

        // Assume that user wants their selected colour overridden too.
        if (properties.getSpreadSheetOverlapColor() == null) {
            properties.setSpreadSheetSelectedColor(DEFAULT_SELECTED);
            properties.setSpreadSheetOverlapColour(DEFAULT_OVERLAP);
            save();
        }

        // If one property is null, just reset all.
        if (properties.getMixerUnselectedCarriageColor() == null) {
            properties.setMixerUnselectedCarriageColor(DEFAULT_UNSELECTED_CARRIAGE_COLOR);
            properties.setMixerUnselectedOutlineColor(DEFAULT_UNSELECTED_OUTLINE_COLOR);
            properties.setMixerSelectedCarriageColor(DEFAULT_SELECTED_CARRIAGE_COLOR);
            properties.setMixerSelectedOutlineColor(DEFAULT_SELECTED_OUTLINE_COLOR);
            save();
        }
    }

    /**
     * @return The single instance of the Configuration object in Datavyu.
     */
    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    /**
     * @return The data font to use for the spreadsheet.
     */
    public Font getSSDataFont() {
        return properties.getSpreadSheetDataFont();
    }

    /**
     * Sets and saves (to the config file) the data font to use on the
     * spreadsheet.
     *
     * @param font The new data font to use on the spreadsheet.
     */
    public void setSSDataFont(final Font font) {
        properties.setSpreadSheetDataFont(font);
        save();
    }

    /**
     * Changes the data font size.
     *
     * @param size new font size
     */
    public void setSSDataFontSize(final float size) {
        properties.setSpreadSheetDataFont(defaultFont.deriveFont(size));
        save();
    }

    /**
     * @return The data font to use for the spreadsheet.
     */
    public Font getSSLabelFont() {
        return properties.getSpreadSheetLabelFont();
    }

    /**
     * Sets and saves (to the config file) the data font to use on the
     * spreadsheet.
     *
     * @param font The new data font to use on the spreadsheet.
     */
    public void setSSLabelFont(final Font font) {
        properties.setSpreadSheetLabelFont(font);
        save();
    }

    /**
     * @return The background colour for the spreadsheet.
     */
    public Color getSSBackgroundColour() {
        return properties.getSpreadSheetBackgroundColour();
    }

    /**
     * Sets and saves (to the config file) the background colour of the
     * spreadsheet.
     *
     * @param colour The new colour to use for the spreadsheet background.
     */
    public void setSSBackgroundColour(final Color colour) {
        properties.setSpreadSheetBackgroundColour(colour);
        save();
    }

    /**
     * @return The foreground colour of the spreadsheet.
     */
    public Color getSSForegroundColour() {
        return properties.getSpreadSheetForegroundColour();
    }

    /**
     * Sets and saves (to the config file) the foreground colour of the
     * spreadsheet.
     *
     * @param colour The new colour to use for the spreadsheet foreground.
     */
    public void setSSForegroundColour(final Color colour) {
        properties.setSpreadSheetForegroundColour(colour);
        save();
    }

    /**
     * @return The ordinal foreground colour of the spreadsheet.
     */
    public Color getSSOrdinalColour() {
        return properties.getSpreadSheetOrdinalForegroundColour();
    }

    /**
     * Sets and saves (to the config file) the ordinal foreground colour of the
     * spreadsheet.
     *
     * @param colour The new colour to use for the spreadsheet ordinal foreground.
     */
    public void setSSOrdinalColour(final Color colour) {
        properties.setSpreadSheetOrdinalForegroundColour(colour);
        save();
    }

    /**
     * @return The ordinal foreground colour of the spreadsheet.
     */
    public Color getSSTimestampColour() {
        return properties.getSpreadSheetTimeStampForegroundColor();
    }

    /**
     * Sets and saves (to the config file) the ordinal foreground colour of the
     * spreadsheet.
     *
     * @param colour The new colour to use for the spreadsheet ordinal foreground.
     */
    public void setSSTimestampColour(final Color colour) {
        properties.setSpreadSheetTimeStampForegroundColor(colour);
        save();
    }

    /**
     * @return The selected colour of the spreadsheet.
     */
    public Color getSSSelectedColour() {
        return properties.getSpreadSheetSelectedColor();
    }

    /**
     * Sets and saves (to the config file) the selected colour of the
     * spreadsheet.
     *
     * @param colour The new colour to use for spreadsheet selections.
     */
    public void setSSSelectedColour(final Color colour) {
        properties.setSpreadSheetSelectedColor(colour);
        save();
    }

    /**
     * @return The overlap colour of the spreadsheet.
     */
    public Color getSSOverlapColour() {
        return properties.getSpreadSheetOverlapColor();
    }

    /**
     * Sets and saves (to the config file) the overlap colour of the
     * spreadsheet.
     *
     * @param colour The new colour to use for spreadsheet overlaps.
     */
    public void setSSOverlapColour(final Color colour) {
        properties.setSpreadSheetOverlapColour(colour);
        save();
    }

    /**
     * @return The last directory the user navigated too in a file chooser.
     */
    public File getLastChosenDirectory() {
        return new File(properties.getLastChosenDirectory());
    }

    /**
     * Sets and saves (to the config file) the last directory the user navigated
     * too in a chooser.
     *
     * @param location The last location that the user navigated too.
     */
    public void setLastChosenDirectory(final File location) {
        properties.setLastChosenDirectory(location.toString());
        save();
    }

    /**
     * @return the mixerInterfaceSelectedCarriageColour
     */
    public Color getMixerInterfaceSelectedCarriageColour() {
        return properties.getMixerSelectedCarriageColor();
    }

    /**
     * @param newColour the mixerInterfaceSelectedCarriageColour to set
     */
    public void setMixerInterfaceSelectedCarriageColour(final Color newColour) {
        properties.setMixerSelectedCarriageColor(newColour);
        save();
    }

    /**
     * @return the mixerInterfaceSelectedOutlineColour
     */
    public Color getMixerInterfaceSelectedOutlineColour() {
        return properties.getMixerSelectedOutlineColor();
    }

    /**
     * @param newColour the mixerInterfaceSelectedOutlineColour to set
     */
    public void setMixerInterfaceSelectedOutlineColour(final Color newColour) {
        properties.setMixerSelectedOutlineColor(newColour);
        save();
    }

    /**
     * @return the ignoreVersion
     */
    public String getIgnoreVersion() {
        return properties.getIgnoreVersion();
    }

    /**
     * @param version the ignoreVersion to set
     */
    public void setIgnoreVersion(final String version) {
        properties.setIgnoreVersion(version);
        save();
    }
    
    /**
     * @return whether or not to display warnings for illegal column names
     */    
    public boolean getColumnNameWarning()
    {
        return properties.getDoWarnOnColumnNames();
    }
    
    /**
     * @param setWarning whether or not to display warnings for illegal column names
     */    
    public void setColumnNameWarning(final boolean setWarning) {
        properties.setDoWarnOnColumnNames(setWarning);
        save();
    }

    public String getFavouritesFolder() {
        return properties.getFavoritesFolder();
    }
    
    public void setFavouritesFolder(final String path) {
        logger.info("Setting Favourites folder to " + path);
        properties.setFavoritesFolder(path);
        save();
    }

    /**
     * @return the pre-release preference
     */
    public boolean getPrereleasePreference() {
        return properties.getUsePreRelease();
    }

    /**
     * @param preference true if prereleases are preferred
     */
    public void setPrereleasePreference(boolean preference) {
        properties.setUsePreRelease(preference);
        save();
    }

    /**
     * @return the mixerInterfaceNormalCarriageColour
     */
    public Color getMixerInterfaceNormalCarriageColour() {
        return properties.getMixerUnselectedCarriageColor();
    }

    /**
     * @param newColour the mixerInterfaceNormalCarriageColour to set
     */
    public void setMixerInterfaceNormalCarriageColour(final Color newColour) {
        properties.setMixerUnselectedCarriageColor(newColour);
        save();
    }

    /**
     * @return the mixerInterfaceNormalOutlineColour
     */
    public Color getMixerInterfaceNormalOutlineColour() {
        return properties.getMixerUnselectedOutlineColor();
    }

    /**
     * @param newColour the mixerInterfaceNormalOutlineColour to set
     */
    public void setMixerInterfaceNormalOutlineColour(final Color newColour) {
        properties.setMixerUnselectedOutlineColor(newColour);
        save();
    }

    /**
     * Saves the configuration properties in local storage of the swing application framework.
     */
    private void save() {
        try {
            LocalStorage ls = Datavyu.getApplication().getContext().getLocalStorage();
            ls.save(properties, CONFIG_FILE);
        } catch (IOException e) {
            logger.error("Unable to save configuration " + e);
        }
    }
}
