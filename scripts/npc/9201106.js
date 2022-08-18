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

Adonis (El Nath); ITCG Forging Trader.
by: Nina
for: NobleStory

 */
var itcg = new Array(1332031, 2022117, 3010014, 4131001, 4130004, 4131007, 2000005, 1432013)
var itcgLength = itcg.length + 1;
var randNum = Math.floor(Math.random()*itcgLength);
var randitcg= itcg[randNum];
 
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
				cm.sendSimple("Hello. I am Adonis! What would you like to do?#e#b\r\n#L0##i1102194# Upgrade to Shroud of Zakum#l \r\n#L1##i1302107# Upgrade to Black Crystal Blade \r\n#L2##i1102192# Upgrade to Wrath of El Nath \r\n#L3##i1002857# Upgrade to Hard Hat \r\n#L4##i4032134# Exchange my Stone Denari#k#n");	 	
		} else if (status == 1){
			if (selection == 0) {
				if ((cm.haveItem(1102193, 1)) &&
				(cm.haveItem(4031900, 1)) &&
				(cm.haveItem(4005004, 50)) && (cm.haveItem(4031758, 1)) && (cm.haveItem(4031915, 1)) && (cm.haveItem(4032133, 1))) {
				cm.gainItem(1102193, -1);
				cm.gainItem(4031900, -1);
				cm.gainItem(4005004, -50);
				cm.gainItem(4031915, -1);
				cm.gainItem(4031758, -1);
				cm.gainItem(4032133, -1);
				cm.gainItem(1102194, 1);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Cloak of Corruption, 1 Ridley's Book of Rituals, 50 Dark Crystals, 1 Naricain Jewel, 1 Lefay Jewel, and 1 Zakum Diamond#k#n.");
					cm.dispose();
				}
			} if (selection == 1) {
				if ((cm.haveItem(1302106, 1)) &&
				(cm.haveItem(4131000, 1)) &&
				(cm.haveItem(4130002, 1)) && (cm.haveItem(4021008, 50)) && (cm.haveItem(4032133, 1))) {
				cm.gainItem(1302106, -1);
				cm.gainItem(4131000, -1);
				cm.gainItem(4130002, -1);
				cm.gainItem(4021008, -50);
				cm.gainItem(4032133, -1);
				cm.gainItem(1302107, 1);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #r#e1 Crystal Blade, 1 1H Sword Forging Manual, 1 1H Sword Forging Stimulator, 50 Black Crystals, and 1 Zakum Diamond#k#n.");
					cm.dispose();
				}
			}
			 else if (selection == 2) {
				if ((cm.haveItem(1102191, 1)) &&
				(cm.haveItem(4000081, 50)) &&
				(cm.haveItem(4000080, 10)) && (cm.haveItem(4031916, 1)) && (cm.haveItem(4000057, 10)) && (cm.haveITem(4000056 ,10)) && (cm.haveItem(4032133, 1))) {
				cm.gainItem(1102191, -1);
				cm.gainItem(4000081, -50);
				cm.gainItem(4000080, -10);
				cm.gainItem(4031916, -1);
				cm.gainItem(4000057, -10);
				cm.gainItem(4000056, -10);
				cm.gainItem(4032133, -1);
				cm.gainItem(1102192, 1);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 El Nathian Cape, 50 Firebomb Flames, 10 Bain's Spikey Collars, 1 Pharoah's Wrappings, 10 Dark Pepe Beaks, 10 Dark Yeti's Horns, and 1 Zakum Diamond#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 3) {
				if ((cm.haveItem(1002856, 1)) &&
				(cm.haveItem(4000391, 10)) &&
				(cm.haveItem(4000376, 5)) && (cm.haveItem(4005000, 1)) && (cm.haveItem(4011004, 10)) && cm.haveItem(4032133, 1)) {
				cm.gainItem(1002856, -1);
				cm.gainItem(4000391, -10);
				cm.gainItem(4000376, -5);
				cm.gainItem(4005000, -1);
				cm.gainItem(4011004, -10);
				cm.gainItem(4032133, -1);
				cm.gainItem(1002857, 1);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Miner's Hat, 10 Boomer Cores, 5 Batteries, 1 Power Crystal, 10 Silver Plates, and 1 Zakum Diamond#k#n.");
					cm.dispose();
				}
			}
			else if (selection == 4) {
				if ((cm.haveItem(4032134, 1))) {
				cm.gainItem(randitcg, 1, true, true);
				cm.gainItem(4032134, -1);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Stone Denari#k#n.");
					cm.dispose();
				}
			} 
				}
			}
		}
	} 
