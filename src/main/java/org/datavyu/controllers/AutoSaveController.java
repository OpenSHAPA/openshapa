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

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.Datavyu;
import org.datavyu.controllers.project.ProjectController;
import org.datavyu.models.db.UserWarningException;
import org.datavyu.util.FileFilters.OpfFilter;
import org.datavyu.util.FileFilters.ShapaFilter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class AutoSaveController implements ActionListener {

    /** Logger for this class */
    private static Logger logger = LogManager.getLogger(DeleteColumnController.class);

    /** Timer for this class */
    private static Timer timer;

    /** File to auto save */
    private static File file;

    private AutoSaveController() {}

    public static void setInterval(int interval) {
        if (interval == 0) {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
            return;
        }
        interval *= 60000;
        if (timer == null) {
            timer = new Timer(interval, new AutoSaveController());
            timer.start();
        } else {
            timer.setDelay(interval);
        }
    }

    public static void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        String baseName;
        String ext;
        try {
            if (file != null) {
                file.delete();
            }
            ProjectController projController = Datavyu.getProjectController();
            SaveController saveController = new SaveController();
            if (projController.isNewProject() || (projController.getProjectName() == null)) {
                baseName = "~noname_";
                ext = ".opf";
                file = File.createTempFile(baseName, ext);
                saveController.saveProject(file, projController.getProject(),
                        projController.getDataStore(), false);
            } else {
                if ((projController.getLastSaveOption() instanceof ShapaFilter)
                        || (projController.getLastSaveOption()
                        instanceof OpfFilter)) {
                    baseName = "~" + projController.getProjectName() + "_";
                    ext = ".opf";
                    file = File.createTempFile(baseName, ext);
                    saveController.saveProject(file, projController.getProject(),
                            projController.getDataStore(), false);
                    // Save content just as a database.
                } else {
                    String filename = "~" + projController.getDatabaseFileName();
                    baseName = FilenameUtils.getBaseName(filename) + "_";
                    ext = "." + FilenameUtils.getExtension(filename);
                    file = File.createTempFile(baseName, ext);
                    saveController.saveDataStore(file, projController.getDataStore(), false);
                }
            }
        } catch (UserWarningException lee) {
            logger.error("UserWarningException: Unable to autosave.", lee);
        } catch (IOException ioe) {
            logger.error("IOException: Unable to autosave.", ioe);
        } finally {
            file.deleteOnExit();
        }
    }
}
