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
import org.datavyu.Datavyu;
import org.datavyu.FileHistory;
import org.datavyu.models.db.DataStore;
import org.datavyu.models.db.UserWarningException;
import org.datavyu.models.project.Project;
import org.datavyu.models.project.ViewerSetting;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Master controller for handling project and database file saving logic.
 */
public final class SaveC {

    /**
     * The logger for this class.
     */
    private static Logger logger = LogManager.getLogger(SaveC.class);

    /**
     * Saves only a database to disk.
     *
     * @param databaseFile The location to save the database too.
     * @param dataStore    The dataStore to save to disk.
     * @throws UserWarningException If unable to save the database.
     */
    public void saveDatabase(final String databaseFile,
                             final DataStore dataStore)
            throws UserWarningException {
        this.saveDatabase(new File(databaseFile), dataStore);
    }

    /**
     * Saves only a database to disk.
     *
     * @param databaseFile The location to save the database too.
     * @param dataStore    The dataStore to save to disk.
     * @throws UserWarningException If unable to save the database.
     */
    public void saveDatabase(final File databaseFile,
                             final DataStore dataStore)
            throws UserWarningException {
        saveDatabase(databaseFile, dataStore, true);
    }

    /**
     * Saves only a database to disk.
     *
     * @param databaseFile The location to save the database too.
     * @param dataStore    The dataStore to save to disk.
     * @param remember     Add this project to the rememberProject list.
     * @throws UserWarningException If unable to save the database.
     */
    public void saveDatabase(final File databaseFile,
                             final DataStore dataStore,
                             boolean remember)
            throws UserWarningException {
        logger.info("saving database");

        SaveDatabaseFileC saveDBC = new SaveDatabaseFileC();
        saveDBC.saveDatabase(databaseFile, dataStore);
        if (remember) {
            FileHistory.rememberProject(databaseFile);
        }
    }


    /**
     * Saves an entire project, including database to disk.
     *
     * @param projectFile The destination to save the project too.
     * @param project     The project to save to disk.
     * @param dataStore   The dataStore to save to disk.
     * @throws UserWarningException If unable to save the entire project to
     *                              disk.
     */
    public void saveProject(final File projectFile,
                            final Project project,
                            final DataStore dataStore)
            throws UserWarningException {
        saveProject(projectFile, project, dataStore, true);
    }

    /**
     * Saves an entire project, including database to disk.
     *
     * @param projectFile The destination to save the project too.
     * @param project     The project to save to disk.
     * @param dataStore   The dataStore to save to disk.
     * @param remember    Add this project to the rememberProject list.
     * @throws UserWarningException If unable to save the entire project to
     *                              disk.
     */
    public void saveProject(final File projectFile, final Project project, final DataStore dataStore, boolean remember)
            throws UserWarningException {

        try {
            logger.info("Save project.");

            FileOutputStream fos = new FileOutputStream(projectFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            ZipEntry projectEntry = new ZipEntry("project");
            zos.putNextEntry(projectEntry);
            new SaveProjectFileC().save(zos, project);
            zos.closeEntry();

            ZipEntry dbEntry = new ZipEntry("db");
            zos.putNextEntry(dbEntry);
            new SaveDatabaseFileC().saveAsCSV(zos, dataStore);
            zos.closeEntry();

            // BugzID:1806
            for (ViewerSetting vs : project.getViewerSettings()) {
                ZipEntry vsEntry = new ZipEntry(vs.getSettingsId());
                zos.putNextEntry(vsEntry);
                vs.writeSettings(zos);
            }

            zos.finish();
            zos.close();

            fos.flush();
            fos.close();

            if (remember) {
                FileHistory.rememberProject(projectFile);
            }

        } catch (FileNotFoundException e) {
            ResourceMap rMap = Application.getInstance(Datavyu.class).getContext().getResourceMap(Datavyu.class);
            logger.error("Failed to save project. Error: ", e);
            throw new UserWarningException(rMap.getString("UnableToSave.message", projectFile), e);
        } catch (IOException e) {
            ResourceMap rMap = Application.getInstance(Datavyu.class).getContext().getResourceMap(Datavyu.class);
            logger.error("Failed to save project. Error: ", e);
            throw new UserWarningException(rMap.getString("UnableToSave.message", projectFile), e);
        }
    }
}
