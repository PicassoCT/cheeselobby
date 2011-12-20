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

package net.cheesecan.cheeselobby.tables;

import java.util.List;
import net.cheesecan.cheeselobby.session.User;

/**
 *
 * @author jahziah
 */
public class BattleUserTableModel extends LobbyTableModel<User> {

    public BattleUserTableModel(List<User> users, String[] columnNames) {
        super(users, columnNames, 1);   // sort by status by default
    }

    @Override
    public Object getValueAt(int row, int column) {
        return ((User)getData().get(row)).getBattleTableValueByIndex(column);
    }

}
