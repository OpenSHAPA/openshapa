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
import org.datavyu.views.NewVariableV;

import javax.swing.*;

/**
 * Controller for creating new variables.
 */
public class NewVariableController {

    /** The logger instance for this class */
    private static Logger logger = LogManager.getLogger(NewVariableController.class);

    /**
     * Constructor, creates the new variable controller.
     */
    public NewVariableController() {
        // Create the view, and display it.
        JFrame mainFrame = Datavyu.getApplication().getMainFrame();
        NewVariableV view = new NewVariableV(mainFrame, false);
        Datavyu.getApplication().show(view);
        logger.info("Created a controller for variable: " + view.getVariableName());
    }
}
