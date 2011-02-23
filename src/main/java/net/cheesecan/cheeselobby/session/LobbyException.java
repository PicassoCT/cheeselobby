/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.cheesecan.cheeselobby.session;

/**
 *
 * @author jahziah
 */
public class LobbyException extends Exception {

    public LobbyException(String message) {
        super(message);
    }

    public static class ListenerException extends LobbyException {
        public ListenerException(String message) {
            super(message);
        }
    }
}
