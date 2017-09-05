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
import org.datavyu.models.db.DataStore;
import org.datavyu.models.project.Project;
import org.datavyu.models.project.ViewerSetting;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Controller for opening Datavyu databases and project files.
 */
public final class OpenController {

    /** The logger instance for this class */
    private static Logger logger = LogManager.getLogger(OpenController.class);

    /** Reference to the data base that this controller opened */
    private DataStore database = null;

    /** Reference to the project that this controller opened */
    private Project project = null;

    /**
     * Opens a file as a Datavyu database.
     *
     * @param databaseFile The file to use when opening a file as a database.
     */
    public void openDataStore(final File databaseFile) {
        OpenDataStoreFileController odc = new OpenDataStoreFileController();
        database = odc.open(databaseFile);
        database.deselectAll();
    }

    /**
     * Opens a file as a Datavyu project.
     *
     * @param projectFile The file to use when opening a file as a project.
     */
    public void openProject(final File projectFile) {

        // If project is archive - open it as such.
        if (projectFile.getName().endsWith(".opf")) {
            logger.info("open project archive");
            openProjectArchive(projectFile);

            // Otherwise project is uncompressed.
        } else {
            logger.info("open legacy shapa");

            OpenProjectFileController opc = new OpenProjectFileController();
            project = opc.open(projectFile);

            if (project != null) {
                OpenDataStoreFileController odc = new OpenDataStoreFileController();
                database = odc.open(new File(projectFile.getParent(),
                        project.getDatabaseFileName()));
            }
        }
        database.setName(projectFile.getName());

        database.deselectAll();

        // Mark as unchanged after loading spreadsheet
        database.markAsUnchanged();
    }

    /**
     * Opens a file as a Datavyu archive.
     *
     * @param archiveFile The archive to open as a project.
     */
    private void openProjectArchive(final File archiveFile) {

        try {
            ZipFile zf = new ZipFile(archiveFile);

            String arch = archiveFile.getName().substring(0,
                    archiveFile.getName().lastIndexOf('.'));
            ZipEntry zProj = zf.getEntry("project");

            // BugzID:1941 - Older project files are nested within a directory.
            // Try in the nested location if unable to find a project.
            if (zProj == null) {
                zProj = zf.getEntry(arch + File.separator + "project");
            }

            OpenProjectFileController opc = new OpenProjectFileController();
            project = opc.open(zf.getInputStream(zProj));

            ZipEntry zDb = zf.getEntry("db");

            // BugzID:1941 - Older database files are nested within a directory
            // Try in the nested location if unable to find a project.
            if (zDb == null) {
                zDb = zf.getEntry(arch + File.separator + "db");
            }

            OpenDataStoreFileController odc = new OpenDataStoreFileController();
            database = odc.openAsCsv(zf.getInputStream(zDb));

            // BugzID:1806
            for (ViewerSetting vs : project.getViewerSettings()) {

                if (vs.getSettingsId() != null) {
                    ZipEntry entry = zf.getEntry(vs.getSettingsId());
                    vs.copySettings(zf.getInputStream(entry));
                }
            }

            zf.close();
        } catch (Exception e) {
            logger.error("Unable to open project archive", e);
        }

        database.deselectAll();
    }

    /**
     * @return The instance of the data store that was opened by this controller, returns null if no database opened.
     */
    public DataStore getDataStore() {
        return database;
    }

    /**
     * @return The instance of the project
     */
    public Project getProject() {
        return project;
    }
}
