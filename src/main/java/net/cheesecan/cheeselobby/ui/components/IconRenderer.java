/*
 *  Copyright 2010 Jahziah Wagner <jahziah[dot]wagner[at]gmail.com>.
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
import java.awt.Dimension;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

/**
 * This renderer reads the text on a country column and determines what country icon to put there.
 * Only for use on columns where you want country flags based on ISO-3166.
 * @author jahziah
 */
public class IconRenderer extends SubstanceDefaultTableCellRenderer {

    private Map<String, ImageIcon> icons;

    public IconRenderer(Map<String, ImageIcon> icons) {
        this.icons = icons;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Add icon
        JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        ImageIcon icon = icons.get(value.toString().toLowerCase());
        label.setIcon(icon);
        label.setToolTipText(value.toString());
        label.setText("");
        label.setHorizontalAlignment(SwingConstants.CENTER);

        if(icon != null) {
            label.setMaximumSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        }
        
        return label;
    }
}