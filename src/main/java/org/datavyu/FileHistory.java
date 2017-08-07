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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Remembers, saves, and loads lists of recent files for Datavyu's history functionality in the menus.
 */
public class FileHistory {

    /** Maximum number of files per list */
    private static final int NUM_MAX_HISTORY = 5;

    /** Name of the history file to load from */
    private static final String fileName = "FileHistory.yml";

    /** Logger for this class */
    private static Logger logger = LogManager.getLogger(FileHistory.class);

    /** The history file to read and write to */
    private static final File historyFile = new File(
            Datavyu.getApplication().getContext().getLocalStorage().getDirectory(), fileName);;

    /** List of recently opened projects */
    private static List<File> projects = new LinkedList<>();

    /** List of recently opened scripts */
    private static List<File> scripts = new LinkedList<>();

    /** Load the history file to this static context */
    static {
        logger.info("Looking for history file: " + historyFile.getAbsolutePath());
        if (historyFile.exists()) {
            load();
        }
    }

    /**
     * Remember the given file in the list of projects.
     *
     * @param file The given project file.
     */
    public static void rememberProject(final File file) {
        remember(projects, file);
        save();
    }

    /**
     * Remember the given file in the list of scripts.
     *
     * @param file The given script file.
     */
    public static void rememberScript(final File file) {
        remember(scripts, file);
        save();
    }

    /**
     * Remember the file in the history.
     *
     * @param history The history of files.
     * @param file The file to remember.
     */
    private static void remember(final List<File> history, final File file) {
        if (history.contains(file)) {
            history.remove(file);
        }
        if (history.size() == NUM_MAX_HISTORY) {
            history.remove(NUM_MAX_HISTORY - 1);
        }
        history.add(0, file);
    }

    /**
     * Recently opened projects. The most recent is first.
     */
    public static Iterable<File> getRecentProjects() { return projects; }

    /**
     * Recently opened scripts. The most recent is first.
     */
    public static Iterable<File> getRecentScripts() {
        return scripts;
    }

    /**
     * Save file history to disk in YAML format.
     */
    private static void save() {
        Map<String, List<String>> historyMap = Maps.newHashMap();
        historyMap.put("projects", filesToPaths(projects));
        historyMap.put("scripts", filesToPaths(scripts));

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);
        Writer writer = null;

        try {
            writer = new FileWriter(historyFile);
            yaml.dump(historyMap, writer);
        } catch (IOException e) {
            logger.error("Couldn't save history", e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }


    /**
     * Load history from disk in yaml format.
     */
    private static void load() {
        Yaml yaml = new Yaml();
        Reader reader = null;

        try {
            reader = new FileReader(historyFile);

            Map data = (Map) yaml.load(reader);

            List<String> projectPaths = (List) data.get("projects");
            projects = pathsToFiles(projectPaths);

            List<String> scriptPaths = (List) data.get("scripts");
            scripts = pathsToFiles(scriptPaths);

        } catch (FileNotFoundException e) {
            // Function is only called if the file exists.
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Get absolute paths for a listing of files.
     *
     * @param listing The listing of files.
     *
     * @return A listing of the files as list of String objects.
     */
    private static List<String> filesToPaths(final List<File> listing) {
        List<String> paths = Lists.newLinkedList();
        for (File file : listing) {
            paths.add(file.getAbsolutePath());
        }
        return paths;
    }

    /**
     * A listing of files as string list.
     *
     * @param listing The listing of files.
     *
     * @return The listing of files as list of File objects.
     */
    private static List<File> pathsToFiles(final List<String> listing) {
        List<File> files = Lists.newLinkedList();
        for (String path : listing) {
            files.add(new File(path));
        }
        return files;
    }
}
