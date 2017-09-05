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
import org.datavyu.models.db.Cell;
import org.datavyu.undoableedits.ChangeCellEdit;
import org.datavyu.undoableedits.ChangeOffsetCellEdit;

import javax.swing.undo.UndoableEdit;

/**
 * Controller for setting the stop time (offset) of a new cell.
 */
public final class SetNewCellStopTimeController {
    // TODO: This should be refactored probably SetNewCellCurrentTimeController (we refactored offset to current time)

    /** The logger instance for this class */
    private static Logger logger = LogManager.getLogger(SetNewCellStopTimeController.class);

    /**
     * Sets the stop time of the last cell that was created.
     *
     * @param milliseconds The number of milliseconds since the origin of the spread sheet to set the stop time for.
     */
    public SetNewCellStopTimeController(final long milliseconds) {
        logger.info("Set new cell offset");

        Cell cell = Datavyu.getProjectController().getLastCreatedCell();
        // record the effect
        UndoableEdit edit = new ChangeOffsetCellEdit(cell, cell.getOffset(), milliseconds,
                ChangeCellEdit.Granularity.FINEGRAINED);
        Datavyu.getView().getUndoSupport().postEdit(edit);
        cell.setOffset(milliseconds);
    }
}
