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
package net.cheesecan.cheeselobby;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.cheesecan.cheeselobby.session.ThreadState;
import net.cheesecan.cheeselobby.session.LobbyException.ListenerException;

/**
 * Listens for messages input from TasServer and places each message onto a FIFO queue.
 * @author jahziah
 */
public class ListenerThread extends Thread {

    private ThreadState threadState;
    private BufferedReader in;
    private LinkedList<String> queue;  // thread-safe queue
    private FileWriter logWriter;
    private SessionController sessionController;
    private Semaphore sem;

    public ListenerThread(SessionController sessionController) {
        setName("CheeseLobbyListener");
        threadState = ThreadState.Started;
        this.sessionController = sessionController;

        try {
            logWriter = new FileWriter("./../server.log");
        } catch (IOException ex) {
            Logger.getLogger(ListenerThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        queue = new LinkedList<String>();
        sem = new Semaphore(1);
    }

    public void setInputStream(BufferedReader in) {
        this.in = in;
    }

    private void read() throws InterruptedException {
        try {
            String str;
            while ((str = in.readLine()) != null) { // while there are things to read
                // If this is a pong message
                if (str.equals("PONG")) {
                    sessionController.setLastPong(System.currentTimeMillis());
                } // If this is a regular message
                else {
                    sem.acquire();
                    queue.add(str);                 // add to queue
                    sem.release();
                }
                // Write msg to a file
                logWriter.write(str + "\n");
                logWriter.flush();
            }
        } catch (IOException ex) {
            if (threadState == ThreadState.Stopped) {
                System.err.println("Listener thread was stopped while trying to read.");
            } else {
                System.err.println(ex.getCause().getMessage());
            }
        }
    }

    @Override
    public void run() {
        while (threadState == ThreadState.Started) {
            try {
                read();
            } catch (InterruptedException ex) {
                Logger.getLogger(ListenerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Synchronized method for popping queue item.
     * @return returns a string. String will be null if the queue is empty.
     */
    public String popQueueItem() throws InterruptedException {
        sem.acquire();
        // Return null if empty
        if (queue.isEmpty()) {
            sem.release();
            return null;
        }

        String item = queue.peek();
        queue.remove();
        sem.release();
        return item;
    }

    public void disconnect() {
        try {
            threadState = ThreadState.Stopped;
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(ListenerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
