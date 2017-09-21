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
import org.datavyu.util.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Remembers, saves, and loads lists of recent files for project files and script files used by Datavyu.
 */
public final class FileHistory {

    /** Logger for this class */
    private static Logger logger = LogManager.getLogger(FileHistory.class);

    /** List of recently opened projects */
    private static RecentFiles projects;

    /** List of recently opened scripts */
    private static RecentFiles scripts;

    // Loads the histories to the static context
    static {
        try {
            projects = RecentFiles.load(Constants.PROJECT_FILE_HISTORY);
        } catch (IOException io) {
            logger.error("Could not find history for project files. Error: ", io);
        }
        if (projects == null) {
            logger.info( "Could not load existing history for project files.");
            projects = new RecentFiles();
        }
        try {
            scripts = RecentFiles.load(Constants.SCRIPT_FILE_HISTORY);
        } catch (IOException io) {
            logger.error("Could not find history for script files. Error: ", io);
        }
        if (scripts == null) {
            logger.info("Could not load existing history for script files.");
            scripts = new RecentFiles();
        }
    }

    /**
     * Remember the given file in the list of projects.
     *
     * @param file The given project file.
     */
    public static void rememberProject(final File file) {
        projects.remember(file.getAbsolutePath());
        try {
            RecentFiles.save(projects, Constants.PROJECT_FILE_HISTORY);
        } catch (IOException io) {
            logger.error("Could not save project file history. Error: ", io);
        }
    }

    /**
     * Remember the given file in the list of scripts.
     *
     * @param file The given script file.
     */
    public static void rememberScript(final File file) {
        scripts.remember(file.getAbsolutePath());
        try {
            RecentFiles.save(scripts, Constants.SCRIPT_FILE_HISTORY);
        } catch (IOException io) {
            logger.error("Could not save script file history. Error: ", io);
        }
    }

    /**
     * Recently opened projects. The most recent is first.
     */
    public static Iterable<File> getRecentProjects() {
        return stringListToFileList(projects.getRecentFiles());
    }

    /**
     * Recently opened scripts. The most recent is first.
     */
    public static Iterable<File> getRecentScripts() {
        return stringListToFileList(scripts.getRecentFiles());
    }

    /**
     * Converts a list of Strings into a list of Files.
     *
     * @param listing The list of Strings.
     *
     * @return The list of Files.
     */
    private static List<File> stringListToFileList(List<String> listing) {
        List<File> files = new ArrayList<>();
        for (String projectName : listing) {
            files.add(new File(projectName));
        }
        return files;
    }
}
