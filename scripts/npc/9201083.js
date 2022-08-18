/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*

Glimmer Man (NLC); ITCG Forging Trader.
by: Nina
for: NobleStory

 */
function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {

         
    if (mode == -1) {
		cm.dispose();
	} else if (mode == 0) {
		cm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
{	
		if (status == 0) {
				cm.sendSimple("Hello. I am the Glimmer Man! \r\nWhich ITCG weapon would you like to trade for? #e#b\r\n#L0##i1302079# Astral Blade#l \r\n#L1##i1322059# Cosmic Scepter \r\n#L2##i1412032# Crescent Moon \r\n#L3##i1442060# Heavenly Messenger \r\n#L4##i1432045# Sunspear \r\n#L5##i1382053# Celestial Staff \r\n#L6##i1452052# Andromeda Bow \r\n#L7##i1462046# Void Hunter \r\n#L8##i1472062# Black Hole \r\n#L9##i1332064# Nebula Dagger (LUK) \r\n#L10##i1332065# Nebula Dagger (STR)#k#n");	 	
		} else if (status == 1){
			if (selection == 0) {
				if (cm.haveItem(4031761, 1)) {
				cm.gainItem(4031761, -1);
				cm.gainItem(1302079, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have a #e#rMateria Orb#n#k.");
					cm.dispose();
				}
			} else if (selection == 1) {
				if (cm.haveItem(4031761, 1)) {
				cm.gainItem(4031761, -1);
				cm.gainItem(1322059, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have a #e#rMateria Orb#n#k.");
					cm.dispose();
				}
			}
			 else if (selection == 2) {
				if (cm.haveItem(4031761, 1)) {
				cm.gainItem(4031761, -1);
				cm.gainItem(1412032, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have a #e#rMateria Orb#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 3) {
				if (cm.haveItem(4031761, 1)) {
				cm.gainItem(4031761, -1);
				cm.gainItem(1442060, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have a #e#rMateria Orb#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 4) {
				if (cm.haveItem(4031761, 1)) {
				cm.gainItem(4031761, -1);
				cm.gainItem(1432045, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have a #e#rMateria Orb#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 5) {
				if (cm.haveItem(4031761, 1)) {
				cm.gainItem(4031761, -1);
				cm.gainItem(1382053, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have a #e#rMateria Orb#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 6) {
				if (cm.haveItem(4031761, 1)) {
				cm.gainItem(4031761, -1);
				cm.gainItem(1452052, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have a #e#rMateria Orb#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 7) {
				if (cm.haveItem(4031761, 1)) {
				cm.gainItem(4031761, -1);
				cm.gainItem(1462046, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have a #e#rMateria Orb#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 8) {
				if (cm.haveItem(4031761, 1)) {
				cm.gainItem(4031761, -1);
				cm.gainItem(1472062, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have a #e#rMateria Orb#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 9) {
				if (cm.haveItem(4031761, 1)) {
				cm.gainItem(4031761, -1);
				cm.gainItem(1332064, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have a #e#rMateria Orb#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 10) {
				if (cm.haveItem(4031761, 1)) {
				cm.gainItem(4031761, -1);
				cm.gainItem(1332065, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have a #e#rMateria Orb#k#n.");
					cm.dispose();
				}
			}
		}
	} 
		}
	}