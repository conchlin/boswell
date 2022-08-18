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

Professor Foxwit (NLC); ITCG Forging Trader.
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
				cm.sendSimple("Hello. What would you like to do?#e#b\r\n#L8#Create Forging Manuals \r\n#L9#Forge Equipment#k#n");
			}
		else if (status == 1) {
			if (selection == 8) {
			cm.sendSimple("Which forging manual would you like to create?#e#b\r\n#L0##i4031829# Black Phoenix Shield Forging Manual#l \r\n#L1##i4031828# Dark Shard Earrings Forging Manual \r\n#L2##i4031827# Sirius Cloak Forging Manual \r\n#L3##i4031826# Zeta Cape Forging Manual#k#n");
		} else if (selection == 9) {
			cm.sendSimple("Which ITCG equip would you like to create?#e#b\r\n#L4##i1092052# Black Phoenix Shield \r\n#L5##i1032049# Dark Shards \r\n#L6##i1102145# Sirius Cloak \r\n#L7##i1102146# Zeta Cape#k#n");
		}
		}
			else if (status == 2){
			if (selection == 0) {
				if ((cm.haveItem(4031751, 1)) &&
				(cm.haveItem(4031753, 1)) &&
				(cm.haveItem(4031754, 1))) {
				cm.gainItem(4031751, -1);
				cm.gainItem(4031753, -1);
				cm.gainItem(4031754, -1);
				cm.gainItem(4031829, 1);
				cm.dispose();
				} else {
					cm.sendOk("You don't have at least #e#r1 Vorticular Gyro, 1 Zeta Residue, and 1 Black Versal Materia#k#n.");
					cm.dispose();
				}
			} else if (selection == 1) {
				if ((cm.haveItem(4031753, 1)) &&
				(cm.haveItem(4031752, 1))) {
				cm.gainItem(4031752, -1);
				cm.gainItem(4031753, -1);
				cm.gainItem(4031828, 1);
				cm.dispose();
				} else {
					cm.sendOk("You don't have at least #e#r1 Zeta Residue and 1 Blinking Dingbat#k#n.");
					cm.dispose();
				}
			}
			 else if (selection == 2) {
				if ((cm.haveItem(4031752, 1)) &&
				(cm.haveItem(4031750, 1)) &&
				(cm.haveItem(4031754, 1))) {
				cm.gainItem(4031752, -1);
				cm.gainItem(4031750, -1);
				cm.gainItem(4031754, -1);
				cm.gainItem(4031827, 1);
				cm.dispose();
				} else {
					cm.sendOk("You don't have at least #e#r1 Blinking Dingbat, 1 Dark Matter, and 1 Black Versal Materia#n#k.");
					cm.dispose();
				}
			}
			else if (selection == 3) {
				if ((cm.haveItem(4031750, 1)) &&
				(cm.haveItem(4031751, 1))) {
				cm.gainItem(4031751, -1);
				cm.gainItem(4031750, -1);
				cm.gainItem(4031826, 1);
				cm.dispose();
				} else {
					cm.sendOk("You don't have at least #e#r1 Dark Matter and 1 Vorticular Gyro#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 4) {
				if ((cm.haveItem(4031829, 1)) &&
				(cm.haveItem(4011002 , 10)) &&
				(cm.haveItem(4021005, 2)) && 
				(cm.haveItem(4004004, 1))) {
				cm.gainItem(4031829, -1);
				cm.gainItem(4011002, -10);
				cm.gainItem(4021005, -2);
				cm.gainItem(4004004, -1);
				cm.gainItem(1092052, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You don't have at least #r#e1 Black Phoenix Shield Forging Manual, 10 Mithril Plates, 2 Sapphires, and 1 Dark Crystal Ores#k#n.");
					cm.dispose();
				}
			} 
			else if (selection == 5) {
			if ((cm.haveItem(4031828, 1)) &&
				(cm.haveItem(4020008, 10) &&
				(cm.haveItem(4004000, 10)))) {
				cm.gainItem(4031828, -1);
				cm.gainItem(4020008, -10);
				cm.gainItem(4004000, -10);
				cm.gainItem(1032049, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You don't have at least #e#r1 Dark Shard Earrings Forging Manual, 10 Black Crystal Ores, and 10 Power Crystal Ores.#n#k");
					cm.dispose();
				}
			}
			else if (selection == 6) {
				if ((cm.haveItem(4031827, 1)) &&
				(cm.haveItem(4011006, 5)) &&
				(cm.haveItem(4021005, 5)) && 
				(cm.haveItem(4021007, 2))) {
				cm.gainItem(4031827, -1);
				cm.gainItem(4011006, -5);
				cm.gainItem(4021005, -5);
				cm.gainItem(4021007, -2);
				cm.gainItem(1102145, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You don't have at least #r#e1 Sirius Cloak Forging Manual, 5 Gold Plates, 5 Sapphires, and 2 Diamonds#n#k.");
					cm.dispose();
				}
			}
			else if (selection == 7) {
				if ((cm.haveItem(4031826, 1)) &&
				(cm.haveItem(4004000, 5)) &&
				(cm.haveItem(4010006, 5)) && 
				(cm.haveItem(4021004, 5))) {
				cm.gainItem(4031826, -1);
				cm.gainItem(4004000, -5);
				cm.gainItem(4010006, -5);
				cm.gainItem(4021004, -5);
				cm.gainItem(1102146, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You don't have at least #e#r1 Zeta Cape Forging Manual, 5 Power Crystal Ores, 5 Gold Ores, and 5 Opals#k#n.");
					cm.dispose();
				}
			}
				}
			}
		}
	} 