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

Fiona (Phantom Forest); ITCG Forging Trader.
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
		if (cm.haveItem(3992040)) {
				cm.sendSimple("Hello. I am Fiona! Which ITCG item would you like to forge?#e#b\r\n#L0##i2070018# Balanced Fury#l \r\n#L1##i1382060# Crimson Arcanon \r\n#L2##i1442068# Crimson Arcglaive \r\n#L3##i1452060# Crimson Arclancer \r\n#L4##i1332079# Dawn Raven's Beak \r\n#L5##i1332080# Dusk Raven's Beak \r\n#L6##i1332078# Night Raven's Beak \r\n#L7##i1472074# Dawn Raven's Claw \r\n#L8##i1472075# Dusk Raven's Claw \r\n#L9##i1472073# Night Raven's Claw \r\n#L10##i1462054# Dawn Raven's Eye \r\n#L11##i1462055# Dusk Raven's Eye \r\n#L12##i1462053# Night Raven's Eye \r\n#L13##i1402050# Dawn Raven's Wing \r\n#L14##i1402051# Dusk Raven's Wing \r\n#L15##i1402049# Night Raven's Wing #k#n");	 	
		} else {
			cm.sendOk("Prove to us that you have what it takes to be a member of the Raven Ninja Clan...");
			cm.dispose();
		}} else if (status == 1){
			if (selection == 0) {
				if ((cm.haveItem(4021008, 100)) && (cm.haveItem(4032015, 1)) && (cm.haveItem(4032016, 1)) && (cm.haveItem(4032017, 1)) && (cm.haveItem(4032005, 30))) {
				cm.gainItem(4021008, -100);
				cm.gainItem(4032015, -1);
				cm.gainItem(4032016, -1);
				cm.gainItem(4032017, -1);
				cm.gainItem(4032005, -30);
				cm.gainItem(2070018, 1);
				cm.dispose();
				} else {
					cm.sendOk("You do not have #e#r100 Black Crystals, 1 Tao of Harmony, 1 Tao of Sight, 1 Tao of Shadows, and 30 Typhon Feathers#n#k.");
					cm.dispose();
				}
			} else if (selection == 1) {
				if ((cm.haveItem(4032012, 30)) && (cm.haveItem(4032004, 400)) && (cm.haveItem(4032017, 1)) && (cm.haveItem(4032016, 1)) && (cm.haveItem(4032005, 10)) && (cm.haveItem(4005001, 4))) {
				cm.gainItem(4032012, -30);
				cm.gainItem(4032004, -400);
				cm.gainItem(4032017, -1);
				cm.gainItem(4032016, -1);
				cm.gainItem(4032005, -10);
				cm.gainItem(4005001, -4);
				cm.gainItem(1382060, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #r#e30 Crimson Hearts, 400 Crimson Wood, 1 Tao of Harmony, 1 Tao of Sight, 10 Typhon Feathers, and 4 Wisdom Crystals#k#n.");
					cm.dispose();
				}
			}
			 else if (selection == 2) {
				if ((cm.haveItem(4032012, 20)) && (cm.haveItem(4032004, 500)) && (cm.haveItem(4032017, 1)) && (cm.haveItem(4032015, 1)) && (cm.haveItem(4032005, 40)) && (cm.haveItem(4005000, 4))) {
				cm.gainItem(4032012, -20);
				cm.gainItem(4032004, -500);
				cm.gainItem(4032017, -1);
				cm.gainItem(4032015, -1);
				cm.gainItem(4032005, -40);
				cm.gainItem(4005000, -4);
				cm.gainItem(1442068, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r20 Crimson Hearts, 600 Crimson Wood, 4 Power Crystals, 1 Tao of Harmony, 1 Tao of Shadows, and 40 Typhon Feathers#k#n.");
					cm.dispose();
			}
			} else if (selection == 3) {
				if ((cm.haveItem(4032012, 10)) && (cm.haveItem(4032004, 300)) && (cm.haveItem(4032016, 1)) && (cm.haveItem(4032015, 1)) && (cm.haveItem(4032005, 75)) && (cm.haveItem(4005002, 4))) {
				cm.gainItem(4032012, -10);
				cm.gainItem(4032004, -300);
				cm.gainItem(4032016, -1);
				cm.gainItem(4032015, -1);
				cm.gainItem(4032005, -75);
				cm.gainItem(4005002, -4);
				cm.gainItem(1452060, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r10 Crimson Hearts, 300 Crimson Wood, 4 DEX Crystals, 1 Tao of Shadows, 1 Tao of Sight, and 75 Typhon Feathers#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 4) {
				if ((cm.haveItem(4021008, 20)) && (cm.haveItem(4005000, 5)) && (cm.haveItem(1332077, 1)) && (cm.haveItem(4032016, 1))) {
				cm.gainItem(4021008, -20);
				cm.gainItem(4005000, -5);
				cm.gainItem(1332077, -1);
				cm.gainItem(4032016, -1);
				cm.gainItem(1332079, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #r#e20 Black Crystals, 5 Power Crystals, 1 Raven's Beak, and 1 Tao of Sight#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 5) {
				if ((cm.haveItem(4021008, 30)) && (cm.haveItem(4005000, 5)) && (cm.haveItem(1332077, 1)) && (cm.haveItem(4032017, 1))) {
				cm.gainItem(4021008, -30);
				cm.gainItem(4005000, -5);
				cm.gainItem(1332077, -1);
				cm.gainItem(4032017, -1);
				cm.gainItem(1332080, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #r#e30 Black Crystals, 5 Power Crystals, 1 Raven's Beak, and 1 Tao of Harmony#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 6) {
				if ((cm.haveItem(4021008, 20)) && (cm.haveItem(4005003, 5)) && (cm.haveItem(1332077, 1)) && (cm.haveItem(4032015, 1))) {
				cm.gainItem(4021008, -20);
				cm.gainItem(4005003, -5);
				cm.gainItem(1332077, -1);
				cm.gainItem(4032015, -1);
				cm.gainItem(1332078, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r20 Black Crystals, 5 LUK Crystals, 1 Raven's Beak, and 1 Tao of Shadows#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 7) {
			if ((cm.haveItem(4021008, 20)) && (cm.haveItem(4005001, 5)) && (cm.haveItem(1472072, 1)) && (cm.haveItem(4032016, 1))) {
				cm.gainItem(4021008, -20);
				cm.gainItem(4005001, -5);
				cm.gainItem(1472072, -1);
				cm.gainItem(4032016, -1);
				cm.gainItem(1472074, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r20 Black Crystals, 1 Raven's Claw, 1 Tao of Sight, and 10 Wisdom Crystals#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 8) {
				if ((cm.haveItem(4021008, 30)) && (cm.haveItem(4005000, 5)) && (cm.haveItem(1472072, 1)) && (cm.haveItem(4032017, 1))) {
				cm.gainItem(4021008, -30);
				cm.gainItem(4005000, -5);
				cm.gainItem(1472072, -1);
				cm.gainItem(4032017, -1);
				cm.gainItem(1472075, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r30 Black Crystals, 5 Power Crystals, 1 Raven's Beak, and 1 Tao of Harmony#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 9) {
				if ((cm.haveItem(4021008, 30)) && (cm.haveItem(4005002, 10)) && (cm.haveItem(1472072, 1)) && (cm.haveItem(4032015, 1))) {
				cm.gainItem(4021008, -30);
				cm.gainItem(4005002, -10);
				cm.gainItem(1472072, -1);
				cm.gainItem(4032015, -1);
				cm.gainItem(1472073, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r30 Black Crystals, 10 DEX Crystals, 1 Raven's Claw, and 1 Tao of Shadows#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 10) {
				if ((cm.haveItem(4021008, 30)) && (cm.haveItem(4005002, 5)) && (cm.haveItem(1462052, 1)) && (cm.haveItem(4032016, 1))) {
				cm.gainItem(4021008, -30);
				cm.gainItem(4005002, -5);
				cm.gainItem(1462052, -1);
				cm.gainItem(4032016, -1);
				cm.gainItem(1462054, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r30 Black Crystals, 5 DEX Crystals, 1 Raven's Eye, and 1 Tao of Sight#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 11) {
				if ((cm.haveItem(4021008, 30)) && (cm.haveItem(4005002, 5)) && (cm.haveItem(1462052, 1)) && (cm.haveItem(4032017, 1))) {
				cm.gainItem(4021008, -30);
				cm.gainItem(4005002, -5);
				cm.gainItem(1462052, -1);
				cm.gainItem(4032017, -1);
				cm.gainItem(1462055, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #r#e30 Black Crystals, 5 DEX Crystals, 1 Raven's Eye, and 1 Tao of Harmony#k#n.");
					cm.dispose();
				}
			} else if (selection == 12) {
				if ((cm.haveItem(4021008, 20)) && (cm.haveItem(4005004, 1)) && (cm.haveItem(1462052, 1)) && (cm.haveItem(4032015, 1))) {
				cm.gainItem(4021008, -20);
				cm.gainItem(4005004, -1);
				cm.gainItem(1462052, -1);
				cm.gainItem(4032015, -1);
				cm.gainItem(1462053, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r20 Black Crystals, 1 Dark Crystal, 1 Raven's Eye, and 1 Tao of Shadows#k#n.");
					cm.dispose();
				}
			} else if (selection == 13) {
				if ((cm.haveItem(4021008, 30)) && (cm.haveItem(4005000, 10)) && (cm.haveItem(1402048, 1)) && (cm.haveItem(4032016, 1))) {
				cm.gainItem(4021008, -30);
				cm.gainItem(4005000, -10);
				cm.gainItem(1402048, -1);
				cm.gainItem(4032016, -1);
				cm.gainItem(1402050, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r30 Black Crystals, 10 Power Crystals, 1 Raven's Wing, and 1 Tao of Sight#k#n.");
					cm.dispose();
				}
			} else if (selection == 14) {
				if ((cm.haveItem(4021008, 30)) && (cm.haveItem(4005001, 10)) && (cm.haveItem(1402048, 1)) && (cm.haveItem(4032017, 1))) {
				cm.gainItem(4021008, -30);
				cm.gainItem(4005001, -10);
				cm.gainItem(1402048, -1);
				cm.gainItem(4032017, -1);
				cm.gainItem(1402051, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #r#e30 Black Crystals, 1 Raven's Wing, 1 Tao of Harmony, and 10 Wisdom Crystals#k#n.");
					cm.dispose();
				}
			} else if (selection == 15) {
				if ((cm.haveItem(4021008, 30)) && (cm.haveItem(4005002, 5)) && (cm.haveItem(1402048, 1)) && (cm.haveItem(4032015, 1))) {
				cm.gainItem(4021008, -30);
				cm.gainItem(4005002, -5);
				cm.gainItem(1402048, -1);
				cm.gainItem(4032015, -1);
				cm.gainItem(1402049, 1, true, true);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r30 Black Crystals, 5 DEX Crystals, 1 Raven's Wing, and 1 Tao of Shadows#k#n.");
					cm.dispose();
				}
			}
		}
	} 
		}
	}