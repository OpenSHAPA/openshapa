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

import java.util.*;


public final class DatavyuMatrixCellValue extends DatavyuCellValue implements MatrixCellValue {

    private UUID parentId;
    private String value;
    private List<CellValue> cellValues;


    public DatavyuMatrixCellValue() {
    }

    public DatavyuMatrixCellValue(UUID parent_id, Argument type, Cell parent) {
        this.parentId = parent_id;
        this.parent = parent;
        cellValues = new ArrayList<CellValue>();
        for (Argument arg : type.childArguments) {
            createArgument(arg);
        }
        this.arg = type;
        value = "MATRIX";
    }

    // Method to order the cellValues coming out of the DB.
    private static void order(List<CellValue> cellValues) {

        Collections.sort(cellValues, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {

                int x1 = ((DatavyuCellValue) o1).getIndex();
                int x2 = ((DatavyuCellValue) o2).getIndex();

                if (x1 != x2) {
                    return x1 - x2;
                } else {
                    return 0;
                }
            }
        });
    }
    
    @Override
    public String toString() {
        String result = "";
        List<CellValue> cellValues = getArguments();

        result += "(";
        for (int i = 0; i < cellValues.size(); i++) {
            CellValue v = cellValues.get(i);
            if (v.toString() == null) {
                result += "<code" + String.valueOf(i) + ">";
            } else {
                result += v.toString();
            }
            if (i < cellValues.size() - 1) {
                result += ",";
            }
        }
        result += ")";

        return result;
    }

    public String serialize() {
        List<CellValue> cellValues = getArguments();

        StringBuilder result = new StringBuilder("(");
        for (Iterator<CellValue> i = cellValues.iterator(); i.hasNext(); ) {
            CellValue v = i.next();
            result.append(v.serialize());
            if (i.hasNext())
                result.append(',');
        }
        result.append(')');

        return result.toString();
    }

    @Override
    public List<CellValue> getArguments() {
        order(cellValues);
        return cellValues;
    }

    @Override
    public CellValue createArgument(Argument arg) {
        CellValue val = null;
        String name = String.format("code%02d", getArguments().size() + 1);
        if (arg.type == Argument.Type.NOMINAL) {
            val = new DatavyuNominalCellValue(this.id, name, getArguments().size(), arg, parent);
        } else if (arg.type == Argument.Type.TEXT) {
            val = new DatavyuTextCellValue(this.id, name, getArguments().size(), arg, parent);
        }
        this.getArguments().add(val);
        return val;
    }

    @Override
    public void removeArgument(final int index) {
        List<CellValue> args = getArguments();
        args.remove(index);
        CellValue v;
        for (int i = 0; i < args.size(); i++) {
            v = args.get(i);
            ((DatavyuNominalCellValue) v).setIndex(i);
        }
    }

    @Override
    public void set(String value) {
        if (value.startsWith("(") && value.endsWith(")")) {
            value = value.substring(1, value.length() - 1);
        }
        String[] args = value.split(",", -1);
        List<CellValue> cellValues = getArguments();

        // Handle legacy variable types
        if (cellValues.size() == 1 && cellValues.get(0).getArgument().type != Argument.Type.MATRIX) {
            cellValues.get(0).set(value);
        } else {
            if (args.length != cellValues.size()) {
                System.err.println("Error: Arg list and value list are different sizes, cannot undo.");
            }
            for (int i = 0; i < args.length; i++) {
                cellValues.get(i).set(args[i]);
            }
        }
    }
}
