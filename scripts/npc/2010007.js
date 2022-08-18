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
/* guild creation npc */
var status = 0;
var sel;
var cost = 500000000;

function start() {
    cm.sendSimple("What would you like to do?\r\n#b" +
        "#L0#Create a Guild#l\r\n" +
        "#L1#Disband your Guild#l\r\n" +
        "#L2#Increase your Guild's capacity#l\r\n" +
        "#L3#Change your Guild's name#l#k");
}

function action(mode, type, selection) {
    if (mode === -1) {
        cm.dispose();
    } else {
        if (mode === 0 && status === 0) {
            cm.dispose();
            return;
        }
        if (mode === 1)
            status++;
        else
            status--;
        if (status === 0) {
            cm.dispose();
            return;
        }
        if (status === 1) {
            sel = selection;
            if (selection === 0) {
                if (cm.getPlayer().getGuildId() > 0) {
                    cm.sendOk("You may not create a new guild while you are in one.");
                    cm.dispose();
                } else
                    cm.sendYesNo("Creating a guild costs #b1500000 mesos#k, are you sure you want to continue?");
            } else if (selection === 1) {
                if (cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() !== 1) {
                    cm.sendOk("You can only disband a Guild if you are the leader of that Guild.");
                    cm.dispose();
                } else
                    cm.sendYesNo("Are you sure you want to disband your guild? You will not be able to recover it afterward and all your GP will be gone.");
            } else if (selection === 2) {
                if (cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() !== 1) {
                    cm.sendOk("You can only increase your Guild's capacity if you are the leader.");
                    cm.dispose();
                } else
                    cm.sendYesNo("Increasing your guild capacity by #b5#k costs #b"
                        + Packages.net.server.guild.MapleGuild.getIncreaseGuildCost(cm.getPlayer().getGuild().getCapacity())
                        + " mesos#k, are you sure you want to continue?");
            } else if (selection === 3) {
                if (cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() !== 1) {
                    cm.sendOk("You can only update your guild's name if you are the leader.");
                    cm.dispose();
                } else
                    cm.sendYesNo("Changing your guild's name will cost #b" + cost + " mesos#k, and will update on the next server restart. Are you sure you want to continue?");
            }
        } else if (status === 2) {
            if (sel === 0 && cm.getPlayer().getGuildId() <= 0) {
                cm.getPlayer().genericGuildMessage(1);
                cm.dispose();
            } else if (cm.getPlayer().getGuildId() > 0 && cm.getPlayer().getGuildRank() === 1) {
                if (sel === 1) {
                    cm.getPlayer().disbandGuild();
                    cm.dispose();
                } else if (sel === 2) {
                    cm.getPlayer().increaseGuildCapacity();
                    cm.dispose();
                } else if (sel === 3) {
                    cm.sendGetText("What would you like to rename your Guild?\r\n" +
                        "Please limit the name of your guild to #r12 characters#k.");
                }
            }
        } else if (status === 3) {
            var newName = cm.getText();
            var guildId = cm.getPlayer().getGuildId();
            var success = cm.getPlayer().updateGuildName(guildId, newName);

            if (success === 2) {
                cm.gainMeso(-cost);
                cm.sendOk("Congratulations, from now on your guild shall be known as: #d" + cm.getText() + "#k.\r\n"
                    + "Please wait for the #bnext server restart#k for the guild's name to update.");
            } else if (success === 1) {
                cm.sendOk("The name #r" + cm.getText() + "#k is already in use, please select another name.");
            } else if (success === 0) {
                cm.sendOk("The name #r" + cm.getText() + "#k exceeds the 12 character limit, please select another name.");
            }
            cm.dispose();
        }
    }
}
