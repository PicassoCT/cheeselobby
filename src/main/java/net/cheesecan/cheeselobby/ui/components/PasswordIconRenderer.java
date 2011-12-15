/*
 *  Copyright 2011 jahziah.
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
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer.BooleanRenderer;

/**
 *
 * @author jahziah
 */
public class PasswordIconRenderer extends BooleanRenderer {

    private ImageIcon passwordIcon;

    public PasswordIconRenderer() {
        passwordIcon = new ImageIcon(getClass().getResource("/img/status/password.png"));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Object is a boolean
        boolean passworded = (Boolean)value;

        JLabel label = new JLabel();
        if(passworded) {
            label.setIcon(passwordIcon);
        }
        label.setMinimumSize(new Dimension(passwordIcon.getIconWidth(), passwordIcon.getIconHeight()));
        return label;
    }
}
