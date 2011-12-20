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
package net.cheesecan.cheeselobby.ui.components;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextArea;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

/**
 *
 * @author jahziah
 */
public class GameTitleRenderer extends SubstanceDefaultTableCellRenderer {

    private final int LIMIT = 40;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String val = (String) value;
        if (column == 2 && val.length() > LIMIT) { // name column is too long
            val = val.substring(0, LIMIT) + "-\n" + val.substring(LIMIT, val.length());
            if (val.length() > 80) {
                val = val.substring(0, 77) + "...";
            }
            JTextArea label = new JTextArea(val);
            label.setToolTipText(val);
            return label;
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
