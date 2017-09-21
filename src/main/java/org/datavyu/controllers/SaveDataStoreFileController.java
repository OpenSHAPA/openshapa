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
import org.datavyu.models.db.*;
import org.datavyu.util.StringUtils;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

import java.io.*;


/**
 * Controller for saving the database to disk.
 */
public final class SaveDataStoreFileController {

    /** The logger instance for this class */
    private static Logger logger = LogManager.getLogger(SaveDataStoreFileController.class);

    /**
     * Saves the database to the specified destination, if the file ends with .csv, the data store is saved as CSV.
     *
     * @param destinationFile The destination to save the database too.
     * @param dataStore The data store to save to disk.
     * @throws UserWarningException If unable to save the database to the desired location.
     */
    protected void saveDataStore(final File destinationFile, final DataStore dataStore) throws UserWarningException {

        // We bypass any overwrite checks here.
        String outputFile = destinationFile.getName().toLowerCase();
        String extension = outputFile.substring(outputFile.lastIndexOf('.'), outputFile.length());

        if (extension.equals(".csv")) {
            saveAsCsv(destinationFile.toString(), dataStore);
        }
    }

    /**
     * Serialize the database to the specified stream in a CSV format.
     *
     * @param outStream The stream to use when serializing.
     * @param dataStore The data store to save as a CSV file.
     * @throws UserWarningException When unable to save the database as a CSV to
     *                              disk (usually because of permissions errors).
     */
    public void saveAsCsv(final OutputStream outStream, final DataStore dataStore) throws UserWarningException {
        logger.info("Save data store as CSV to stream");

        PrintStream ps = new PrintStream(outStream);
        ps.println("#4");  // Write an identifier for the version of file

        for (Variable variable : dataStore.getAllVariables()) {
            ps.printf("%s (%s,%s,%s)",
                    StringUtils.escapeCSV(variable.getName()),
                    variable.getRootNode().type,
                    !variable.isHidden(),
                    "");

            if (variable.getRootNode().type == Argument.Type.MATRIX) {
                ps.print('-');

                int numArgs = 0;
                for (Argument arg : variable.getRootNode().childArguments) {
                    ps.printf("%s|%s",
                            StringUtils.escapeCSV(arg.name),
                            arg.type);

                    if (numArgs < (variable.getRootNode().childArguments.size() - 1)) {
                        ps.print(',');
                    }
                    numArgs++;
                }
            }

            ps.println();

            for (Cell cell : variable.getCells()) {
                ps.printf("%s,%s,%s",
                        cell.getOnsetString(),
                        cell.getOffsetString(),
                        cell.getCellValue().serialize());
                ps.println();
            }
        }
    }

    /**
     * Saves the database to the specified destination in a CSV format.
     *
     * @param outFile The path of the file to use when writing to disk.
     * @param dataStore      The datastore to save as a CSV file.
     * @throws UserWarningException When unable to save the database as a CSV to
     *                              disk (usually because of permissions errors).
     */
    public void saveAsCsv(final String outFile, final DataStore dataStore) throws UserWarningException {

        try {
            FileOutputStream fos = new FileOutputStream(outFile);
            saveAsCsv(fos, dataStore);
            fos.close();
        } catch (IOException ie) {
            ResourceMap rMap = Application.getInstance(Datavyu.class)
                    .getContext().getResourceMap(Datavyu.class);
            throw new UserWarningException(rMap.getString("UnableToSave.message", outFile), ie);
        }
    }
}
