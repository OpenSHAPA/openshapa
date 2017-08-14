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
package org.datavyu.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.Datavyu;
import org.jdesktop.application.LocalStorage;

import java.awt.*;
import java.io.*;

/**
 * ConfigProperties holds configuration properties that are loaded from a settings.xml file.
 */
public final class ConfigProperties implements Serializable {

    /** Unique identifier for this serial version */
    private static final long serialVersionUID = 4L;

    /** The colour to use for the border */
    public static final Color DEFAULT_BORDER_COLOUR = new Color(175, 175, 175);

    /** Default data font size */
    private static final float DEFAULT_DATA_FONT_SIZE = 14;

    /** Default label font size */
    private static final float DEFAULT_LABEL_FONT_SIZE = 12;

    /** Default font */
    private static final Font DEFAULT_SPREAD_SHEET_DATA_FONT = new Font("Arial", Font.PLAIN, 14);

    /** Spread sheet data font */
    private Font spreadSheetDataFont;

    /** Default font for labels */
    private static final Font DEFAULT_SPREAD_SHEET_LABEL_FONT = new Font("Arial", Font.PLAIN, 12);

    /** Spread sheet label font */
    private Font spreadSheetLabelFont;

    /** Default spreadsheet background color */
    private static final Color DEFAULT_SPREAD_SHEET_BACKGROUND_COLOR = new Color(249, 249, 249);

    /** Spread sheet background color */
    private Color spreadSheetBackgroundColor;

    /** Default spreadsheet foreground color */
    private static final Color DEFAULT_SPREAD_SHEET_FOREGROUND_COLOR = new Color(58, 58, 58);

    /** Spreadsheet foreground color */
    private Color spreadSheetForegroundColor;

    /** Default spreadsheet ordinal foreground colour */
    private static final Color DEFAULT_SPREAD_SHEET_ORDINAL_FOREGROUND_COLOR = new Color(175, 175, 175);

    /** Foreground color for the spreadsheet ordinal */
    private Color spreadSheetOrdinalForegroundColor;

    /** Default spreadsheet time stamp foreground color */
    private static final Color DEFAULT_SPREAD_SHEET_TIME_STAMP_FOREGROUND_COLOR = new Color(90, 90, 90);

    /** Foreground color of for spreadsheet timestamp */
    private Color spreadSheetTimeStampForegroundColor;

    /** Default spreadsheet selected color */
    private static final Color DEFAULT_SPREAD_SHEET_SELECTED_COLOR = new Color(176, 197, 227);

    /** Spreadsheet selection color */
    private Color spreadSheetSelectedColor;

    /** Default spreadsheet overlap color */
    private static final Color DEFAULT_SPREAD_SHEET_OVERLAP_COLOR = Color.RED;

    /** Spreadsheet overlap color */
    private Color spreadSheetOverlapColor;

    /** Default for the last chosen directory */
    private static final String DEFAULT_LAST_CHOSEN_DIRECTORY = System.getProperty("user.home");

    /** Last chosen directory */
    private String lastChosenDirectory;

    private static final String DEFAULT_IGNORE_VERSION = "";

    /** Version number to ignore for update reminders */
    private String ignoreVersion;

    /** Default value for warn on column names */
    private static final boolean DO_WARN_ON_COLUMN_NAMES = true;

    /** True if column name warnings should be displayed */
    private boolean doWarnOnColumnNames;

    /** Default on use of pre release */
    private static final boolean USE_PRE_RELEASE = false;

    /** True if pre releases are preferred */
    private boolean usePreRelease;

    /** Default for favorites folder */
    private static final String DEFAULT_FAVORITES_FOLDER = "favorites";

    /** Favorites folder */
    private String favoritesFolder;

    /** Default for support site url */
    private static final String DEFAULT_SUPPORT_SITE_URL = "http://www.datavyu.org/support";

    /** URL for the support site */
    private String supportSiteUrl;

    /** Default for the user guide url */
    private static final String DEFAULT_USER_GUIDE_URL = "http://www.datavyu.org/user-guide/index.html";

    /** URL for the user guide site */
    private String userGuideUrl;

    /** This is the only instance for the configuration properties that is loaded at start-up */
    private static ConfigProperties configProperties = new ConfigProperties();

    /** The logger for this class */
    private static Logger logger = LogManager.getLogger(ConfigProperties.class);

    static {
        LocalStorage localStorage = Datavyu.getApplication().getContext().getLocalStorage();
        String localDirectory = localStorage.getDirectory().getAbsolutePath();

        // Copy the settings.xml file from the resources to the tmp folder where the Swing Application Framework
        // loads and stores the *.properties and *.xml files for this application with user defined properties.
        try {
            logger.info("Copying " + Constants.CONFIGURATION_FILE + " to " + localDirectory);
            // It is important that the path into the resource with "/"
            InputStream inputStream = configProperties.getClass().getResourceAsStream(
                                                                        "/" + Constants.CONFIGURATION_FILE);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(localDirectory,
                                                                              Constants.CONFIGURATION_FILE));
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream,
                                                                                 Constants.BUFFER_COPY_SIZE);
            int count;
            byte[] data = new byte[Constants.BUFFER_COPY_SIZE];
            while ((count = inputStream.read(data, 0, Constants.BUFFER_COPY_SIZE)) != -1) {
                bufferedOutputStream.write(data, 0, count);
            }
            bufferedOutputStream.close();
            fileOutputStream.close();
            inputStream.close();
        } catch (IOException io) {
            logger.error("Could not copy resource for settings " + io.getMessage());
        }

        // Try to load the configuration
        try {
            logger.info("Configuration loaded from directory " + localDirectory);
            configProperties = (ConfigProperties) localStorage.load(Constants.CONFIGURATION_FILE);
            logger.info("Loaded configuration properties: " + configProperties);
        } catch (IOException io) {
            logger.error("Unable to load configuration file " + io.getMessage());
            logger.info("Setting default properties.");
            configProperties = new ConfigProperties();
        }

        // If values are not set/loaded set their defaults
        if (!configProperties.hasSpreadSheetDataFont()) {
            configProperties.setSpreadSheetDataFont(DEFAULT_SPREAD_SHEET_DATA_FONT);
        }
        if (!configProperties.hasSpreadSheetLabelFont()) {
            configProperties.setSpreadSheetLabelFont(DEFAULT_SPREAD_SHEET_LABEL_FONT);
        }
        if (!configProperties.hasSpreadSheetSelectedColor()) {
            configProperties.setSpreadSheetSelectedColor(DEFAULT_SPREAD_SHEET_SELECTED_COLOR);
        }
        if (!configProperties.hasSpreadSheetOverlapColor()) {
            configProperties.setSpreadSheetOverlapColor(DEFAULT_SPREAD_SHEET_OVERLAP_COLOR);
        }
        if (!configProperties.hasIgnoreVersion()) {
            configProperties.setIgnoreVersion(DEFAULT_IGNORE_VERSION);
        }
        configProperties.setDoWarnOnColumnNames(DO_WARN_ON_COLUMN_NAMES);
        configProperties.setUsePreRelease(USE_PRE_RELEASE);
        if (!configProperties.hasFavoritesFolder()) {
            configProperties.setFavoritesFolder(DEFAULT_FAVORITES_FOLDER);
        }
        if (!configProperties.hasSupportSiteUrl()) {
            configProperties.setSupportSiteUrl(DEFAULT_SUPPORT_SITE_URL);
        }
        if (!configProperties.hasUserGuideUrl()) {
            configProperties.setUserGuideUrl(DEFAULT_USER_GUIDE_URL);
        }
        if (!configProperties.hasSpreadSheetOrdinalForegroundColor()) {
            configProperties.setSpreadSheetOrdinalForegroundColor(DEFAULT_SPREAD_SHEET_ORDINAL_FOREGROUND_COLOR);
        }
        if (!configProperties.hasSpreadSheetTimeStampeForegroundColor()) {
            configProperties.setSpreadSheetTimeStampForegroundColor(DEFAULT_SPREAD_SHEET_TIME_STAMP_FOREGROUND_COLOR);
        }
        if (!configProperties.hasSpreadSheetBackgroundColor()) {
            configProperties.setSpreadSheetBackgroundColor(DEFAULT_SPREAD_SHEET_BACKGROUND_COLOR);
        }
        if (!configProperties.hasSpreadSheetForegroundColor()) {
            configProperties.setSpreadSheetForegroundColor(DEFAULT_SPREAD_SHEET_FOREGROUND_COLOR);
        }
        if (!configProperties.hasLastChosenDirectory()) {
            configProperties.setLastChosenDirectory(DEFAULT_LAST_CHOSEN_DIRECTORY);
        }
        try {
            Font defaultFont = Font.createFont(Font.TRUETYPE_FONT,
                    configProperties.getClass().getResourceAsStream(Constants.DEFAULT_FONT_FILE));
            configProperties.setSpreadSheetDataFont(defaultFont.deriveFont(DEFAULT_DATA_FONT_SIZE));
            configProperties.setSpreadSheetLabelFont(defaultFont.deriveFont(DEFAULT_LABEL_FONT_SIZE));
        } catch (Exception e) {
            logger.error("Error, unable to load font " + Constants.DEFAULT_FONT_FILE + ". The error is " + e);
        }

        // In all cases save this setting for the next run
        save();
    }

    /**
     * Set the default values for all properties.
     *
     * Public to be accessible by the XMLReader for JavaBeans to create an object instance.
     */
    public ConfigProperties() {}

    /**
     * Get the static instance for the configuration.
     *
     * @return
     */
    public static ConfigProperties getInstance() {
        return configProperties;
    }

    /**
     * Saves the configuration properties in local storage of the swing application framework.
     */
    public static void save() {
        try {
            LocalStorage ls = Datavyu.getApplication().getContext().getLocalStorage();
            ls.save(configProperties, Constants.CONFIGURATION_FILE);
        } catch (IOException e) {
            logger.error("Unable to save configuration " + e.getMessage());
        }
    }

    /**
     * @return The spreadsheet data font.
     */
    public Font getSpreadSheetDataFont() {
        return spreadSheetDataFont;
    }

    /**
     * Sets the spreadsheet data font.
     *
     * @param font The new font to use for spreadsheet data.
     */
    public void setSpreadSheetDataFont(final Font font) {
        spreadSheetDataFont = font;
    }

    public void setSpreadSheetDataFontSize(final float size) {
        setSpreadSheetDataFont(getSpreadSheetDataFont().deriveFont(size));
    }

    public boolean hasSpreadSheetDataFont() {
        return spreadSheetDataFont != null;
    }

    /**
     * @return The spreadsheet data font.
     */
    public Font getSpreadSheetLabelFont() {
        return spreadSheetLabelFont;
    }

    /**
     * Sets the spreadsheet data font.
     *
     * @param font The new font to use for spreadsheet data.
     */
    public void setSpreadSheetLabelFont(final Font font) {
        spreadSheetLabelFont = font;
    }

    public boolean hasSpreadSheetLabelFont() {
        return spreadSheetLabelFont != null;
    }

    /**
     * @return The spreadsheet background colour.
     */
    public Color getSpreadSheetBackgroundColour() {
        return spreadSheetBackgroundColor;
    }

    /**
     * Sets the spreadsheet background colour.
     *
     * @param color The new colour to use for the spreadsheet background.
     */
    public void setSpreadSheetBackgroundColor(final Color color) {
        spreadSheetBackgroundColor = color;
    }

    public boolean hasSpreadSheetBackgroundColor() {
        return spreadSheetBackgroundColor != null;
    }

    /**
     * @return The spreadsheet foreground colour.
     */
    public Color getSpreadSheetForegroundColor() {
        return spreadSheetForegroundColor;
    }

    /**
     * Sets the spreadsheet foreground colour.
     *
     * @param color The new colour to use for the spreadsheet foreground.
     */
    public void setSpreadSheetForegroundColor(final Color color) {
        spreadSheetForegroundColor = color;
    }

    public boolean hasSpreadSheetForegroundColor() {
        return spreadSheetForegroundColor != null;
    }

    /**
     * @return The spreadsheet ordinal foreground colour.
     */
    public Color getSpreadSheetOrdinalForegroundColour() {
        return spreadSheetOrdinalForegroundColor;
    }

    /**
     * Sets the spreadsheet ordinal foreground colour.
     *
     * @param color to use for the spreadsheet foreground.
     */
    public void setSpreadSheetOrdinalForegroundColor(final Color color) {
        spreadSheetOrdinalForegroundColor = color;
    }

    public boolean hasSpreadSheetOrdinalForegroundColor() {
        return spreadSheetOrdinalForegroundColor != null;
    }

    /**
     * Sets the spreadsheet timestamp foreground colour.
     *
     * @param color The new colour to use for the spreadsheet foreground.
     */
    public void setSpreadSheetTimeStampForegroundColor(final Color color) {
        spreadSheetTimeStampForegroundColor = color;
    }

    public boolean hasSpreadSheetTimeStampeForegroundColor() {
        return spreadSheetTimeStampForegroundColor != null;
    }

    /**
     * @return The spreadsheet timestamp foreground colour.
     */
    public Color getSpreadSheetTimeStampForegroundColor() {
        return spreadSheetTimeStampForegroundColor;
    }

    /**
     * @return The spreadsheet selections colour.
     */
    public Color getSpreadSheetSelectedColor() {
        return spreadSheetSelectedColor;
    }

    /**
     * Sets the spreadsheet selected colour.
     *
     * @param color The new colour to use for spreadsheet selections.
     */
    public void setSpreadSheetSelectedColor(final Color color) {
        spreadSheetSelectedColor = color;
    }

    public boolean hasSpreadSheetSelectedColor() {
        return spreadSheetSelectedColor != null;
    }

    /**
     * @return The spreadsheet overlap colour.
     */
    public Color getSpreadSheetOverlapColor() {
        return spreadSheetOverlapColor;
    }

    /**
     * Sets the spreadsheet overlap colour.
     *
     * @param color The new colour to use for spreadsheet overlaps.
     */
    public void setSpreadSheetOverlapColor(final Color color) {
        spreadSheetOverlapColor = color;
    }

    public boolean hasSpreadSheetOverlapColor() {
        return spreadSheetOverlapColor != null;
    }

    /**
     * @return The last chooser directory that the user nominated.
     */
    public String getLastChosenDirectory() {
        return lastChosenDirectory;
    }

    /**
     * Sets the last chooser directory that the user nominated.
     *
     * @param directory The last location the user nominated.
     */
    public void setLastChosenDirectory(final String directory) {
        lastChosenDirectory = directory;
    }

    public boolean hasLastChosenDirectory() {
        return lastChosenDirectory != null;
    }

    /**
     * @return the ignoreVersion
     */
    public String getIgnoreVersion() {
        return ignoreVersion;
    }

    /**
     * @param version the version to set
     */
    public void setIgnoreVersion(final String version) {
        ignoreVersion = version;
    }

    public boolean hasIgnoreVersion() {
        return ignoreVersion != null;
    }

    /**
     * @return whether or not to display warnings for illegal column names
     */    
    public boolean getDoWarnOnColumnNames()
    {
        return doWarnOnColumnNames;
    }
    
    /**
     * @param doWarn whether or not to display warnings for illegal column names
     */    
    public void setDoWarnOnColumnNames(final boolean doWarn) {
        doWarnOnColumnNames = doWarn;
    }

    /**
     * @return the pre-release preference
     */
    public boolean getUsePreRelease() {
        return usePreRelease;
    }

    /**
     * @param usePreRelease true if prereleases are preferred
     */
    public void setUsePreRelease(boolean usePreRelease) {
        this.usePreRelease = usePreRelease;
    }

    /**
     * @return favorites folder
     */
    public String getFavoritesFolder(){
        return favoritesFolder;
    }

    /**
     * @param pathName path for the favorites folder
     */
    public void setFavoritesFolder(String pathName){
        favoritesFolder = pathName;
    }

    public boolean hasFavoritesFolder() {
        return favoritesFolder != null;
    }

    public String getSupportSiteUrl() {
        return supportSiteUrl;
    }

    public void setSupportSiteUrl(String supportSiteUrl) {
        this.supportSiteUrl = supportSiteUrl;
    }

    public boolean hasSupportSiteUrl() {
        return supportSiteUrl != null;
    }

    public String getUserGuideUrl() {
        return userGuideUrl;
    }

    public void setUserGuideUrl(String userGuideUrl) {
        this.userGuideUrl = userGuideUrl;
    }

    public boolean hasUserGuideUrl() {
        return userGuideUrl != null;
    }
}
