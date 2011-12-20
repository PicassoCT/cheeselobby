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

import net.cheesecan.cheeselobby.session.Battle;
import net.cheesecan.cheeselobby.session.User;
import net.cheesecan.cheeselobby.session.User.GameStatus;

/**
 *
 * @author jahziah
 */
public interface BattleObserverDeprecated {

    /**
     * Called when the client has joined a battle that should be displayed.
     * @param battle
     */
    public void displayBattle(Battle battle);

    /**
     * Closes battle view.
     */
    public void closeBattle();

     /**
     * Add user to a user table.
     * @param channel
     * @param user
     */
    public void addUser(User user);

    /**
     * Remove a user from user table based on user object.
     * @param channel
     * @param user
     */
    public void removeUser(User user);

    /**
     * Updates a user's status in the battle.
     * @param user
     * @param previousMode the previous mode of the user. This could be the same or have changed. It is used for the auto-unspec feature.
     */
    public void updateUser(User user, GameStatus previousMode);

    /**
     * Set our own user.
     * @param User new user
     */
    public void setOwnUser(User user);

    /**
     * Notify that someone wrote something in the battle.
     * @param sender
     * @param msg
     */
    public void said(String sender, String msg);

    /**
     * Same as said but for expressions.
     * @param sender
     * @param msg
     */
    public void saidExpression(String sender, String msg);

    /**
     * Makes GUI display an error msg.
     * @param msg contents of said message.
     */
    public void showErrorMessage(String msg, String title);

    /**
     * Notify the battle room that we were kicked.
     */
    public void kickedFromBattle();
}
