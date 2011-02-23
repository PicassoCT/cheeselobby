package net.cheesecan.cheeselobby.tables;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * @author Rid
 * @author Santhosh Kumar T
 */
public class PackedTable extends JTable {
        public PackedTable(DefaultTableModel model) {
                super(model);
        }

        private TablePacker packer = null;

        public void pack(int rowsIncluded, boolean distributeExtraArea) {
                packer = new TablePacker(rowsIncluded, true);
                if (isShowing()) {
                        packer.pack(this);
                        packer = null;
                }
        }

        public void addNotify() {
                super.addNotify();
                if (packer != null) {
                        packer.pack(this);
                        packer = null;
                }
        }

        @Override
        public void setVisible(boolean aFlag) {
                // TODO Auto-generated method stub
                super.setVisible(aFlag);
                if (aFlag)
                        this.pack(TablePacker.VISIBLE_ROWS, true);

        }
}