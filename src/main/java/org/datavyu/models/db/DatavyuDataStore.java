/*
 * Copyright (c) 2011 Datavyu Foundation, http://datavyu.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.datavyu.models.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * TODO: Fill in the comment at the ???
 *
 * Acts as a connector between Datavyu's various data structures and a hash map that serves as key value store.
 */
public class DatavyuDataStore implements DataStore {

    /** Logger for the DataStore */
    private static Logger logger = LogManager.getLogger(DatavyuDataStore.class);

    /** The notifier to ping when the application's title changes. */
    private static TitleNotifier titleNotifier = null;

    /** Name of the DataStore - does not need to persist - is used for file names. */
    private String name = "untitled";

    /** Has the DataStore changed since it has last been marked as not changed. */
    private boolean changed;

    /** All listeners of this data store */
    private List<DataStoreListener> dataStoreListeners = new ArrayList<DataStoreListener>();

    /** The variable that this data store holds */
    private Map<String, Variable> variables;

    /** Compare variable with this class instance */
    private VariableComparator VariableComparator = new VariableComparator();
    
    private String exemptionVariables = "";


    public DatavyuDataStore() {
        variables = new HashMap<>();
        changed = false;
    }

    @Override
    public void markAsChanged() {
        if (!changed) {
            changed = true;
            if (DatavyuDataStore.titleNotifier != null) {
                DatavyuDataStore.titleNotifier.updateTitle();
            }
        }
    }

    @Override
    public List<Variable> getAllVariables() {
        List<Variable> allVariables = new ArrayList<Variable>();
        for (String s : variables.keySet()) {
            allVariables.add(variables.get(s));
        }
        Collections.sort(allVariables, VariableComparator);
        return allVariables;
    }

    public List<Variable> getVisibleVariables() {
        List<Variable> allVariables = getAllVariables();
        List<Variable> visibleVariables = new ArrayList<Variable>();
        for (Variable variable : allVariables) {
            if (!variable.isHidden()) {
                visibleVariables.add(variable);
            }
        }
        return visibleVariables;
    }

    @Override
    public List<Variable> getSelectedVariables() {
        List<Variable> selectedVariables = new ArrayList<Variable>();
        for (String s : variables.keySet()) {
            if (variables.get(s).isSelected()) {
                selectedVariables.add(variables.get(s));
            }
        }
        return selectedVariables;
    }

    @Override
    public void clearVariableSelection() {
        for (String s : variables.keySet()) {
            variables.get(s).setSelected(false);
        }
    }

    @Override
    public List<Cell> getSelectedCells() {
        List<Cell> selectedCells = new ArrayList<Cell>();
        for (Variable variable : variables.values()) {
            for (Cell cell : variable.getCells()) {
                if (cell.isSelected()) {
                    selectedCells.add(cell);
                }
            }
        }
        return selectedCells;
    }

    @Override
    public void clearCellSelection() {
        for (Variable variable : variables.values()) {
            for (Cell cell : variable.getCells()) {
                if (cell.isSelected()) {
                    cell.setSelected(false);
                }
                if (cell.isHighlighted()) {
                    cell.setHighlighted(false);
                }
            }
        }
    }

    @Override
    public void deselectAll() {
        this.clearCellSelection();
        this.clearVariableSelection();
    }

    @Override
    public Variable getVariable(String varName) {
        return variables.get(varName);
    }

    @Override
    public Variable getVariable(Cell cell) {
        for (Variable v : variables.values()) {
            if (v.getCells().contains(cell)) return v;
        }
        return null;
    }

    @Override
    public Variable createVariable(final String name, final Argument.Type type) throws UserWarningException {
        return createVariable(name, type, false);
    }

    @Override
    public Variable createVariable(final String name, final Argument.Type type, boolean grandfathered)
            throws UserWarningException {
        // Check to make sure the variable name is not already in use:
        Variable varTest = getVariable(name);
        if (varTest != null) {
            throw new UserWarningException("Unable to add column with name '" + name
                    + "', one with the same name already exists.");
        }

        Argument rootNode;
        if (type == Argument.Type.MATRIX) rootNode = new Argument(name + name.hashCode(), type);
        else rootNode = new Argument("var", type);

        Variable v = new DatavyuVariable(name, rootNode, grandfathered, this);
        variables.put(name, v);

        for (DataStoreListener dbl : this.dataStoreListeners) {
            dbl.variableAdded(v);
        }

        markAsChanged();
        return v;
    }

    @Override
    public void removeVariable(final Variable var) {
        for (DataStoreListener listener : this.dataStoreListeners) {
            listener.variableRemoved(var);
        }
        variables.remove(var.getName());
        markAsChanged();
    }

    @Override
    public void addVariable(final Variable var) {
        for (DataStoreListener dbl : this.dataStoreListeners) {
            dbl.variableAdded(var);
        }

        variables.put(var.getName(), var);
        markAsChanged();
    }

    @Override
    public void removeCell(final Cell cell) {
        getVariable(cell).removeCell(cell);
        markAsChanged();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String datastoreName) {
        name = datastoreName;
    }

    @Override
    public void markAsUnchanged() {
        if (changed) {
            changed = false;

            if (DatavyuDataStore.titleNotifier != null) {
                DatavyuDataStore.titleNotifier.updateTitle();
            }
        }
    }

    @Override
    public void updateVariableName(String oldName, String newName, Variable variable) {
        this.variables.remove(oldName);
        this.variables.put(newName, variable);
        if (!oldName.equals(newName)) markAsChanged();
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public void setTitleNotifier(final TitleNotifier titleNotifier) {
        DatavyuDataStore.titleNotifier = titleNotifier;
    }

    @Override
    public void addListener(final DataStoreListener listener) {
        dataStoreListeners.add(listener);
    }

    @Override
    public void removeListener(final DataStoreListener listener) {
        if (dataStoreListeners.contains(listener)) {
            dataStoreListeners.remove(listener);
        }
    }
    
    @Override
    public void addExemptionVariable(String name)
    {
        exemptionVariables += name + "\n";
    }
    
    @Override
    public String getExemptionVariables()
    {
        return exemptionVariables;
    }
}
