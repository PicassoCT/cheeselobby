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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import net.cheesecan.cheeselobby.ui.interfaces.DownloaderFacade;

/**
 *
 * @author jahziah
 */
public class ProgressElement {

    private JLabel label;
    private JProgressBar progress;
    private JButton closeButton;
    private DownloaderFacade parent;
    private int position = -1;
    private JPanel panel;
    private String name;

    public ProgressElement(String name, DownloaderFacade parent) {
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
        DownloaderFacade.progressContainer.remove(label.getName());
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