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
package net.cheesecan.cheeselobby.session;

import java.awt.Color;

/**
 *
 * @author jahziah
 */
public class User implements SessionObject {
    // main fields
    private String name;
    private int status;
    private String country;
    private int rank;
    private int experience;
    private int cpu;

    // hidden fields
    private boolean inGame;
    private boolean away;
    private boolean accessStatus;
    private boolean botMode;
    private int hostingBattleId;    // is this user hosting a battle, if so that battle's id is stored here
    private int inBattle;           // if we know this user is in a battle

    // battle fields
    private GameStatus gameStatus;
    private int battleStatus;
    private int faction;
    private Color color;
    private int team;
    private int ally;
    private int handicap;
    private boolean isSynced;

    public enum GameStatus {
        READY,
        UNREADY,
        SPECTATING,
        BUSY
    }
/*
    public User(int status, String country, String name, int rank, int experience, int cpu) {
        this.status = status;
        this.country = country;
        setName(name);
        this.rank = rank;
        this.experience = experience;
        this.cpu = cpu;
    }

 * 
 */
    public User(String name, String country, int cpu) {
        this.name = name;
        this.country = country;
        this.cpu = cpu;
        this.gameStatus = GameStatus.UNREADY;
        this.color = color.white;
        this.isSynced = true;
        this.hostingBattleId = -1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public int getCpu() {
        return cpu;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;

        // Is ingame or not, bit 0
        inGame = (status & 1) == 1;

        // Away status, bit 1
        away = ((status >> 1 ) & 1) == 1;

        // bits 2-4 is rank, so rank is composed of 3 bits in total
        rank = ((status >> 2 ) & 7);  // we plus by one since the 1st rank is 1 not 0

        // bit 5 is access status
        accessStatus = ((status >> 5 ) & 1) == 1;

        // bit 6 is bot mode
        botMode = ((status >> 6 ) & 1) == 1;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isAccessStatus() {
        return accessStatus;
    }

    public void setAccessStatus(boolean accessStatus) {
        this.accessStatus = accessStatus;
    }

    public int getAlly() {
        return ally;
    }

    public void setAlly(int ally) {
        this.ally = ally;
    }

    public boolean isAway() {
        return away;
    }

    public void setAway(boolean away) {
        this.away = away;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    /** -----battlestatus------
     *  b0 = undefined (reserved for future use)
    b1 = ready (0=not ready, 1=ready)
    b2..b5 = team no. (from 0 to 15. b2 is LSB, b5 is MSB)
    b6..b9 = ally team no. (from 0 to 15. b6 is LSB, b9 is MSB)
    b10 = mode (0 = spectator, 1 = normal player)
    b11..b17 = handicap (7-bit number. Must be in range 0..100). Note: Only host can
    change handicap values of the players in the battle (with HANDICAP command).
    These 7 bits are always ignored in this command. They can only be changed using
    HANDICAP command.
    b18..b21 = reserved for future use (with pre 0.71 versions these bits were used for
    team color index)
    b22..b23 = sync status (0 = unknown, 1 = synced, 2 = unsynced)
    b24..b27 = side (e.g.: arm, core, tll, ... Side index can be between 0 and 15,
    inclusive)
    b28..b31 = undefined (reserved for future use)
     */
    public int getBattleStatus() {
        int retval = 0;

        // Set b1 ready
        int ready = (gameStatus == GameStatus.READY) ? 1 : 0;
        retval |= (ready << 1);
        // Set b2..b5 team
        retval |= (team << 2);
        // set b6..b9 ally
        retval |= (ally << 6);
        // b10
        int mode = (gameStatus == GameStatus.SPECTATING) ? 0 : 1;
        retval |= (mode << 10);
        // b11..b17 handicap
        retval |= (handicap << 11);
        // b18..b21 reserved
        //b22..b23 sync status
        retval |= ((isSynced == true ? 1 : 0) << 22);
        // b24..b27 side
        retval |= (faction << 24);
        // b28..31 is reserved

        return retval;
    }

    public void setBattleStatus(int battleStatus) {
        this.battleStatus = battleStatus;

        // b0 is undefined/rserved for future use

        // b1 is ready boolean
        boolean ready = ((battleStatus >> 1) & 1) == 1;
        if(ready) {
            gameStatus = GameStatus.READY;
        }
        else {
            gameStatus = GameStatus.UNREADY;
        }
        
        // b2-5 is team
        team = (battleStatus >> 2) & 0xF;

        //b6-9 is ally
        ally = (battleStatus >> 6) & 0xF;

        // b10 is mode, spectator or player
        boolean isSpectator = ((battleStatus >> 10) & 1) == 0;
        if(isSpectator) {
            gameStatus = GameStatus.SPECTATING;
        }

        // b11-b17 is handicap
        handicap = (battleStatus >> 11) & 0x7F;

        // b18-21 reserved for future use

        // b22-23 is sync status
        isSynced = ((battleStatus >> 22) & 1) == 1;

        // b24-b27 is side
        faction = (battleStatus >> 24) & 0xF;

        // b28-31 is undefined/reserved for futureu se
    }

    public boolean isPlayer() {
        return gameStatus == GameStatus.UNREADY || gameStatus == GameStatus.READY;
    }

    public boolean isSpectator() {
        return gameStatus == GameStatus.SPECTATING;
    }

    public boolean isBotMode() {
        return botMode;
    }

    public void setBotMode(boolean botMode) {
        this.botMode = botMode;
    }

    public int getColor() {
        int retval = 0;
        retval = retval | (color.getRed() << 16);
        retval = retval | (color.getGreen() << 8);
        retval = retval | color.getBlue();
        return retval;
    }

    public Color getColorByColor() {
        return color;
    }

    public void setColor(int color) {
        // Determine color
        // RGB coding 1 byte red, 1 byte green, 1 byte blue
        int r = (color >> 16) & 0xFF;   // get 3rd byte
        int g = (color >> 8) & 0xFF;   // get 2nd byte
        int b = color & 0xFF;    // get first byte

        // make a color out of that
        Color c = new Color(r, g, b);

        this.color = c;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getFaction() {
        return faction;
    }

    public void setFaction(int faction) {
        this.faction = faction;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public int getHandicap() {
        return handicap;
    }

    public void setHandicap(int handicap) {
        this.handicap = handicap;
    }

    public boolean isIsSynced() {
        return isSynced;
    }

    public void setIsSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }

    public boolean isHostingABattle() {
        return hostingBattleId != -1;
    }

    public int getHostingBattleId() {
        return hostingBattleId;
    }

    public void setHostingBattleId(int hostingBattleId) {
        this.hostingBattleId = hostingBattleId;
    }

    @Override
    public Object getValueByIndex(int i) {
        switch(i) {
            case 0:
                return inGame;
            case 1:
                return getName();
            case 2:
                return country;
            case 3:
                return rank;
        }
        return "Empty";
    }

    public Object getBattleTableValueByIndex(int i) {
        // Status, ingame, faction, color, country, rank, name, team, ally, cpu
        switch(i) {
            case 0:
                return gameStatus;
            case 1:
                return inGame;
            case 2:
                return faction;
            case 3:
                return color;
            case 4:
                return country;
            case 5:
                return rank;
            case 6:
                return name;
            case 7:
                return team;
            case 8:
                return ally;
            case 9:
                return cpu;
        }
        return 1;
    }

    public int getInBattle() {
        return inBattle;
    }

    public void setInBattle(int inBattle) {
        this.inBattle = inBattle;
    }

    public void setUserNotInBattle() {
        setInBattle(-1);
    }

}
