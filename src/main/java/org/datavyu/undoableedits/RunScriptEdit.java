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
package org.datavyu.undoableedits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.controllers.DeleteColumnC;
import org.datavyu.models.db.Cell;
import org.datavyu.models.db.UserWarningException;
import org.datavyu.models.db.Variable;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Undoable script edit.
 */
public class RunScriptEdit extends SpreadSheetEdit {

    /** Logger for this class */
    private static final Logger logger = LogManager.getLogger(RunScriptEdit.class);

    /** Script path */
    private String scriptPath;

    /** Relevant columns */
    private List<VariableTO> colsTO;

    public RunScriptEdit(String scriptPath) {
        super();
        this.scriptPath = scriptPath;
        colsTO = getSpreadSheetState();
    }

    @Override
    public String getPresentationName() {

        return "Run Script \"" + scriptPath + "\"";
    }

    @Override
    public void undo() throws CannotRedoException {
        super.undo();
        toggleSpreadSheetState();
    }

    @Override
    public void redo() throws CannotUndoException {
        super.redo();
        toggleSpreadSheetState();
    }

    private void toggleSpreadSheetState() {
        List<VariableTO> tempColsTO = this.getSpreadSheetState();
        setSpreadSheetState(colsTO);
        colsTO = tempColsTO;
    }

    private List<VariableTO> getSpreadSheetState() {
        List<VariableTO> varsTO = new ArrayList<>();

        int pos = 0;
        for (Variable var : model.getAllVariables()) {
            varsTO.add(new VariableTO(var, pos));
            pos++;
        }

        return varsTO;
    }

    private void setSpreadSheetState(List<VariableTO> varsTO) {
        try {
            HashMap<String, Boolean> hiddenStates = new HashMap<>();
            for (Variable v : model.getAllVariables()) {
                hiddenStates.put(v.getName(), v.isHidden());
            }
            new DeleteColumnC(new ArrayList<>(model.getAllVariables()));

            for (VariableTO varTO : varsTO) {
                Variable var = model.createVariable(varTO.getName(), varTO.getType().type);
                var.setRootNode(varTO.getType());

                for (CellTO cellTO : varTO.getTOCells()) {
                    Cell c = var.createCell();
                    c.setOnset(cellTO.getOnset());
                    c.setOffset(cellTO.getOffset());
                    c.getValue().set(cellTO.getValue());
                }
            }
            for (Variable v : model.getAllVariables()) {
                v.setHidden(hiddenStates.get(v.getName()));
            }
        } catch (UserWarningException e) {
            logger.error("Unable to set spread sheet state. Error: ", e);
        }
        unselectAll();
    }
}
