package net.cheesecan.cheeselobby.ui.components;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import net.cheesecan.cheeselobby.ui.interfaces.DownloadObserver;

/**
 *
 * @author jahziah
 */
public class ProgressElement {

    private JLabel label;
    private JProgressBar progress;
    private JButton closeButton;
    private DownloadObserver parent;
    private int position = -1;
    private JPanel panel;
    private String name;

    public ProgressElement(String name, DownloadObserver parent) {
        this.parent = parent;
        this.name = name;
        
        label = new JLabel(name);
        progress = new JProgressBar();
        closeButton = new JButton("x");
        closeButton.setPreferredSize(new Dimension(25, (int)progress.getPreferredSize().getHeight()));

        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                remove();
            }
        });
        
        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        
        panel.add(label);
        panel.add(progress);
        panel.add(closeButton);
    }

    public void setProgress(int i, long bps) {
        if(bps == -1) {
            label.setText(name + ": Done");
            closeButton.setEnabled(false);
        } else {
            label.setText(name +"("+ bps/1024 + " kb/s)");
        }
        
        progress.setValue(i);
    }

    private void remove() {
        DownloadObserver.progressContainer.remove(label.getName());
        parent.removeProgressElement(this);
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public JLabel getLabel() {
        return label;
    }

    public JButton getCloseButton() {
        return closeButton;
    }

    public JProgressBar getProgress() {
        return progress;
    }

    public JPanel getPanel() {
        return panel;
    }

    public void refresh() {
        progress.validate();
        progress.repaint();
        label.validate();
        label.repaint();
    }
    
    
}