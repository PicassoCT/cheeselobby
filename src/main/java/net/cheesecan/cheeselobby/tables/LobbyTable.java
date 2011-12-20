/*
 *  Copyright 2011 Jahziah Wagner <jahziah[dot]wagner[at]gmail.com>.
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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * @author jahziah
 */
public class LobbyTable extends PackedTable {
    
    private int lastRowSelectedHash = -1;

    public LobbyTable(LobbyTableModel model) {
        // call my father and pass to it my TableModel
        super(model);

        // Remove border
        //setBorder(null);
        //SubstanceLookAndFeel.setDecorationType(this, DecorationAreaType.NONE);

        // Make column headers clickable
        JTableHeader header = this.getTableHeader();
        header.setUpdateTableInRealTime(true);

        final TableColumnModel m = this.getColumnModel();
        final LobbyTable table = this;
        header.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int col = m.getColumnIndexAtX(e.getX());
                ((LobbyTableModel) table.getModel()).sort(col, false);
            }
        });
        
        // Make selectable one row at a time
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        
        getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                selectedRowEvent(e.getFirstIndex());
            }
        });
        
        setEnabled(true);
    }
    
    private void selectedRowEvent(int index0) {
        if(index0 == -1 || index0 >= ((LobbyTableModel) getModel()).getData().size()){
            return;
        }
        lastRowSelectedHash = ((LobbyTableModel) getModel()).getData().get(index0).hashCode();
        
        //System.out.println("Selected row " + index0 +" with hash " + lastRowSelectedHash + ".");
        //System.out.println("Battle is " + ((LobbyTableModel) getModel()).getData().get(index0).toString() +"");
    }
    
    public void sortByColumn(int columnId) {
        ((LobbyTableModel) getModel()).sort(columnId, false);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        
        // When there is a fireTableStructureChanged event, we want to remember what row we had selected previously, if any
        if(lastRowSelectedHash != -1) {
            List data = ((LobbyTableModel) getModel()).getData();
            for(int k=0; k<data.size(); k++) {
                if(data.get(k).hashCode() == lastRowSelectedHash) {
                    setRowSelectionInterval(k, k);
                    
                    //System.out.println("Re-selected row " + k + " with hash " + data.get(k).hashCode() + ".");
                    //System.out.println("Battle is " + data.get(k).toString() +"\n");
                    break; 
                }
            }
        }
    }

    @Override
    public void setModel(TableModel dataModel) {
        // Remember currently selected row
        int selRow = getSelectedRow();

        super.setModel(dataModel);

        // Set selected row back
        if(selRow != -1 && selRow <= getRowCount()) {
            setRowSelectionInterval(selRow, selRow);
        }
    }
}
