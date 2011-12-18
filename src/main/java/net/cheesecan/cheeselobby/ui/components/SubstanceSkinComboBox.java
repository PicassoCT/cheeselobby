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
