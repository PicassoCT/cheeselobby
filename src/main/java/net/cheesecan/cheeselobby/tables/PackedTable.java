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