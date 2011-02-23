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

import net.cheesecan.cheeselobby.lobby_connection.interfaces.ChatObserver;

/**
 * The ChatControllerFacade is used by GUI chat windows to communicate with the SessionController.
 * @author jahziah
 */
public interface ChatControllerFacade {

    /**
     * Registers the caller as a ChatObserver.
     * @param caller
     */
    public void registerChatObserver(ChatObserver caller);

    /**
     * List all chat channels on the server.
     */
    public void listAllChannels();

    /**
     * Join a chat channel.
     * @param name
     */
    public void joinChannel(String name);

    /**
     * Leave a channel.
     * @param name
     */
    public void leaveChannel(String name);

    /**
     * Say something in a channel
     * @param msg
     * @param channel
     */
    public void say(String msg, String channel);

    /**
     * Say something in a private conversation
     * @param msg
     * @param channel
     */
    public void sayPrivate(String msg, String channel);
    
    /**
     * Used to check if a username exists on server.
     * @param username the specified username.
     * @return true if the username exists, otherwise null.
     */
    public boolean isValidUsername(String username);
}
