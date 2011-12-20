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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.cheesecan.cheeselobby.ui.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultComboBoxRenderer;
import org.pushingpixels.substance.api.skin.SkinInfo;

/**
 *
 * @author jahziah
 */
public class SubstanceSkinComboBox extends JComboBox {

    public SubstanceSkinComboBox() {
        // populate the combobox
        super(new ArrayList<SkinInfo>(SubstanceLookAndFeel.getAllSkins().values()).toArray());
        // set the current skin as the selected item
        SubstanceSkin currentSkin = SubstanceLookAndFeel.getCurrentSkin();
        if(currentSkin != null) {
            for (SkinInfo skinInfo : SubstanceLookAndFeel.getAllSkins().values()) {
                if (skinInfo.getDisplayName().equals(
                        currentSkin.getDisplayName())) {
                    this.setSelectedItem(skinInfo);
                    break;
                }
            }
        }
        // set custom renderer to show the skin display name
        this.setRenderer(new SubstanceDefaultComboBoxRenderer(this) {

            @Override
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                return super.getListCellRendererComponent(list,
                        ((SkinInfo) value).getDisplayName(), index, isSelected,
                        cellHasFocus);
            }
        });
        // add an action listener to change skin based on user selection
        this.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        SubstanceLookAndFeel.setSkin(((SkinInfo) SubstanceSkinComboBox.this.getSelectedItem()).getClassName());
                    }
                });
            }
        });
    }
}
