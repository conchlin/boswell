/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

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

package server.expeditions;

/**
*
* @author Alan (SharpAceX)
*/

public enum MapleExpeditionType {

    BALROG_EASY(3, 30, 50, 255, 20),
    BALROG_NORMAL(6, 30, 50, 255, 20),
    SCARGA(3, 30, 80, 255, 20),
    SHOWA(3, 30, 100, 255, 20),
    ZAKUM(3, 30, 50, 255, 20),
    //PINKZAKUM(4, 30, 150, 255, 20),
    //VONLEON(6, 30, 110, 255, 20),
    HORNTAIL(6, 30, 100, 255, 20),
    CHAOS_ZAKUM(6, 30, 120, 255, 20),
    CHAOS_HORNTAIL(6, 30, 120, 255, 20),
    ARIANT(2, 7, 20, 30, 20),
    ARIANT1(2, 7, 20, 30, 20),
    ARIANT2(2, 7, 20, 30, 20),
    PINKBEAN(6, 30, 120, 255, 20),
    CWKPQ(6, 30, 90, 255, 20);   // CWKPQ min-level 90, found thanks to Cato
    
    private int minSize;
    private int maxSize;
    private int minLevel;
    private int maxLevel;
    private int registrationTime;
        
    private MapleExpeditionType(int minSize, int maxSize, int minLevel, int maxLevel, int minutes) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.registrationTime = minutes;
    }

    public int getMinSize() {
    	return minSize;
    }
    
    public int getMaxSize() {
        return maxSize;
    }
    
    public int getMinLevel() {
    	return minLevel;
    }
    
    public int getMaxLevel() {
    	return maxLevel;
    }
    
    public int getRegistrationTime(){
    	return registrationTime;
    }
}
