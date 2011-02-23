/*
 *  Copyright 2011 jahziah.
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

package net.cheesecan.cheeselobby.ui.interfaces;

import net.cheesecan.cheeselobby.lobby_connection.interfaces.LoginObserver;

/**
 *
 * @author jahziah
 */
public interface LoginControllerFacade {

    /**
     * Register as a login observer to receive login notifications.
     * @param caller the this reference of the caller.
     */
    public void registerAsLoginObserver(LoginObserver caller);

    /**
     * Used by callee to notify SessionController that callee wants the SessionController to login to server.
     * @param username username to login with
     * @param passwordPlaintext password to login with
     */
    public void login(final String username, final String passwordPlaintext);

    /**
     * Disconnect from TasServer.
     */
    public void disconnect();

    /**
     * Register a new player account.
     * @param username
     * @param passwordPlaintext
     */
    public void register(String username, String passwordPlaintext);

    /**
     * Change player password.
     * @param newPassword
     */
    public void changePassword(String newPassword);

    /**
     * Get username.
     * @return returns username if logged in, or null if not logged in.
     */
    public String getUsername();

    /**
     * Notifies controller that GUI is ready to begin taking call-backs.
     */
    public void guiIsReady();

}
