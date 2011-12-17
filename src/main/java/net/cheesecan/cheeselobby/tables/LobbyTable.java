package net.cheesecan.cheeselobby.tables;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
        
        super.tableChanged(e);
        
        // When there is a fireTableStructureChanged event, we want to remember what row we had selected previously, if any
        if(prevSelectedRow != -1 && prevSelectedRow <= getRowCount()) {
            setRowSelectionInterval(prevSelectedRow, prevSelectedRow);
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
