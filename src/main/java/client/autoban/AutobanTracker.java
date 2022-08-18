/*
    This file is part of the Noblestory MapleStory Server
    Copyleft (L) 2019 Saffron

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package client.autoban;

/**
 *
 * @author Saffron
 * @date 6/18/2019
 */
public class AutobanTracker {
    
    private int attacksWithoutHit = 0;
    
//        to do acc hack
//        double accuracy = (chr.getDex() * 0.8) + (chr.getLuk() * 0.5);
//        double maxAcc = accuracy + 20 + 100; // this accounts for the max weapon mastery and the use item Maple Pop
//        int lvDiff = mob.getLevel() - chr.getLevel();
//        double accNeeded = (55 + 2 * lvDiff)* mob.getAvoid() / 15; // calculate how much acc is needed for the monster
//        
//       compare hit ratio to damage dealt to monster
    
    /*
    returns amount of consecutive times a player has done damage but not taken damage
    */
    public int getAttacksWithoutHit() {
        return attacksWithoutHit;
    }
    
    /*
    set amount of times player has dealt damage but not taken damage
    */
    public void setAttacksWithoutHit(int attacksWithoutHit) {
        this.attacksWithoutHit = attacksWithoutHit;
    }
}
