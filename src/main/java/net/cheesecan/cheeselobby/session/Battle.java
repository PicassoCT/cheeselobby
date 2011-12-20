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

import net.cheesecan.cheeselobby.session.Battle.BattleStatus;

/**
 *
 * @author jahziah
 */
public class Battle implements SessionObject {

    public enum BattleStatus {

        open,
        openAndStarted,
        closed,
        closedAndStarted,
    }
    // Fields
    private final int battleId;
    private int type;
    private int natType;
    private String ip;
    private int port;
    private int maxPlayers;
    private boolean passworded;
    private int mapHash;
    private String mapName;
    private String title;
    private String modName;
    private BattleStatus status;
    private String creatorName;
    private int rankLimit;
    private int numPlayers;
    private int numSpectators;
    private String description;

    /*
     * Use this constructor to initialize a battle from the battleopened command.
     * All parameters are in the order received by that server message.
     */
    public Battle(int battleId) {
        this.battleId = battleId;
        status = BattleStatus.open;
    }

    @Override
    public Object getValueByIndex(int i) {
        // "Status", "Name", "Creator", "Rank limit", "Mod", "Map", "Players", "Spectators"}
        switch (i) { // TODO optimize n if-cases to one array lookup
            case 0:
                return status;
            case 1:
                return passworded;
            case 2:
                return title;
            case 3:
                return creatorName;
            case 4:
                return rankLimit;
            case 5:
                return modName;
            case 6:
                return mapName;
            case 7:
                return numPlayers;
            case 8:
                return numSpectators;
            default:
                return "Invalid index.";
        }
    }

    public int getBattleId() {
        return battleId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getMapHash() {
        return mapHash;
    }

    public void setMapHash(int mapHash) {
        this.mapHash = mapHash;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getModName() {
        return modName;
    }

    public void setModName(String modName) {
        this.modName = modName;
    }

    public int getNatType() {
        return natType;
    }

    public void setNatType(int natType) {
        this.natType = natType;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public void setNumPlayers(int numPlayers) {
        int i = numPlayers - getNumSpectators();
        this.numPlayers = i < 0 ? 0 : i;

        // check if battle is closed
        battleIsLocked();
    }

    public int getNumSpectators() {
        return numSpectators;
    }

    public void setNumSpectators(int numSpectators) {
        this.numSpectators = numSpectators;

        // check if battle is closed
        battleIsLocked();
    }

    public int getPassworded() {
        return passworded ? 1 : 0;
    }

    public void setPassworded(int passworded) {
        if (passworded == 1) {
            this.passworded = true;
        } else {
            this.passworded = false;
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getRankLimit() {
        return rankLimit;
    }

    public void setRankLimit(int rankLimit) {
        this.rankLimit = rankLimit;
    }

    private void battleIsLocked() {
        if ((numPlayers) >= maxPlayers) {
            if(isStarted()) {
            status = BattleStatus.closedAndStarted;
            }
            else {
                status = BattleStatus.closed;
            }
        }
    }

    public BattleStatus getStatus() {
        return status;
    }

    public void setStatus(BattleStatus status) {
        this.status = status;
    }

    private boolean isOpen() {
        return status == BattleStatus.open;
    }

    private boolean isStarted() {
        return status == BattleStatus.openAndStarted || status == BattleStatus.closedAndStarted;
    }

    public void setStarted() {
        if (isOpen()) {
            status = BattleStatus.openAndStarted;
        } else {
            status = BattleStatus.closedAndStarted;
        }
    }

    public void setEnded() {
        if (isOpen()) {
            status = BattleStatus.open;
        } else {
            status = BattleStatus.closed;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLocked(int booleanValue) {
        if (booleanValue == 1) {
            status = BattleStatus.closed;
        } else {
            status = BattleStatus.open;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Battle other = (Battle) obj;
        if (this.battleId != other.battleId) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return battleId;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
