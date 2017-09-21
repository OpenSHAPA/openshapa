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
import org.jdesktop.application.LocalStorage;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * A class that maintains N most recently remembered files.
 */
public final class RecentFiles implements Serializable{

    /** Logger for this class */
    private static Logger logger = LogManager.getLogger(RecentFiles.class);

    /** Limit for the file listing */
    private int limit;

    /** A listing of the N most recent files */
    private List<String> recentFiles;

    /** Maximum number of files per list */
    private static final int NUM_MAX_HISTORY = 5;

    /**
     * Create recent files.
     *
     * @param limit The limit for the recent files.
     */
    public RecentFiles(int limit) {
        this.limit = limit;
        this.recentFiles = new LinkedList<>();
    }

    public RecentFiles() {
        this(NUM_MAX_HISTORY);
    }

    /**
     * Remember the file as most recent one among the others.
     *
     * @param file The file.
     */
    public void remember(String file) {
        logger.info("Remember file: " + file);
        // possibly remove the file if it exists (must be most recent)
        recentFiles.remove(file);
        // add the file at the first place
        recentFiles.add(0, file);
        // If we exceed the limit remove the last entry
        if (recentFiles.size() == limit) {
            recentFiles.remove(limit-1);
        }
    }

    /**
     * Get the list of most recent files.
     *
     * @return A list of most recent files.
     */
    public List<String> getRecentFiles() {
        return recentFiles;
    }

    /**
     * Set a list of most recently remembered files.
     *
     * @param recentFiles
     */
    @SuppressWarnings("unused")  // Used by the reflection to restore this class
    public void setRecentFiles(List<String> recentFiles) {
        this.recentFiles = recentFiles;
    }

    /**
     * Get the limit that is used for maintaining the n most recently remembered files.
     * @return
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Set the limit for the n most recently remembered files.
     *
     * @param limit The limit.
     */
    @SuppressWarnings("unused")  // Used by the reflection to restore this class
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * Load recently remembered files.
     *
     * @param fileName The file name where to load from.
     *
     * @return Loaded instance of recent files.
     *
     * @throws IOException Exception while loading from file.
     */
    public static RecentFiles load(String fileName) throws IOException {
        LocalStorage localStorage = Datavyu.getApplication().getContext().getLocalStorage();
        logger.info("Loading recent files from: "
                + localStorage.getDirectory().getAbsolutePath() + File.separator + fileName);
        return (RecentFiles) localStorage.load(fileName);
    }

    /**
     * Save the most recently remembered files.
     *
     * @param recentFiles A most recently remembered files instance.
     *
     * @param fileName The file name where to save them.
     *
     * @throws IOException Exception while saving to file.
     */
    public static void save(RecentFiles recentFiles, String fileName) throws IOException {
        LocalStorage localStorage = Datavyu.getApplication().getContext().getLocalStorage();
        logger.info("Saving recent files to: "
                + localStorage.getDirectory().getAbsolutePath() + File.separator + fileName);
        localStorage.save(recentFiles, fileName);
    }
}
