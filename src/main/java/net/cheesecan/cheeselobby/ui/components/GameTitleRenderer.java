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
