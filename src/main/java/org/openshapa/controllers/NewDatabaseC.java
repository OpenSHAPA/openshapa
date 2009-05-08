package org.openshapa.controllers;

import org.openshapa.OpenSHAPA;
import org.openshapa.views.NewDatabaseV;
import javax.swing.JFrame;

/**
 * Controller for creating new databases.
 *
 * @author switcher (logic of controller - pulled from spreadsheet panel.)
 */
public class NewDatabaseC {

    /**
     * Constructor, creates the New Database controller.
     */
    public NewDatabaseC() {
        // Create the view, register this controller with it and display it.
        JFrame mainFrame = OpenSHAPA.getApplication().getMainFrame();
        NewDatabaseV view = new NewDatabaseV(mainFrame, false);
        OpenSHAPA.getApplication().show(view);
    }
}