package net.cheesecan.cheeselobby.ui.interfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.cheesecan.cheeselobby.session.Battle;
import net.cheesecan.cheeselobby.lobby_connection.interfaces.ChatObserver;
import net.cheesecan.cheeselobby.session.User;

/**
 *
 * @author jahziah
 */
public interface SessionHandlerFacade {
    // Account

    /**
     * Login the client.
     * @param username
     * @param passwordPlaintext
     */
    public void login(final String username, final String passwordPlaintext);

    /**
     * Called by GUI to confirm login success.
     */
    public void confirmLogin();

    /**
     * Disconnect player.
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
     * Get username;
     * @return
     */
    public String getUsername();

    // Battles
    /**
     * Say something in a channel
     * @param msg
     * @param channel
     */
    public void say(String msg, String channel);

    /**
     * Say something inside current battle.
     * @param str
     */
    public void sayBattle(String str);

    /**
     * Join a battle.
     * @param battleId
     */
    public void joinBattle(int battleId);

    /**
     * Leave current battle.
     */
    public void leaveBattle();

    // Channels
    /**
     * List all chat channels.
     */
    public void listChannels();

    /**
     * Join a chat channel.
     * @param name
     */
    public void joinChannel(String name);

    // Containers
    /**
     * List that contains all users inside a particular channel.
     * @param channel
     * @return
     */
    public List<User> getChannelUserList(String channel);

    /**
     * Map that contains all users inside a particular battle room.
     * @return
     */
    public HashMap<Integer, ArrayList<User>> getBattleRoomUsers();

    /**
     * Map that contains all users sorted by which chat rooms they are in.
     * @return
     */
    public HashMap<String, List<User>> getChatRoomUsers();

    /**
     * Map that contains all battles.
     * @return
     */
    public HashMap<Integer, Battle> getBattles();

    /**
     * Map that contains every single user on the server right now.
     * @return
     */
    public List<User> getServerUsers();

    /**
     * Called by a chat panel to register itself as an observer of chat rooms.
     * @param o
     */
    public void registerChatObserver(ChatObserver o);
}
