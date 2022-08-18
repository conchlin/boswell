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

Corine(NLC); ITCG Forging Trader.
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
				cm.sendSimple("Hello. If you have a Taru Weapon, I can upgrade it for you. Which ITCG weapon would you like to upgrade to?#e#b\r\n#L0##i1452055# Akhasuma#l \r\n#L1##i1452056# Akhamagna\r\n#L2##i1472066# Kumasuma \r\n#L3##i1472067# Kumamagna \r\n#L4##i1332068# Makusuma (LUK) \r\n#L5##i1332069# Makumagna (LUK) \r\n#L6##i1332071# Makusuma (STR) \r\n#L7##i1332072# Makumagna (STR) \r\n#L8##i1462048# Xarusuma \r\n#L9##i1462049# Xarumagna \r\n#L10##i1382055# Umarusuma \r\n#L11##i1382056# Umarumagna#k#n");	 	
		} else if (status == 1){
			if (selection == 0) {
				if ((cm.haveItem(1452054, 1)) &&(cm.haveItem(4031936, 1)) &&
				(cm.haveItem(4031937, 10)) &&(cm.haveItem(4005001, 5))) {
				cm.gainItem(4031936, -1);
				cm.gainItem(4031937, -10);
				cm.gainItem(4005001, -5);
				cm.gainItem(1452054, -1);
				cm.gainItem(1452055, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #r#e1 Akha, 1 Taru Spririt Feather, 5 Wisdom Crystals, and 10 Jungle Lilies#n#k.");
					cm.dispose();
				}
			} else if (selection == 1) {
				if ((cm.haveItem(1452055, 1)) && (cm.haveItem(4031936, 2)) &&(cm.haveItem(4031937, 50)) &&
				(cm.haveItem(4005001, 15))) {
				cm.gainItem(4031936, -2);
				cm.gainItem(4031937, -50);
				cm.gainItem(4005001, -15);
				cm.gainItem(1452055, -1);
				cm.gainItem(1452056, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #r#e1 Akhasuma, 2 Taru Spirit Feathers, 15 Wisdom Crystals, and 50 Jungle Lilies#k#n.");
					cm.dispose();
				}
			}
			 else if (selection == 2) {
				if ((cm.haveItem(1472065, 1)) && (cm.haveItem(4031936, 1)) &&
				(cm.haveItem(4031937, 10)) &&
				(cm.haveItem(4005001, 5))) {
				cm.gainItem(4031936, -1);
				cm.gainItem(4031937, -10);
				cm.gainItem(4005001, -5);
				cm.gainItem(1472065, -1);
				cm.gainItem(1472066, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Kuma, 1 Taru Spirit Feather, 5 Wisdom Crystals, and 10 Jungle Lilies#k#n.");
					cm.dispose();
				}
			} else if (selection == 3) {
				if ((cm.haveItem(1472066, 1)) && (cm.haveItem(4031936, 2)) &&
				(cm.haveItem(4031937, 50)) &&
				(cm.haveItem(4005001, 15))) {
				cm.gainItem(4031936, -2);
				cm.gainItem(4031937, -50);
				cm.gainItem(4005001, -15);
				cm.gainItem(1472066, -1);
				cm.gainItem(1472067, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Kumasuma, 2 Taru Spirit Feathers, 15 Wisdom Crystals, 50 Jungle Lilies#n#k.");
					cm.dispose();
				}
			} else if (selection == 4) {
				if ((cm.haveItem(1332067, 1)) && (cm.haveItem(4031936, 1)) &&
				(cm.haveItem(4031937, 10)) &&
				(cm.haveItem(4005001, 5))) {
				cm.gainItem(4031936, -1);
				cm.gainItem(4031937, -10);
				cm.gainItem(4005001, -5);
				cm.gainItem(1332067, -1);
				cm.gainItem(1332068, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r 1 Maku (LUK), 1 Taru Spirit Feather, 5 Wisdom Crystals, and 10 Jungle Lilies#k#n.");
					cm.dispose();
				}
			}
			 else if (selection == 5) {
				if ((cm.haveItem(1332068, 1)) && (cm.haveItem(4031936, 2)) &&
				(cm.haveItem(4031937, 50)) &&
				(cm.haveItem(4005001, 15))) {
				cm.gainItem(4031936, -2);
				cm.gainItem(4031937, -50);
				cm.gainItem(4005001, -15);
				cm.gainItem(1332068, -1);
				cm.gainItem(1332069, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Makusuma (LUK), 2 Taru Spirit Feathers, 15 Wisdom Crystals, and 50 Jungle Lilies#k#n.");
					cm.dispose();
				}
			} else if (selection == 6) {
				if ((cm.haveItem(1332070, 1)) && (cm.haveItem(4031936, 1)) &&
				(cm.haveItem(4031937, 10)) &&
				(cm.haveItem(4005001, 5))) {
				cm.gainItem(4031936, -1);
				cm.gainItem(4031937, -10);
				cm.gainItem(4005001, -5);
				cm.gainItem(1332070, -1);
				cm.gainItem(1332071, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Maku (STR), 1 Taru Spirit Feather, 5 Wisdom Crystals, and 10 Jungle Lilies#k#n.");
					cm.dispose();
				}
			} else if (selection == 7) {
				if ((cm.haveItem(1332071, 1)) && (cm.haveItem(4031936, 2)) &&
				(cm.haveItem(4031937, 50)) &&
				(cm.haveItem(4005001, 15))) {
				cm.gainItem(4031936, -2);
				cm.gainItem(4031937, -50);
				cm.gainItem(4005001, -15);
				cm.gainItem(1332071, -1);
				cm.gainItem(1332072, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Makusuma (STR), 2 Taru Spirit Feathers, 15 Wisdom Crystals, and 50 Jungle Lilies#k#n.");
					cm.dispose();
				}
			} else if (selection == 8) {
				if ((cm.haveItem(1462047, 1)) && (cm.haveItem(4031936, 1)) &&
				(cm.haveItem(4031937, 10)) &&
				(cm.haveItem(4005001, 5))) {
				cm.gainItem(4031936, -1);
				cm.gainItem(4031937, -10);
				cm.gainItem(4005001, -5);
				cm.gainItem(1462047, -1);
				cm.gainItem(1462048, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Xaru, 1 Taru Spirit Feather, 5 Wisdom Crystals, and 10 Jungle Lilies#k#n.");
					cm.dispose();
				}
			}
			 else if (selection == 9) {
				if ((cm.haveItem(1462048, 1)) && (cm.haveItem(4031936, 2)) &&
				(cm.haveItem(4031937, 50)) &&
				(cm.haveItem(4005001, 15))) {
				cm.gainItem(4031936, -2);
				cm.gainItem(4031937, -50);
				cm.gainItem(4005001, -15);
				cm.gainItem(1462048, -1);
				cm.gainItem(1462049, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Xarusuma, 2 Taru Spirit Feathers, 15 Wisdom Crystals, and 50 Jungle Lilies#k#n.");
					cm.dispose();
				}
			} else if (selection == 10) {
				if ((cm.haveItem(1382054, 1)) && (cm.haveItem(4031936, 1)) &&
				(cm.haveItem(4031937, 10)) &&
				(cm.haveItem(4005001, 5))) {
				cm.gainItem(4031936, -1);
				cm.gainItem(4031937, -10);
				cm.gainItem(4005001, -5);
				cm.gainItem(1382054, -1);
				cm.gainItem(1382055, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Umaru, 1 Taru Spirit Feather, 5 Wisdom Crystals, and 10 Jungle Lilies#k#n.");
					cm.dispose();
				}
			} else if (selection == 11) {
				if ((cm.haveItem(1382055, 1)) && (cm.haveItem(4031936, 2)) &&
				(cm.haveItem(4031937, 50)) &&
				(cm.haveItem(4005001, 15))) {
				cm.gainItem(4031936, -2);
				cm.gainItem(4031937, -50);
				cm.gainItem(4005001, -15);
				cm.gainItem(1382055, -1);
				cm.gainItem(1382056, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Umarusuma, 2 Taru Spirit Feathers, 15 Wisdom Crystals, and 50 Jungle Lilies#k#n.");
					cm.dispose();
				}
			}
				}
			}
		}
	} 
