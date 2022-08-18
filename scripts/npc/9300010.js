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

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Mr Moneybags
-- By --------------------------------------------------------------------------------------------------
	xQuasar
	Altered for Noble-Story by Kalyb.
**/

var status;
var oldWepName;
var oldWepId;
var oldWepQty;
var newWepId;
var newWepName;
var cost;
var getNewWep;
var mPlates = 4011002; //Mithril
var wPlates = 4011006; //Gold
var bPlates = 4011005; //Orihalcon
var tPlates = 4011004; //Silver
var pPlates = 4011003; //Adamantium
var stim;
var manual;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else if (status == -1) {
			status = 0;
			cm.sendSimple("Hello. I can upgrade your Maple Weapons and Shields to level 64. What would you like to upgrade? \r\n#e#b#L20#Warrior Maple Items \r\n#L21#Magician Maple Items \r\n#L22#Bowman Maple Items \r\n#L23#Thief Maple Items \r\n#L24#Pirate Maple Items#k#n");
	} else if (status == 0) {
		if (selection == 20) {
		cm.sendSimple("Which item would you like to upgrade? \r\n\r\n#e#b#L0##i1302064# Maple Glory Sword#l \r\n#L1##i1402039# Maple Soul Rohen#l \r\n#L2##i1312032# Maple Steel Axe#l \r\n#L3##i1412027# Maple Demon Axe#l \r\n#L4##i1322054# Maple Havoc Hammer#l \r\n#L5##i1422029# Maple Belzet#l \r\n#L11##i1432040# Maple Soul Spear#l \r\n#L12##i1442051# Maple Karstan#l \r\n#L17##i1092046# Maple Warrior Shield#k#n");
	} else if (selection == 21) {
		cm.sendSimple("Which item would you like to upgrade? \r\n\r\n#e#b#L13##i1372034# Maple Shine Wand#l \r\n#L14##i1382039# Maple Wisdom Staff#l \r\n#L18##i1092045# Maple Magician Shield#l#k#n");
	} else if (selection == 22) {
	cm.sendSimple("Which item would you like to upgrade? \r\n\r\n#e#b#L6##i1452045# Maple Kandiva Bow#l \r\n#L7##i1462040# Maple Nishada#l#k#n");
	} else if (selection == 23) {
		cm.sendSimple("Which item would you like to upgrade? \r\n\r\n#e#b#L8##i1472055# Maple Skanda#l \r\n#L9##i1332056# Maple Asura Dagger#l \r\n#L10##i1332055# Maple Dark Mate#l \r\n#L19##i1092047# Maple Thief Shield#l#k#n");
	} else if (selection ==24) {
		cm.sendSimple("Which item would you like to upgrade? \r\n\r\n#e#b#L15##i1482022# Maple Golden Claw#l \r\n#L16##i1492022# Maple Cannon Shooter#k#n");
	}
	status = 1;
	} else if (status == 1) {
			if (selection >= 0 && selection <= 19) {
				if (selection == 0) {
					oldWepName = "Maple Soul Singer";
					oldWepId = 1302030;
					oldWepQty = 1;
					newWepName = "Maple Glory Sword";
					newWepId = 1302064;
					leaves = 10;
					cost = 1000000;
					stim = 4130002;
					manual = 4131000;
				} else if (selection == 1) {
					oldWepName = "Maple Soul Singer";
					oldWepId = 1302030;
					oldWepQty = 1;
					newWepName = "Maple Soul Rohen";
					newWepId = 1402039;
					leaves = 10;
					cost = 1000000;
					stim = 4130005;
					manual = 4131003;
				} else if (selection == 2) {
					oldWepName = "Maple Dragon Axe";
					oldWepId = 1412011;
					oldWepQty = 1;
					newWepName = "Maple Steel Axe";
					newWepId = 1312032;
					leaves = 10;
					cost = 1000000;
					stim = 4130006;
					manual = 4131004;
					stim = 4130003;
					manual = 4131001;
				} else if (selection == 3) {
					oldWepName = "Maple Dragon Axe";
					oldWepId = 1412011;
					oldWepQty = 1;
					newWepName = "Maple Demon Axe";
					newWepId = 1412027;
					leaves = 10;
					cost = 1000000;
					stim = 4130006;
					manual = 4131004;
				} else if (selection == 4) {
					oldWepName = "Maple Doom Singer";
					oldWepId = 1422014;
					oldWepQty = 1;
					newWepName = "Maple Havoc Hammer";
					newWepId = 1322054;
					leaves = 10;
					cost = 1000000;
					stim = 4130004;
					manual = 4131002;
				} else if (selection == 5) {
					oldWepName = "Maple Doom Singer";
					oldWepId = 1422014;
					oldWepQty = 1;
					newWepName = "Maple Belzet";
					newWepId = 1422029;
					leaves = 10;
					cost = 1000000;
					stim = 4130007;
					manual = 4131005;
				} else if (selection == 6) {
					oldWepName = "Maple Soul Searcher";
					oldWepId = 1452022;
					oldWepQty = 1;
					newWepName = "Maple Kandiva Bow";
					newWepId = 1452045;
					leaves = 10;
					cost = 1000000;
					stim = 4130012;
					manual = 4131010;
				} else if (selection == 7) {
					oldWepName = "Maple Crossbow";
					oldWepId = 1462019;
					oldWepQty = 1;
					newWepName = "Maple Nishada";
					newWepId = 1462040;
					leaves = 10;
					cost = 1000000;
					stim = 4130013;
					manual = 4131011;
				} else if (selection == 8) {
					oldWepName = "Maple Kandayo";
					oldWepId = 1472032;
					oldWepQty = 1;
					newWepName = "Maple Skanda";
					newWepId = 1472055;
					leaves = 10;
					cost = 1000000;
					stim = 4130015;
					manual = 4131013;
				} else if (selection == 9 || selection == 10) {
					oldWepName = "Maple Wagner";
					oldWepId = 1332025;
					oldWepQty = 1;
					if (selection == 9) {
						newWepName = "Maple Asura Dagger";
						newWepId = 1332056;
					} else {
						newWepName = "Maple Dark Mate";
						newWepId = 1332055;
					}
					leaves = 10;
					cost = 1000000;
					stim = 4130014;
					manual = 4131012;
				} else if (selection == 11) {
					oldWepName = "Maple Impaler";
					oldWepId = 1432012;
					oldWepQty = 1;
					newWepName = "Maple Soul Spear";
					newWepId = 1432040;
					leaves = 10;
					cost = 1000000;
					stim = 4130008;
					manual = 4131006;
				} else if (selection == 12) {
					oldWepName = "Maple Scorpio";
					oldWepId = 1442024;
					oldWepQty = 1;
					newWepName = "Maple Karstan";
					newWepId = 1442051;
					leaves = 10;
					cost = 1000000;
					stim = 4130009;
					manual = 4131007;
				} else if (selection == 13) {
					oldWepName = "Maple Lama Staff";
					oldWepId = 1382012;
					oldWepQty = 1;
					newWepName = "Maple Shine Wand";
					newWepId = 1372034;
					leaves = 10;
					cost = 1000000;
					stim = 4130010;
					manual = 4131008;
				} else if (selection == 14) {
					oldWepName = "Maple Lama Staff";
					oldWepId = 1382012;
					oldWepQty = 1;
					newWepName = "Maple Wisdom Staff";
					newWepId = 1382039;
					leaves = 10;
					cost = 1000000;
					stim = 4130011;
					manual = 4131009;
				} else if (selection == 15) {
					oldWepName = "Maple Storm Finger";
					oldWepId = 1482021;
					oldWepQty = 1;
					newWepName = "Maple Golden Claw";
					newWepId = 1482022;
					leaves = 10;
					cost = 1000000;
					stim = 4130016;
					manual = 4131014;
				} else if (selection == 16) {
					oldWepName = "Maple Storm Pistol";
					oldWepId = 1492021;
					oldWepQty = 1;
					newWepName = "Maple Cannon Shooter";
					newWepId = 1492022;
					leaves = 10;
					cost = 1000000;
					stim = 4130017;
					manual = 4131015;
				} else if (selection == 17) {
					oldWepName = "Maple Shield";
					oldWepId = 1092030;
					oldWepQty = 1;
					newWepName = "Maple Warrior Shield";
					newWepId = 1092046;
					leaves = 10;
					cost = 1000000;
				} else if (selection == 18) {
					oldWepName = "Maple Shield";
					oldWepId = 1092030;
					oldWepQty = 1;
					newWepName = "Maple Magician Shield";
					newWepId = 1092045;
					leaves = 10;
					cost = 1000000;
				} else {
					oldWepName = "Maple Shield";
					oldWepId = 1092030;
					oldWepQty = 1;
					newWepName = "Maple Thief Shield";
					newWepId = 1092047;
					leaves = 10;
					cost = 1000000;
				}
				cm.sendYesNo("Are you sure you want to make a #b" + newWepName + "#k? The following items and materials will be required \r\n\r\n#i" + oldWepId + "# x" + oldWepQty + "\r\n#i4001126# x" + leaves + "\r\n#i4130016# 1 x Stimulator" + "\r\n#i4131014# 1 x Manual" + "\r\n\r\n#fUI/UIWindow.img/QuestIcon/7/0# " + cost);
				status =2;
			} else {
				cm.dispose();
			}
	} else if (status == 2) {
		if (mode !=1) {
			cm.dispose();
		}
			else if ((cm.getMeso() < cost) || (!cm.haveItem(oldWepId)) || (!cm.haveItem(4001126,leaves)) || (!cmhaveItem(stim)) || (!cm.haveItem(manual)) ) {
				cm.sendOk("Sorry, but you don't seem to have all the items. Please get them all, and try again.");
				cm.dispose();
			}
	 		else if (cm.haveItem(oldWepId) && cm.getMeso() >= cost && cm.haveItem(4001126, 10)) {
				cm.gainItem(oldWepId,-oldWepQty);
				cm.gainItem(4001126,-leaves);
				cm.gainMeso(-cost);
				cm.gainItem(-crystals);
				cm.gainItem(newWepId,1, true, true);
				cm.gainItem(-stim);
				cm.gainItem(-manual);
				cm.sendOk("There, all done! That was quick, wasn't it? If you need any more items, I'll be waiting here.");
				cm.dispose();
			}
			}	
}