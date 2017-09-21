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
package org.datavyu.models.db;

import java.util.*;


public class DatavyuCell implements Cell {

    static Map<UUID, List<CellListener>> allListeners = new HashMap<UUID, List<CellListener>>();
    final private UUID id = UUID.randomUUID();
    private long onset = 0L;
    private long offset = 0L;
    private Argument type;
    private boolean selected;
    private boolean highlighted;
    private Variable parent;
    private Map<String, CellValue> arguments = new HashMap<String, CellValue>();
    private CellValue cellValue;

    public DatavyuCell() {
    }

    public DatavyuCell(Variable parent, Argument type) {
        this.parent = parent;
        this.type = type;

        this.onset = 0L;
        this.offset = 0L;
        this.selected = true;
        this.highlighted = true;

        // Build argument list from the argument given

        if (type.type == Argument.Type.NOMINAL) {
            this.cellValue = new DatavyuNominalCellValue(getID(), type, this);
        } else if (type.type == Argument.Type.TEXT) {
            this.cellValue = new DatavyuTextCellValue(getID(), type, this);
        } else {
            this.cellValue = new DatavyuMatrixCellValue(getID(), type, this);
        }
    }

    /**
     * @param cellId The Identifier of the variable we want the listeners for.
     * @return The list of listeners for the specified cellId.
     */
    private static List<CellListener> getListeners(UUID cellId) {
        List<CellListener> result = allListeners.get(cellId);

        if (result == null) {
            result = new ArrayList<CellListener>();
            allListeners.put(cellId, result);
        }

        return result;
    }

    public Variable getVariable() {
        return parent;
    }

    private String convertMStoTimestamp(long time) {
        long hours = Math.round(Math.floor((time / 1000.0 / 60.0 / 60.0)));
        long minutes = Math.round(Math.floor(time / 1000.0 / 60.0 - (hours * 60)));
        long seconds = Math.round(Math.floor(time / 1000.0 - (hours * 60 * 60) - (minutes * 60)));
        long mseconds = Math.round(Math.floor(time - (hours * 60 * 60 * 1000) - (minutes * 60 * 1000) - (seconds * 1000)));

        return String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, mseconds);
    }

    private long convertTimestampToMS(String timestamp) {

        String[] s = timestamp.split(":");
        if(s.length == 1){
            return Long.valueOf(timestamp);
        }
        long hours = Long.valueOf(s[0]) * 60 * 60 * 1000;
        long minutes = Long.valueOf(s[1]) * 60 * 1000;
        long seconds = Long.valueOf(s[2]) * 1000;
        long mseconds = Long.valueOf(s[3]);

        return hours + minutes + seconds + mseconds;
    }


    @Override
    public String getOffsetString() {
        return convertMStoTimestamp(offset);
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public void setOffset(final long newOffset) {
        if (newOffset != offset) parent.getOwningDatastore().markAsChanged();
        offset = newOffset;
        for (CellListener cl : getListeners(getID())) {
            cl.offsetChanged(offset);
        }
    }

    @Override
    public void setOffset(final String newOffset) {
        setOffset(convertTimestampToMS(newOffset));
    }

    @Override
    public Cell getFreshCell() {
        return this;
    }

    @Override
    public long getOnset() {
        return onset;
    }

    @Override
    public void setOnset(final String newOnset) {
        setOnset(convertTimestampToMS(newOnset));
    }

    @Override
    public void setOnset(final long newOnset) {
        if (newOnset != onset) parent.getOwningDatastore().markAsChanged();
        onset = newOnset;
        for (CellListener cl : getListeners(getID())) {
            cl.onsetChanged(onset);
        }
    }

    @Override
    public String getOnsetString() {
        return convertMStoTimestamp(onset);

    }

    @Override
    public String getValueAsString() {
        return getCellValue().toString();
    }

    @Override
    public CellValue getCellValue() {
        return this.cellValue;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(final boolean selected) {
        this.selected = selected;
        if (!selected) {
            setHighlighted(false);
        }

        for (CellListener cl : getListeners(getID())) {
            cl.selectionChange(selected);
            if (!selected) {
                cl.highlightingChange(false);
            }
        }
    }

    @Override
    public boolean isHighlighted() {
        return highlighted;
    }

    @Override
    public void setHighlighted(final boolean highlighted) {
        this.highlighted = highlighted;

        if (highlighted) {
            setSelected(highlighted);
        }

        for (CellListener cl : getListeners(getID())) {
            cl.highlightingChange(highlighted);
        }
    }

    @Override
    public void addMatrixValue(Argument type) {
        DatavyuMatrixCellValue val = (DatavyuMatrixCellValue) getCellValue();
        val.createArgument(type);
    }

    @Override
    public void moveMatrixValue(final int oldIndex, int newIndex) {
        DatavyuMatrixCellValue val = (DatavyuMatrixCellValue) getCellValue();
        List<CellValue> cellValues = val.getArguments();
        CellValue v = cellValues.get(oldIndex);

        cellValues.remove(oldIndex);
        cellValues.add(newIndex, v);

        for (int i = 0; i < cellValues.size(); i++) {
            ((DatavyuCellValue) cellValues.get(i)).setIndex(i);
        }
    }

    @Override
    public void removeMatrixValue(final int index) {
        ((DatavyuMatrixCellValue) getCellValue()).removeArgument(index);
    }

    @Override
    public void setMatrixValue(final int index, final String value) {
        DatavyuMatrixCellValue val = (DatavyuMatrixCellValue) getCellValue();
        List<CellValue> cellValues = val.getArguments();
        cellValues.get(index).set(value);
    }

    @Override
    public CellValue getMatrixValue(final int index) {
        return ((DatavyuMatrixCellValue) getCellValue()).getArguments().get(index);
    }

    @Override
    public void clearMatrixValue(final int index) {
        DatavyuMatrixCellValue val = (DatavyuMatrixCellValue) getCellValue();
        List<CellValue> cellValues = val.getArguments();
        cellValues.get(index).clear();
    }

    @Override
    public void addListener(final CellListener cellListener) {
        getListeners(getID()).add(cellListener);
    }

    @Override
    public void removeListener(final CellListener cellListener) {
        getListeners(getID()).remove(cellListener);
    }

    public UUID getID() {
        return id;
    }

    @Override
    public String getCellId() {
        return this.getID().toString();
    }

    @Override
    public int hashCode() {
        return this.getID().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DatavyuCell)) {
            return false;
        }
        DatavyuCell otherC = (DatavyuCell) other;

        if (otherC.getID().toString().equals(this.getID().toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isInTimeWindow(long time) {
        if (time >= getOnset() && time < getOffset()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isPastTimeWindow(long time) {
        if (time > Math.max(getOnset(), getOffset())) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    /* Print string representation of this cell. */
    public String toString(){
        return "[" +onset+ "," + offset + "," + getValueAsString()+ "]";
    }
}

