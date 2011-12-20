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

package net.cheesecan.cheeselobby.lobby_connection.interfaces;

import java.io.StringReader;

/**
 *
 * @author jahziah
 */
public interface LoginObserver {

      /**
     * Notifies the GUI that login succeeded.
     */
    public void loginSuccess();

    /**
     * Notifies the GUI that login failed.
     * @param reason why the login failed.
     */
    public void loginFail(String reason);

    /**
     * Notifies the GUI that registration succeeded.
     */
    public void registrationSuccess();

    /**
     * Notifies the GUI that registration failed.
     * @param reason why the registration failed.
     */
    public void registrationFail(String reason);

    public void displayAgreement(StringReader rtf);
}
