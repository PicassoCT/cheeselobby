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
            logWriter = new FileWriter("../server.log");
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
                //logWriter.flush();
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
