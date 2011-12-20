/*
 *  Copyright 2011 Jahziah Wagner.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package net.cheesecan.cheeselobby.tables;

import java.util.Collections;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import net.cheesecan.cheeselobby.session.SessionObject;
import net.cheesecan.cheeselobby.ui.components.ColumnComparator;

/**
 *
 * @author jahziah
 */
public class LobbyTableModel<E extends SessionObject> extends DefaultTableModel {

    // Fields
    private List<E> data;
    private String[] columnNames;
    private ColumnComparator[] comparator;
    private int lastSortedBy;   // remember the column we sorted by last time

    public LobbyTableModel(List<E> sessionObjects, String[] columnNames, int initiallySortedByColumn) {
        this.data = sessionObjects;
        this.columnNames = columnNames;
        lastSortedBy = initiallySortedByColumn;

        // Initialize comparator for each column
        comparator = new ColumnComparator[columnNames.length];
        for (int col = 0; col < columnNames.length; col++) {
            comparator[col] = new ColumnComparator(col);
        }
    }

    @Override
    public int getRowCount() {
        /*
        if (data == null || data.isEmpty()) {
            return 0;
        } else {
            return data.size();
        }*/
        if(data == null) {
            return 0;
        }
        return data.size();
    }

    @Override
    public int getColumnCount() {
        /*
        if (columnNames == null || columnNames.length <= 0) {
            return 0;
        } else {
            return columnNames.length;
        }*/
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        return data.get(row).getValueByIndex(column);
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public synchronized void addRow(E newRow) {
        // Add the row
        data.add(newRow);

        // Update entire structure, which causes repaint
        updateTableModel();
    }

    public synchronized void removeRow(E sessionObjectToRemove) {
        // If we don't contain this row
        if (!data.contains(sessionObjectToRemove)) {
            return;
        }

        // Add the row
        data.remove(sessionObjectToRemove);

        // Update entire structure, which causes repaint
        updateTableModel();
    }

    public void setData(List<E> data) {
        this.data = data;
        updateTableModel();
    }

    public List<E> getData() {
        return data;
    }

    private void updateTableModel() {
        // Re-sort table by the same column as it was last sorted by
        sort(lastSortedBy, true);
        
        // Fire change event
        fireTableStructureChanged();
    }

    /**
     * Sorts columns of the model in ascending or descending order.
     * @param column column index to sort.
     */
    public void sort(int column, boolean keepOrder) {
        if(column == lastSortedBy && !keepOrder) {
            // Flip value if this is the same column as last time
            comparator[column].flip();
        }
        // Sort columns
        Collections.sort(data, comparator[column]);
        
        // Set lastSortedBy
        lastSortedBy = column;
    }
}
