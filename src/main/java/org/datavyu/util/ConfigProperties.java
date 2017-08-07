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

import java.awt.*;
import java.io.Serializable;

/**
 * The properties to use for configuration. Access through via org.datavyu.Configuration.
 */
public final class ConfigProperties implements Serializable {

    /** Unique identifier for this serial version */
    private static final long serialVersionUID = 4L;

    /** Spread sheet data font */
    private Font spreadSheetDataFont;

    /** Spread sheet label font */
    private Font spreadSheetLabelFont;

    /** Spread sheet background color */
    private Color spreadSheetBackgroundColor;

    /** Spreadsheet foreground color */
    private Color spreadSheetForegroundColor;

    /** Foreground color for the spreadsheet ordinal */
    private Color spreadSheetOrdinalForegroundColor;

    /** Foreground color of for spreadsheet timestamp */
    private Color spreadSheetTimeStampForegroundColor;

    /** Spreadsheet selection color */
    private Color spreadSheetSelectedColor;

    /** Spreadsheet overlap color */
    private Color spreadSheetOverlapColor;

    /** Last chosen directory */
    private String lastChosenDirectory;

    /** Color of a carriage in the unselected state */
    private Color mixerUnselectedCarriageColor;

    /** Outline color of a carriage in the unselected/normal state */
    private Color mixerUnselectedOutlineColor;

    /** Fill colour of a carriage in the selected state */
    private Color mixerSelectedCarriageColor;

    /** Outline colour of a carriage in the selected state */
    private Color mixerSelectedOutlineColor;

    /** Version number to ignore for update reminders */
    private String ignoreVersion;
    
    /** True if column name warnings should be displayed */
    private boolean doWarnOnColumnNames = true;

    /** True if pre releases are preferred */
    private boolean usePreRelease;
    
    private String favoritesFolder = "favorites";

    /**
     * Default constructor.
     */
    public ConfigProperties() {}

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
    public void setSpreadSheetBackgroundColour(final Color color) {
        spreadSheetBackgroundColor = color;
    }

    /**
     * @return The spreadsheet foreground colour.
     */
    public Color getSpreadSheetForegroundColour() {
        return spreadSheetForegroundColor;
    }

    /**
     * Sets the spreadsheet foreground colour.
     *
     * @param color The new colour to use for the spreadsheet foreground.
     */
    public void setSpreadSheetForegroundColour(final Color color) {
        spreadSheetForegroundColor = color;
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
    public void setSpreadSheetOrdinalForegroundColour(final Color color) {
        spreadSheetOrdinalForegroundColor = color;
    }

    /**
     * Sets the spreadsheet timestamp foreground colour.
     *
     * @param color The new colour to use for the spreadsheet foreground.
     */
    public void setSpreadSheetTimeStampForegroundColor(final Color color) {
        spreadSheetTimeStampForegroundColor = color;
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
    public void setSpreadSheetOverlapColour(final Color color) {
        spreadSheetOverlapColor = color;
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

    /**
     * @return the mixerUnselectedCarriageColor
     */
    public Color getMixerUnselectedCarriageColor() {
        return mixerUnselectedCarriageColor;
    }

    /**
     * @param color the mixerUnselectedCarriageColor to set
     */
    public void setMixerUnselectedCarriageColor(final Color color) {
        mixerUnselectedCarriageColor = color;
    }

    /**
     * @return the mixerUnselectedOutlineColor
     */
    public Color getMixerUnselectedOutlineColor() {
        return mixerUnselectedOutlineColor;
    }

    /**
     * @param color the mixerUnselectedOutlineColor to set
     */
    public void setMixerUnselectedOutlineColor(final Color color) {
        mixerUnselectedOutlineColor = color;
    }

    /**
     * @return the mixerSelectedCarriageColor
     */
    public Color getMixerSelectedCarriageColor() {
        return mixerSelectedCarriageColor;
    }

    /**
     * @param color the mixerSelectedCarriageColor to set
     */
    public void setMixerSelectedCarriageColor(final Color color) {
        mixerSelectedCarriageColor = color;
    }

    /**
     * @return the mixerSelectedOutlineColor
     */
    public Color getMixerSelectedOutlineColor() {
        return mixerSelectedOutlineColor;
    }

    /**
     * @param color the mixerSelectedOutlineColor to set
     */
    public void setMixerSelectedOutlineColor(final Color color) {
        mixerSelectedOutlineColor = color;
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
    public void setDoWarnOnColumnNames(final boolean doWarn)
    {
        doWarnOnColumnNames = doWarn;
    }

    /**
     * @return the prerelease preference
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
    
    public String getFavoritesFolder(){
        return favoritesFolder;
    }
    
    public void setFavoritesFolder(String pathName){
        favoritesFolder = pathName;
    }
}
