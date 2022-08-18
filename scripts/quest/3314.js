/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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

importPackage(Packages.client);

var status = -1;

function isPillUsed(ch) {
    return ch.getBuffSource(MapleBuffStat.INC_EFFECT_HP_POTION) == 2022198;
}

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1)
            status++;
        else
            status--;

        if (status == 0) {
            if (isPillUsed(qm.getPlayer())) {
                print("reached");
                if (qm.canHoldAll([2050004.00, 2022224.00], [10.00, 20.00])) {
                    qm.sendNext("So you took my experiments... hmm... so THAT's the result of it, hehehehe... Ok, " +
                        "take that as compensation will you? And oh, you can #rspew that out#k right away, no " +
                        "worries. (#bright-click on the pill icon at the top-right corner of the screen#k)");

                    qm.gainExp(12500);
                    qm.gainItem(2050004, 10);

                    var i = Math.floor(Math.random() * 5);
                    qm.gainItem(2022224 + i, 10);

                    qm.forceCompleteQuest();
                } else {
                    qm.sendNext("Huh, your inventory is full. Free some spaces on your USE first.");
                }
            } else {
                print("reached2");
                qm.sendNext("You seem pretty normal, don't you? I can't detect any possible effect from my " +
                    "experiment on you. Go take the pill I asked you to take and show me the effects, will you?");
            }
        } else {
            qm.dispose();
        }
    }
}