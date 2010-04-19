package org.openshapa.controllers;

import com.usermetrix.jclient.UserMetrix;
import org.openshapa.OpenSHAPA;
import org.openshapa.views.discrete.SpreadsheetPanel;
import org.openshapa.views.discrete.layouts.SheetLayoutFactory.SheetLayoutType;

/**
 * Controller for setting the spreadsheet layout.
 */
public final class SetSheetLayoutC {

    /** The logger for this class. */
    private UserMetrix logger = UserMetrix.getInstance(SetSheetLayoutC.class);

    /**
     * Constructor - creates and invokes the controller.
     *
     * @param type The layout type to use on the spreadsheet.
     */
    public SetSheetLayoutC(final SheetLayoutType type) {
        logger.usage("setting spreadsheet layout:" + type.toString());
        SpreadsheetPanel view = (SpreadsheetPanel) OpenSHAPA.getApplication()
                                                            .getMainView()
                                                            .getComponent();
        view.setLayoutType(type);
    }
}
