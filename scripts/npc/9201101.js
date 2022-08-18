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

T-1337 (NLC); ITCG Forging Trader.
by: Nina
for: NobleStory

 */
var itcg = new Array(2022338, 2022339, 2022340, 2022341, 2022342, 2022343, 2022344)
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
				cm.sendYesNo("Hello. I am T-1337! Would you like to trade for a random ITCG potion? For #e#r1 Diferium Fuel Cell, 1 Jumper Cable, 1 Sparkplug, 1 T-1 Socket Adapter, and 50 Boomer Cores#k#n, I can offer you one of the following: ElectroJuice, GigaJuice, Blastrojuice, JigaJuice, MegaJuice, Nitrojuice, or Virtrojuice!");	 	
		} else if (status == 1){
				if (cm.haveItem(4032022, 1) && (cm.haveItem(4000391, 50)) && (cm.haveItem(4032023, 1)) && (cm.haveItem(4032024, 1)) &&
				(cm.haveItem(4032025, 1))) {
				cm.gainItem(4032022, -1);
				cm.gainItem(4000391, -50);
				cm.gainItem(4032023, -1);
				cm.gainItem(4032024, -1);
				cm.gainItem(4032025, -1);
				cm.gainItem(randitcg, 1);
				cm.dispose();
				} else {
					cm.sendOk("You do not have at least #r#e1 Diferium Fuel Cell, 1 Jumper Camble, 1 Sparkplug, 1 T-1 Socket Adapter, and 50 Boomer Cores#k#n.");
					cm.dispose();
			}
			}
			}
			}
		}