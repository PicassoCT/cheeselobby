package net.cheesecan.cheeselobby.ui.components;

import java.util.Comparator;
import net.cheesecan.cheeselobby.session.SessionObject;

/**
 *
 * @author jahziah
 */
public class ColumnComparator implements Comparator {

    private int column;
    private boolean sortAscending;

    public ColumnComparator(int column) {
        this.column = column;
        this.sortAscending = true;
    }

    @Override
    public int compare(Object o1, Object o2) {
        String s1 = ((SessionObject) o1).getValueByIndex(column).toString();
        String s2 = ((SessionObject) o2).getValueByIndex(column).toString();

        // Check if strings are actually integers
        int i1;
        int i2;
        try {
            i1 = Integer.valueOf(s1);
            i2 = Integer.valueOf(s2);
        } catch (NumberFormatException e) {
            // These strings were not integers, so we just treat them like what they are(strings)
            return strCompare(s1, s2);
        }
        //System.out.println("Was an int");
        // If we could proceed then they are integers
        // If any of them is less than 10, append a zero in front of them to fix sorting
        s1 = i1 < 10 ? "0"+s1 : s1;
        s2 = i2 < 10 ? "0"+s2 : s2;

        return strCompare(s1, s2);
    }

    public int strCompare(String s1, String s2) {
        if (!sortAscending) {
            return ((String) s1).compareTo((String) s2);
        } else {
            return ((String) s2).compareTo((String) s1);
        }
    }

    public void flip() {
        sortAscending = !sortAscending;
    }
}
