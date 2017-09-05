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
package org.datavyu.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.controllers.project.DatavyuProjectConstructor;
import org.datavyu.models.project.Project;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;

/**
 * Controller for opening and loading Datavyu project files that are on disk.
 */
public final class OpenProjectFileController {

    /** The logger instance for this class */
    private static Logger logger = LogManager.getLogger(OpenProjectFileController.class);

    /**
     * Opens and loads a project file from disk.
     *
     * @param projectFile The project file to open and load, absolute path
     * @return valid project if file was opened and loaded, null otherwise.
     */
    public Project open(final File projectFile) {
        try {
            FileInputStream fileInputStream = new FileInputStream(projectFile);
            Project project = open(fileInputStream);
            fileInputStream.close();
            return project;
        } catch (FileNotFoundException e) {
            logger.info("Could not find file: '" + projectFile.getAbsolutePath() + "'. Error: " + e);
        } catch (IOException e) {
            logger.info("Could not parse file: '" + projectFile.getAbsolutePath() + "'. Error: " + e);
        }
        return null;
    }

    /**
     * Opens and loads a project file from a stream. The caller is responsible
     * for managing the stream.
     *
     * @param inputStream The stream to deserialize and load
     * @return valid project if stream was deserialized, null otherwise.
     */
    public Project open(final InputStream inputStream) {
        DumperOptions options = new DumperOptions();
        options.setAllowUnicode(false); // Allow for the encoding of foreign characters by using escape characters
        Yaml yaml = new Yaml(new DatavyuProjectConstructor(), new Representer(), options);
        Object o = yaml.load(inputStream);

        // Make sure the de-serialised object is a project file
        if (!(o instanceof Project)) {
            logger.error("Not a Datavyu project file");
            return null;
        }

        return (Project) o;
    }
}
