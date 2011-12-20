/*
 *  Copyright 2011 Jahziah Wagner.
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

/**
 *
 * @author jahziah
 */
public enum Rank {

    Unknown,
    Newbie,
    Beginner,
    Average,
    Above_average,
    Experienced,
    Highly_experienced,
    Veteran,
    Badly_needs_to_get_laid;

    @Override
    public String toString() {
            return name().replace("_", " ");
        }

    public static Rank valueOf(int i) {
        switch(i) {
            case 1:
                return Newbie;
            case 2:
                return Beginner;
            case 3:
                return Average;
            case 4:
                return Experienced;
            case 5:
                return Highly_experienced;
            case 6:
                return Veteran;
            case 7:
                return Badly_needs_to_get_laid;
            default:
                return Unknown;
        }
    }
}
