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

import org.datavyu.Datavyu;
import org.datavyu.views.VocabEditorV;

import javax.swing.*;

/**
 * A controller for invoking the vocab editor.
 */
public final class VocabEditorController {

    private static VocabEditorController controller = null; //singleton
    private VocabEditorV view;
    
    /**
     * Constructor.
     */
    private VocabEditorController() {}
        
    public static VocabEditorController getController() {
        if (controller == null) controller = new VocabEditorController();
        return controller;
    }
    
    public void showView() {
        //If we don't already have a view create the view with mainFrame as parent
        if (view == null) {
            JFrame mainFrame = Datavyu.getApplication().getMainFrame();
            view = new VocabEditorV(mainFrame, false);
        }
        Datavyu.getApplication().show(view); // display view
    }

    public void updateView() {
        JFrame mainFrame = Datavyu.getApplication().getMainFrame();
        view = new VocabEditorV(mainFrame, false);
        if (view.isShowing()) {
            view.closeWindow();
            Datavyu.getApplication().show(view);
        }
    }

    public void killView()
    {
        view = null;
    }
}
