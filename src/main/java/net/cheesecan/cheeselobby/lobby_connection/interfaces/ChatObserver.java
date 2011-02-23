/*
 *  Copyright 2010 jahziah.
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

import net.cheesecan.cheeselobby.session.User;

/**
 *
 * @author jahziah
 */
public interface ChatObserver {

    /**
     * Replies with which channel the observer is currently looking at.
     * @return
     */
    public String getActiveChannel();

    /**
     * Add user to a user table
     * @param channel
     * @param user
     */
    public void addUser(String channel, User user);

    /**
     * Remove a user from user table based on user object.
     * @param channel
     * @param user
     */
    public void removeUser(String channel, User user, String reason);


    /**
     * Tells the observer that it should open a chat channel tab.
     * @param name name of the channel
     * @param room reference to the channel user list
     */
    public void openChatPanel(String name);


     /**
     * Notifies the GUI that it should close a chat tab.
     * @param channel
     */
    public void closeChannelTab(String channel);


    /**
     * Tells the GUI to set the message of the day.
     * @param line
     */
    public void messageOfTheDay(String line);


    /**
     * Notifies the GUI that it should read the topic of a specific channel it's in.
     * @param chanName
     * @param author
     * @param changedTime
     * @param topic
     */
    public void setTopic(String chanName, String author, long changedTime, String topic);

    /**
     * Notifies the GUI of a private chat message.
     *
     *
    /**
     * Notifies the GUI of a chat message.
     * @param channel
     * @param sender
     * @param msg
     */
    public void notifyChatMessage(String channel, String sender, String msg, boolean isExpression);

    /*
     * @param channel
     * @param sender
     * @param msg
     */
    public void notifyPrivateChatMessage(String ourselves, String otherParty, String msg, boolean echoMessage);

    /**
     * Notifies observer that the server broadcasted a message to a channel.
     * @param channel channel that broadcast is in
     * @param message message broadcasted
     */
    public void notifyBroadcastMessage(String channel, String message);

    /**
     * Notifies the chat observer what the lobby latency to server is.
     * @param latency time measured since sometime during the 1970s.
     */
    public void notifyLatency(long latency);

    /**
     * Notify window that channel could not be joined
     */
    void joinFailed(String reason);

}
