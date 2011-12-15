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

package net.cheesecan.cheeselobby.ui.components;

import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.ImageIcon;
import net.cheesecan.cheeselobby.session.Battle.BattleStatus;
import net.cheesecan.cheeselobby.session.User.GameStatus;

/**
 *
 * @author jahziah
 */
public class LobbyIcons {

    private final Map<String, ImageIcon> countryIcons;
    private final Map<String, ImageIcon> rankIcons;
    private final Map<String, ImageIcon> statusIcons;
    private final Map<String, ImageIcon> playerStatusIcons;
    private final Map<String, ImageIcon> battleStatusIcons;

    public LobbyIcons() {
        // Generate country icons
        countryIcons = new HashMap<String, ImageIcon>();
        String[] countries = Locale.getISOCountries();
        for (String country : countries) {
            URL url = getClass().getResource("/img/flags/" + country.toLowerCase() + ".png");
            if (url != null) {
                countryIcons.put(country.toLowerCase(), new ImageIcon(url));
            }
        }
        // Insert ?? icon
        countryIcons.put("??", new ImageIcon(getClass().getResource("/img/flags/__.png")));

        // Generate rank icons
        rankIcons = new HashMap<String, ImageIcon>();
        for(int i = 0; i <= 7; i++) {
            URL url = getClass().getResource("/img/ranks/" + i + ".gif");
            rankIcons.put(String.valueOf(i), new ImageIcon(url));
        }

        // Initialize status icons
        statusIcons = new HashMap<String, ImageIcon>();
        statusIcons.put(GameStatus.BUSY.name().toLowerCase(), new ImageIcon(getClass().getResource("/img/status/busy.png")));
        statusIcons.put(GameStatus.READY.name().toLowerCase(), new ImageIcon(getClass().getResource("/img/status/ready.png")));
        statusIcons.put(GameStatus.UNREADY.name().toLowerCase(), new ImageIcon(getClass().getResource("/img/status/unready.png")));
        statusIcons.put(GameStatus.SPECTATING.name().toLowerCase(), new ImageIcon(getClass().getResource("/img/status/spectating.png")));

        // Initialize battle status icons
        battleStatusIcons = new HashMap<String, ImageIcon>();
        battleStatusIcons.put(BattleStatus.open.name().toLowerCase(), new ImageIcon(getClass().getResource("/img/icons/battle/open.png")));
        battleStatusIcons.put(BattleStatus.openAndStarted.name().toLowerCase(), new ImageIcon(getClass().getResource("/img/icons/battle/openandstarted.png")));
        battleStatusIcons.put(BattleStatus.closed.name().toLowerCase(), new ImageIcon(getClass().getResource("/img/icons/battle/closed.png")));
        battleStatusIcons.put(BattleStatus.closedAndStarted.name().toLowerCase(), new ImageIcon(getClass().getResource("/img/icons/battle/closedandstarted.png")));
    
        // Player icons
        playerStatusIcons = new HashMap<String, ImageIcon>();
        playerStatusIcons.put(Boolean.toString(true), new ImageIcon(getClass().getResource("/img/icons/player/in-game.png")));
        playerStatusIcons.put(Boolean.toString(false), new ImageIcon(getClass().getResource("/img/icons/player/available.png")));
    }

    public ImageIcon getCountryIcon(String countryISOCode) {
        return countryIcons.get(countryISOCode.toLowerCase());
    }

    public ImageIcon getRankIcon(int rankNr) {
        return rankIcons.get(rankNr);
    }

    public Map<String, ImageIcon> getCountryIcons() {
        return countryIcons;
    }

    public Map<String, ImageIcon> getRankIcons() {
        return rankIcons;
    }

    public Map<String, ImageIcon> getStatusIcons() {
        return statusIcons;
    }

    public Map<String, ImageIcon> getPlayerStatusIcons() {
        return playerStatusIcons;
    }

    public Map<String, ImageIcon> getBattleStatusIcons() {
        return battleStatusIcons;
    }

    
}
