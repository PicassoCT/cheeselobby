package net.cheesecan.cheeselobby.tables;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * @author jahziah
 */
public class LobbyTable extends PackedTable {

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
        setEnabled(true);
    }
    
    public void sortByColumn(int columnId) {
        ((LobbyTableModel) getModel()).sort(columnId, false);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        int prevSelectedRow = this.getSelectedRow();
        
        Object prev = null;
        if(prevSelectedRow != -1) {
            prev = ((LobbyTableModel) getModel()).getData().get(prevSelectedRow);
        }
        
        super.tableChanged(e);
        
        // When there is a fireTableStructureChanged event, we want to remember what row we had selected previously, if any
        if(prevSelectedRow != -1 && prevSelectedRow <= getRowCount()) {
            int pHashCode = prev.hashCode(); // hash of previous row, used to select same row after the update as indexes have changed
            
            List data = ((LobbyTableModel) getModel()).getData();
            for(int k=0; k<data.size(); k++) {
                if(data.get(k).hashCode() == pHashCode) {
                    setRowSelectionInterval(k, k);
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
