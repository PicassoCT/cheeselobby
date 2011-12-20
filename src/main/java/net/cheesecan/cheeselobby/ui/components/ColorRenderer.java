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

import java.awt.Color;
import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JTable;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer.BooleanRenderer;

/**
 *
 * @author jahziah
 */
public class ColorRenderer extends BooleanRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Color that we use as background color for a button
        JButton btn = new JButton();
        btn.setBackground((Color) value);
        btn.setFocusPainted(hasFocus);
        btn.setSelected(isSelected);
        return btn;
    }

}
