package net.cheesecan.cheeselobby;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    private ConcurrentLinkedQueue<String> queue;  // thread-safe queue
    private FileWriter logWriter;
    SessionController sessionController;

    public ListenerThread(SessionController sessionController) {
        setName("CheeseLobbyListener");
        threadState = ThreadState.Started;
        this.sessionController = sessionController;

        try {
            logWriter = new FileWriter("../server.log");
        } catch (IOException ex) {
            Logger.getLogger(ListenerThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        queue = new ConcurrentLinkedQueue<String>();
    }

    public void setInputStream(BufferedReader in) {
        this.in = in;
    }

    private void read() {
        try {
            String str;
            while ((str = in.readLine()) != null) { // while there are things to read
                // If this is a pong message
                if (str.equals("PONG")) {
                    sessionController.setLastPong(System.currentTimeMillis());
                } // If this is a regular message
                else {
                    queue.add(str);                 // add to queue
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
            read();
        }
    }

    /**
     * Synchronized method for popping queue item.
     * @return returns a string. String will be null if the queue is empty.
     */
    public synchronized String popQueueItem() {
        // Return null if empty
        if (queue.isEmpty()) {
            return null;
        }

        String item = queue.peek();
        queue.remove();
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
