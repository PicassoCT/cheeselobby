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
package net.cheesecan.cheeselobby;

import net.cheesecan.cheeselobby.session.ClientInfo;
import net.cheesecan.cheeselobby.session.Battle;
import net.cheesecan.cheeselobby.lobby_connection.interfaces.BattleListObserver;
import net.cheesecan.cheeselobby.lobby_connection.interfaces.ChatObserver;
import net.cheesecan.cheeselobby.lobby_connection.interfaces.BattleRoomObserver;
import net.cheesecan.cheeselobby.io.ScriptFile;
import net.cheesecan.cheeselobby.io.SettingsFile;
import net.cheesecan.cheeselobby.ui.interfaces.BattleListControllerFacade;
import net.cheesecan.cheeselobby.ui.interfaces.BattleControllerFacade;
import net.cheesecan.cheeselobby.ui.interfaces.LoginControllerFacade;
import net.cheesecan.cheeselobby.ui.interfaces.ChatControllerFacade;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import net.cheesecan.cheeselobby.session.ServerMessage;
import net.cheesecan.cheeselobby.session.ThreadState;
import net.cheesecan.cheeselobby.session.LobbyException.ListenerException;
import net.cheesecan.cheeselobby.lobby_connection.interfaces.LoginObserver;
import net.cheesecan.cheeselobby.session.MD5Base64Hash;
import net.cheesecan.cheeselobby.session.User;
import net.cheesecan.cheeselobby.session.User.GameStatus;
import net.cheesecan.cheeselobby.ui.interfaces.Disconnectable;

/**
 *
 * @author jahziah
 */
public class SessionController extends Thread implements BattleListControllerFacade, BattleControllerFacade,
        ChatControllerFacade, LoginControllerFacade, Executor {

    // The GUI can ask this thread to perform tasks. These are performed in FIFO order.
    final Queue<Runnable> tasks;
    // Observers
    private LoginObserver loginObserver;
    private ChatObserver chatObserver;
    private BattleListObserver battleListObserver;
    private BattleRoomObserver battleObserver;
    private Disconnectable mainFrame;
    // Connections
    private Socket socket;
    private ListenerThread listener;
    // Containers
    private HashMap<String, User> usersOnServer;    //O(1) lookup of users
    private Map<Integer, Battle> battlesOnServer;
    private HashMap<Integer, ArrayList<User>> usersInBattles;
    // Status
    private ThreadState threadState;
    private boolean guiReady;
    private boolean isLoggedIn;
    private String username;
    private int currentBattle;
    // Ping
    private long lastPing;      // the time when we sent our last ping
    private long lastPong;      // the time when we got our last pong
    private Semaphore lastPongSemaphore = new Semaphore(1); // sempahore used to provide concurrent-safe access to lastPong
    //private long pingInterval;  // how often to send pings
    private boolean ping;
    private static long minPingDelay;  // minimum delay between pings
    private long pingTimeout;
    // Application info
    private SettingsFile settings;
    // Used for agreement message only, TODO consider refactoring
    StringBuffer agreement = new StringBuffer();
    private String battlePswd;

    /**
     * Initializes members.
     */
    public SessionController(SettingsFile settings, Disconnectable main) {
        setName("CheeseLobbyController");
        mainFrame = main;
        tasks = new LinkedBlockingQueue<Runnable>();
        usersOnServer = new HashMap<String, User>();
        battlesOnServer = Collections.synchronizedMap(new HashMap<Integer, Battle>());
        usersInBattles = new HashMap<Integer, ArrayList<User>>();
        threadState = ThreadState.Started;
        guiReady = false;
        isLoggedIn = false;
        lastPing = 0;
        ping = true;            // initialize to true to begin pinging
        minPingDelay = 5000;    // 5 seconds
        pingTimeout = 30000;    // 15 seconds ping timeout
        this.settings = settings;
    }

    @Override
    public void run() {
        while (threadState == ThreadState.Started) {    // while thread is supposed to be running
            // Run GUI tasks
            runTasks();

            // Read server messages
            try {
                handleServerMessages();
            } catch (ListenerException ex) {
                Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Sleep for 0.1 s
            try {
                sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    /**
     * Runs given task.
     * @param command task to run.
     */
    @Override
    public void execute(Runnable command) {
        command.run();
    }

    /**
     * Runs tasks assigned to us by the GUI.
     */
    private void runTasks() {
        while (!tasks.isEmpty()) {   // while there are tasks left to run, run them in FIFO order
            execute(tasks.peek()); // execute task
            tasks.remove(); // remove task
        }
    }

    /**
     * Parses and handles messages from the server.
     */
    private void handleServerMessages() throws ListenerException {
        try {
            // Listener must be on to listen for server messages, and GUI must be ready to accept callbacks
            if (listener == null) {// || !guiReady
                return;
            }

            String msg;
            while ((msg = listener.popQueueItem()) != null) {    // until queue is empty
                handleServerMessage(msg);
            }

            // Send ping message if ping timer expired
            sendPing();
        } catch (InterruptedException ex) {
            Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleServerMessage(String message) {
        String[] sentences = message.split("\t");   // regex for spaces and tabs
        String[] words = sentences[0].split(" ");   // regex for spaces
        String command = words[0];

        ServerMessage commandMatch = ServerMessage.valueOf(command);    // match command to a ServerMessage enum for faster comparisons in a switch

        switch (commandMatch) {
            case SERVERMSG:
                chatObserver.notifyBroadcastMessage("Official server", sentences[0]);
                break;
            case ACCEPTED:
                loginObserver.loginSuccess();
                break;
            case DENIED:
                loginObserver.loginFail(sentences[0]);
                break;
            case AGREEMENT:
                agreement.append(message.substring(9));
                break;
            case AGREEMENTEND:
                loginObserver.displayAgreement(new StringReader(agreement.toString()));
                break;
            case MOTD:
                chatObserver.messageOfTheDay(sentences[0]);
                break;
            case ADDUSER:
                // Add this user to the usersOnServer list
                usersOnServer.put(words[1], new User(words[1], words[2], Integer.valueOf(words[3])));
                // Tell chatobserver to update its list
                chatObserver.addUser(settings.getServerName(), usersOnServer.get(words[1]));
                break;
            case REMOVEUSER:
                // Remove form usersOnServer list
                usersOnServer.remove(words[1]);
                // Tell observer to remove
                chatObserver.removeUser(settings.getServerName(), usersOnServer.get(words[1]), "");
                break;
            case RING:
                System.out.println("Got a ring!");
                // Play "ring.wav"
                playSound("ring.wav");
                break;
            case JOINEDBATTLE:
                int battleUserJoined = Integer.valueOf(words[1]);
                String userJoined = words[2];

                // Set thatu ser is in battle
                usersOnServer.get(userJoined).setInBattle(battleUserJoined);

                // Add user to battle
                usersInBattles.get(battleUserJoined).add(usersOnServer.get(userJoined));

                // Update the number of users in that battle
                battlesOnServer.get(battleUserJoined).setNumPlayers(usersInBattles.get(battleUserJoined).size());

                // Notify battleListObserver of the change
                battleListObserver.updateBattle(battlesOnServer.get(battleUserJoined));

                // If this is our current battle, notify the battleObserver as well
                if (currentBattle == battleUserJoined) {
                    battleObserver.updateUser(usersOnServer.get(userJoined), usersOnServer.get(userJoined).getGameStatus());
                }
                break;
            case JOINFAILED:    // failed to join channel
                chatObserver.joinFailed("Failed to join the channel '" + words[1] + "'.");
                break;
            case REQUESTBATTLESTATUS:
                // Reply with our battle status
                sendMyBattleStatus(usersOnServer.get(username).getBattleStatus(), usersOnServer.get(username).getColor());
                break;
            case LEFTBATTLE:
                // Remove user from battle
                usersInBattles.get(Integer.valueOf(words[1])).remove(usersOnServer.get(words[2]));

                // Update the number of users in that battle
                battlesOnServer.get(Integer.valueOf(words[1])).setNumPlayers(usersInBattles.get(Integer.valueOf(words[1])).size());

                // Notify battleListObserver of the change
                battleListObserver.updateBattle(battlesOnServer.get(Integer.valueOf(words[1])));

                // If this is our current battle, notify the battleObserver as well
                if (currentBattle == Integer.valueOf(words[1])) {
                    battleObserver.removeUser(usersOnServer.get(words[2]));
                }
                break;
            case CLIENTBATTLESTATUS:
                // Get previous user mode
                GameStatus mode = usersOnServer.get(words[1]).getGameStatus();
                // Update user object
                usersOnServer.get(words[1]).setBattleStatus(Integer.valueOf(words[2]));
                usersOnServer.get(words[1]).setColor(Integer.valueOf(words[3]));

                // Notify the battleObserver about this update
                battleObserver.updateUser(usersOnServer.get(words[1]), mode);
                break;
            case BATTLEOPENED:
                // Add battle
                /* params:
                 * BATTLEOPENED BATTLE_ID type natType founder IP port
                maxplayers passworded rank maphash {map} {title} {modname}
                 */
                Battle newBattle = new Battle(Integer.valueOf(words[1]));
                newBattle.setType(Integer.valueOf(words[2]));
                newBattle.setNatType(Integer.valueOf(words[3]));
                newBattle.setCreatorName(words[4]);
                newBattle.setIp(words[5]);
                newBattle.setPort(Integer.valueOf(words[6]));
                newBattle.setMaxPlayers(Integer.valueOf(words[7]));
                newBattle.setPassworded(Integer.valueOf(words[8]));
                newBattle.setRankLimit(Integer.valueOf(words[9]));
                newBattle.setMapHash(Integer.valueOf(words[10]));
                newBattle.setMapName(words[11]);
                newBattle.setTitle(sentences[1]);
                newBattle.setModName(sentences[2]);

                // Put battle into battles list
                battlesOnServer.put(Integer.valueOf(words[1]), newBattle);

                // Create list of users in this battle
                usersInBattles.put(Integer.valueOf(words[1]), new ArrayList<User>());

                // Notify battlelist observer
                battleListObserver.addBattle(newBattle);

                // Set the creator users host
                usersOnServer.get(words[4]).setHostingBattleId(Integer.valueOf(words[1]));

                break;
            case BATTLECLOSED:
                Battle b = battlesOnServer.get(Integer.valueOf(words[1]));

                // Unset the creator as a host
                usersOnServer.get(b.getCreatorName()).setHostingBattleId(-1);

                // Notify battlelist observer
                battleListObserver.removeBattle(battlesOnServer.get(Integer.valueOf(words[1])));

                // Remove battle
                battlesOnServer.remove(Integer.valueOf(words[1]));

                // Notify battleListObserver of the change
                battleListObserver.removeBattle(battlesOnServer.get(Integer.valueOf(words[1])));
                break;
            case UPDATEBATTLEINFO:
                /* params:
                 * BATTLE_ID SpectatorCount locked maphash
                {mapname}
                 */
                battlesOnServer.get(Integer.valueOf(words[1])).setNumSpectators(Integer.valueOf(words[2]));
                battlesOnServer.get(Integer.valueOf(words[1])).setLocked(Integer.valueOf(words[3]));
                battlesOnServer.get(Integer.valueOf(words[1])).setMapHash(Integer.valueOf(words[4]));
                battlesOnServer.get(Integer.valueOf(words[1])).setMapName(words[5]);

                // Notify battleListObserver of the change
                battleListObserver.updateBattle(battlesOnServer.get(Integer.valueOf(words[1])));
                break;
            case JOINBATTLE:
                // We have joined a battle so notify the battleObserver
                int battleJoined = Integer.valueOf(words[1]);

                // Update our own user for battleObser
                battleObserver.setOwnUser(usersOnServer.get(username));

                // Display battle
                battleObserver.displayBattle(battlesOnServer.get(battleJoined));

                // Set our current battle
                currentBattle = battleJoined;
                break;
            case MYBATTLESTATUS:
                // Update our own user for battleObser
                battleObserver.setOwnUser(usersOnServer.get(username));
                break;
            case FORCEQUITBATTLE:
                // Set our current battle
                currentBattle = 0;
                // Notify battle observer
                battleObserver.kickedFromBattle();
                return;
            case LEAVEBATTLE:
                // Set our current battle
                currentBattle = 0;
                // battleObserver has handled everything on it's end
                break;
            case SETSCRIPTTAGS:
                // Todo handle 
                break;
            case JOINBATTLEFAILED:
                // We failed to join the battle
                battleListObserver.joinFailed(appendMessages(words, 1));
                // Remove as active battle
                currentBattle = 0;
                break;
            case CLIENTSTATUS:
                User user = usersOnServer.get(words[1]);
                // Modify user's status
                user.setStatus(Integer.valueOf(words[2]));

                // If this is our own user and inGame bit was set to 1, then we need to launch Spring
                if (words[1].equals(getUsername()) && usersOnServer.get(getUsername()).isInGame()) {
                    launchSpring();
                }

                // If this user is a game room host, his inGame status should affect the status of the room
                if (user.isHostingABattle()) {
                    if (user.isInGame()) {
                        // Set battle as started
                        battlesOnServer.get(user.getHostingBattleId()).setStarted();
                    } else {
                        // Set battle as started
                        battlesOnServer.get(user.getHostingBattleId()).setEnded();
                    }
                }
                break;
            case SAID:
                chatObserver.notifyChatMessage(words[1], words[2], appendMessages(words, 3), false);
                break;
            case SAIDEX:
                chatObserver.notifyChatMessage(words[1], words[2], appendMessages(words, 3), true);
                break;
            case SAIDPRIVATE:
                chatObserver.notifyPrivateChatMessage(getUsername(), words[1], appendMessages(words, 2), false);
                break;
            case SAYPRIVATE:
                chatObserver.notifyPrivateChatMessage(getUsername(), words[1], appendMessages(words, 2), true);
                break;
            case CHANNELMESSAGE:
                chatObserver.notifyBroadcastMessage(words[1], appendMessages(words, 2));
                break;
            case SAIDBATTLE:
                // Notify battleObserver of what was said
                battleObserver.said(words[1], appendMessages(words, 2));
                break;
            case SAIDBATTLEEX:
                // Notify battleObserver of what was said
                battleObserver.saidExpression(words[1], appendMessages(words, 2));
                break;
            case CHANNELTOPIC:
                // Set channel topic
                String topic = "";
                for (int i = 4; i < words.length; i++) {
                    // Append and replace all '\n' with a proper carriage return
                    topic += " " + words[i].replaceAll("\\\\n", "\n");
                }
                chatObserver.setTopic(words[1], words[2], Long.valueOf(words[3]), topic);
                break;
            case JOIN:
                chatObserver.openChatPanel(words[1]);
                break;
            case LEAVE:
                // This client command is never seen by the client(verify?)
                break;
            case JOINED:
                // Client joined channel
                // Notify chat observer
                chatObserver.addUser(words[1], usersOnServer.get(words[2]));
                break;
            case LEFT:
                String reason = appendMessages(words, 3);
                // Remove user
                chatObserver.removeUser(words[1], usersOnServer.get(words[2]), reason);
                break;
            case CLIENTS:
                // Add every client to channel
                for (int i = 2; i < words.length; i++) {
                    chatObserver.addUser(words[1], usersOnServer.get(words[i]));
                }
                break;
            case LOGININFOEND:
                isLoggedIn = true;
                break;
        }
    }

    /**
     * Plays sound file and blocks until finished.
     */
    private static void playSound(String soundFile) {
        /*
        Clip clip = null;
        AudioInputStream audioInputStream = null;
        try {
        audioInputStream = AudioSystem.getAudioInputStream(new File(soundFile));

        clip = AudioSystem.getClip();
        clip.open(audioInputStream);

        clip.start();
        clip.drain();
        } catch(Exception e ) {

        }
        finally {
        clip.close();
        try {
        audioInputStream.close();
        } catch (IOException ex) {
        Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
         * 
         */
    }

    private String appendMessages(String[] words, int startPos) {
        String text = "";
        for (int i = startPos; i < words.length; i++) {
            text += words[i] + " ";
        }
        return text;
    }

    private void sendMessage(String msg) throws IOException {
        byte[] tmp = new byte[msg.getBytes().length + 1];
        System.arraycopy(msg.getBytes(), 0, tmp, 0, msg.getBytes().length);
        tmp[tmp.length - 1] = 0x0a;
        socket.getOutputStream().write(tmp);
        socket.getOutputStream().flush();
        //socket.getOutputStream().flush();
    }

    /**
     * Public methods
     */
    @Override
    public void sayBattle(final String str) {
        tasks.add(new Runnable() {

            @Override
            public void run() {
                try {
                    sendMessage("SAYBATTLE " + str);
                } catch (IOException ex) {
                    Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    public void sayExpressionBattle(final String str) {
        tasks.add(new Runnable() {

            @Override
            public void run() {
                try {
                    sendMessage("SAYBATTLEEX " + str);
                } catch (IOException ex) {
                    Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    public void registerChatObserver(ChatObserver caller) {
        chatObserver = caller;
    }

    @Override
    public void listAllChannels() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void joinChannel(final String name) {
        tasks.add(new Runnable() {

            @Override
            public void run() {
                try {
                    // TODO add support for passworded channels
                    sendMessage("JOIN " + name);
                } catch (IOException ex) {
                    Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    public void registerAsLoginObserver(final LoginObserver caller) {
        tasks.add(new Runnable() {

            @Override
            public void run() {
                loginObserver = caller;
            }
        });
    }

    @Override
    public void login(final String username, final String passwordPlaintext) {
        this.username = username;
        final SessionController thisPtr = this;

        tasks.add(new Runnable() {

            @Override
            public void run() {
                ClientInfo clientObj = null;

                // Initialize client object
                try {
                    clientObj = new ClientInfo(username, passwordPlaintext);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
                }

                // Connect to server
                try {
                    socket = new Socket(settings.getServerHostname(), settings.getServerPort());

                    // Start to recieve messages from server
                    BufferedReader tmpIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String reply = tmpIn.readLine();

                    String substr = reply.substring(0, 9);
                    if (!substr.equals("TASServer")) {
                        loginObserver.loginFail("Could not connect to lobby server.");
                    }

                    // Prepare login message
                    String loginMsg = "LOGIN " + clientObj.getName() + " " + clientObj.getPasswordMd5Base64().replaceAll("\\r\\n", "") + " " + clientObj.getCpuFrequency() + " "
                            + clientObj.getLocalHost();

                    // Start listener thread
                    listener = new ListenerThread(thisPtr);
                    listener.setInputStream(tmpIn);
                    listener.start();

                    // Send login message
                    sendMessage(loginMsg);
                } catch (IOException ex) {
                    loginObserver.loginFail("Couldn't establish connection to " + settings.getServerHostname() + ":" + settings.getServerPort());
                    // throw new LoginException(ex.getCause().getMessage());
                }
            }
        });
    }

    @Override
    public void disconnect() {
        tasks.add(new Runnable() {

            @Override
            public void run() {
                try {
                    // TODO disconnect
                    listener.disconnect();
                    listener = null;
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
    }

    @Override
    public void register(final String username, final String passwordPlaintext) {
        tasks.add(new Runnable() {

            @Override
            public void run() {
                try {
                    socket = new Socket(settings.getServerHostname(), settings.getServerPort());

                    // Start to recieve messages from server
                    BufferedReader tmpIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String reply = tmpIn.readLine();

                    String substr = reply.substring(0, 9);
                    if (!substr.equals("TASServer")) {
                        loginObserver.loginFail("Could not connect to lobby server.");
                    }

                    String passwordHash = MD5Base64Hash.encrypt(passwordPlaintext).replaceAll("\\r\\n", "");

                    String registrationMsg = "REGISTER " + username + " " + passwordHash;

                    // System.out.println(registrationMsg);

                    sendMessage(registrationMsg);

                    // Listen for reply
                    reply = tmpIn.readLine();

                    if (reply.equals("REGISTRATIONACCEPTED")) {
                        loginObserver.registrationSuccess();
                    } else {  // if it failed
                        loginObserver.registrationFail(reply.substring(18));
                    }

                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    public void changePassword(String newPassword) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void guiIsReady() {
        tasks.add(new Runnable() {

            @Override
            public void run() {
                guiReady = true;
            }
        });
    }

    @Override
    public void say(String msg, String channel) {
        try {
            sendMessage("SAY " + channel + " " + msg);
        } catch (IOException ex) {
            Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sayPrivate(String username, String message) {
        try {
            sendMessage("SAYPRIVATE " + username + " " + message);
        } catch (IOException ex) {
            Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean isValidUsername(String username) {
        return usersOnServer.containsKey(username);
    }

    private void sendPing() {
        // Check that we are logged in
        if (!isLoggedIn) {
            return;
        }
        // We have received the reply for our last ping and at least minPingDelay time has passed since we last sent a ping
        if (ping && (System.currentTimeMillis() - lastPing) > minPingDelay) {
            try {
                // Send ping
                sendMessage("PING");

                // Set ping time
                lastPing = System.currentTimeMillis();

                // Don't ping until we get reply
                ping = false;
            } catch (IOException ex) {
                Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } // We never got a reply and pingTimeout time has passed
        else if (ping && (System.currentTimeMillis() - lastPing) > pingTimeout) {
            timeout();
        }
    }

    /**
     * Called when timeout has been discovered.
     */
    private void timeout() {
    }

    /**
     * Concurrent-safe method for setting lastPong.
     * @param value new value
     */
    public void setLastPong(long value) {
        // Assign value
        try {
            lastPongSemaphore.acquire();
            lastPong = value;
            lastPongSemaphore.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Give session controller a task to run
        tasks.add(new Runnable() {

            @Override
            public void run() {
                // Compute latency
                long latency = getLastPong() - lastPing;
                chatObserver.notifyLatency(latency);
                // Enable ping
                ping = true;
            }
        });
    }

    /**
     * Concurrent-safe method for getting lastPong.
     * @return 0 if failed, otherwise the value.
     */
    private long getLastPong() {
        try {
            lastPongSemaphore.acquire();
            long value = lastPong;
            lastPongSemaphore.release();
            return value;
        } catch (InterruptedException ex) {
            Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public void leaveChannel(final String name) {
        tasks.add(new Runnable() {

            @Override
            public void run() {
                try {
                    sendMessage("LEAVE " + name);
                } catch (IOException ex) {
                    Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    public void registerBattleListObserver(BattleListObserver battleListObserver) {
        this.battleListObserver = battleListObserver;
    }

    @Override
    public List<User> getUsersInBattle(int battleId) {
        return usersInBattles.get(battleId);
    }

    @Override
    public boolean joinBattle(final int battleId, final String psw) {
        tasks.add(new Runnable() {

            @Override
            public void run() {
                battlePswd = String.valueOf(username.hashCode());
                
                try {
                    if (psw == null) {
                        sendMessage("JOINBATTLE " + battleId + " " +" "+ battlePswd);
                    } else {  // send password too
                        sendMessage("JOINBATTLE " + battleId + " " + psw +" "+ battlePswd);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        return true;    // TODO always returns true for now
    }

    @Override
    public void leaveBattle() {
        tasks.add(new Runnable() {

            @Override
            public void run() {
                try {
                    sendMessage("LEAVEBATTLE");
                } catch (IOException ex) {
                    Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    public void registerAsBattleObserver(BattleRoomObserver battleObserver) {
        this.battleObserver = battleObserver;
    }

    @Override
    public void sendMyBattleStatus(final int battleStatus, final int color) {
        tasks.add(new Runnable() {

            @Override
            public void run() {
                try {
                    sendMessage("MYBATTLESTATUS " + battleStatus + " " + color);
                } catch (IOException ex) {
                    Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    public boolean isBattlePasswordRequired(int battleId) {
        // battlesOnServer is a synchronized map, so there won't be any risks in reading it from another thread
        return battlesOnServer.get(battleId).getPassworded() == 1;
    }

    private void launchSpring() {
        // Get battle info object
        Battle b = battlesOnServer.get(currentBattle);

        String scriptFilePath = "";
        // Write script.txt
        try {
            String ip = b.getIp();
            int port = b.getPort();
            scriptFilePath = settings.getSpringDataDirectory() + "/script.txt";
            new ScriptFile(ip, port, false, username, battlePswd, scriptFilePath);
        } catch (IOException ex) {
            Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Launch game
        try {
            String springExePath = settings.getSpringExePath() +" "+ scriptFilePath;
            System.out.println(springExePath);
            Runtime.getRuntime().exec(springExePath);
        } catch (IOException ex) {
            Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void startGame() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                System.out.println("Wanna be starting somethin'.");

                // Set our clientstatus
                usersOnServer.get(getUsername()).setInGame(true);

                // Broadcast our status
                broadcastOurStatus();

                // Launch game
                launchSpring();
            }
        });
    }

    private void broadcastOurStatus() {
        try {
            sendMessage("MYSTATUS " + usersOnServer.get(getUsername()).getStatus());
        } catch (IOException ex) {
            Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void acceptTOS() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    sendMessage("CONFIRMAGREEMENT");
                } catch (IOException ex) {
                    Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    public void joinSameBattleAsUser(final String username) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                int battleId = usersOnServer.get(username).getInBattle();
                if(battleId != -1) {
                    joinBattle(battleId, null);
                }
                // else user left the battle already
            }
        });
    }

    @Override
    public void rename(final String name) {
         SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    sendMessage("RENAMEACCOUNT " + name);
                    mainFrame.disconnect(username, name, "You have been automatically disconnected because your account name has changed.");
                } catch (IOException ex) {
                    Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public static long getMinPingDelay() {
        return minPingDelay;
    }
}
