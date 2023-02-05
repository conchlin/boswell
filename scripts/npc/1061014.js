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
 *
 *@author Ronan
 */

importPackage(Packages.server.expeditions);
importPackage(Packages.tools);
importPackage(Packages.scripting.event);
importPackage(Packages.network.packet.context);
importPackage(Packages.enums);

var status = 0;
var expedition;
var expedMembers;
var player;
var em;
var exped = MapleExpeditionType.BALROG_NORMAL;
var expedName = "Balrog";
var expedBoss = "Balrog";
var expedMap = "Balrog's Tomb";

var list = "What would you like to do?#b\r\n\r\n#L1#View current Expedition members#l\r\n#L2#Start the fight!#l\r\n#L3#Stop the expedition.#l";

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {

    player = cm.getPlayer();
    expedition = cm.getExpedition(exped);
    em = cm.getEventManager("BalrogBattle");

    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }

        if (status == 0) {
            if (player.getLevel() < exped.getMinLevel() && player.getLevel() > exped.getMaxLevel()) { //Don't fit requirement
                cm.sendOk("You do not meet the criteria to battle " + expedBoss + "!");
                cm.dispose();
            } else if (expedition == null) { //Start an expedition
                cm.sendSimple("#e#b<Expedition: " + expedName + ">\r\n#k#n" + em.getProperty("party") + "\r\n\r\nWould you like to assemble a team to take on #r" + expedBoss + "#k?\r\n#b#L1#Lets get this going!#l\r\n\#L2#No, I think I'll wait a bit...#l\r\n\#L3#I would like to see info about this expedition...#l");
                status = 1;
            } else if (expedition.isLeader(player)) { //If you're the leader, manage the exped
                if (expedition.isInProgress()) {
                    cm.sendOk("Your expedition is already in progress, for those who remain battling lets pray for those brave souls.");
                    cm.dispose();
                } else {
                    cm.sendSimple(list);
                    status = 2;
                }
            } else if (expedition.isRegistering()) { //If the expedition is registering
                if (expedition.contains(player)) { //If you're in it but it hasn't started, be patient
                    cm.sendOk("You have already registered for the expedition. Please wait for #r" + expedition.getLeader().getName() + "#k to begin it.");
                    cm.dispose();
                } else { //If you aren't in it, you're going to get added
                    cm.sendOk(expedition.addMember(cm.getPlayer()));
                    cm.dispose();
                }
            } else if (expedition.isInProgress()) { //Only if the expedition is in progress
                if (expedition.contains(player)) { //If you're registered, warp you in
                    var eim = em.getInstance(expedName + player.getClient().getChannel());
                    if(eim.getIntProperty("canJoin") == 1) {
                        eim.registerPlayer(player);
                    } else {
                        cm.sendOk("Your expedition already started the battle against " + expedBoss + ". Lets pray for those brave souls.");
                    }
                    
                    cm.dispose();
                } else { //If you're not in by now, tough luck
                    cm.sendOk("Another expedition has taken the initiative to challenge " + expedBoss + ", lets pray for those brave souls.");
                    cm.dispose();
                }
            }
        } else if (status == 1) {
            if (selection == 1) {
                expedition = cm.getExpedition(exped);
                if(expedition != null) {
                    cm.sendOk("Someone already taken the initiative to be the leader of the expedition. Try joining them!");
                    cm.dispose();
                    return;
                }
                
                var res = cm.createExpedition(exped);
                if (res == 0) {
                    cm.sendOk("The #r" + expedBoss + " Expedition#k has been created.\r\n\r\nTalk to me again to view the current team, or start the fight!");
                } else if (res > 0) {
                    cm.sendOk("Sorry, you've already reached the quota of attempts for this expedition! Try again another day...");
                } else {
                    cm.sendOk("An unexpected error has occurred when starting the expedition, please try again later.");
                }
                
                cm.dispose();
                return;
            } else if (selection == 2) {
                cm.sendOk("Sure, not everyone's up to challenging " + expedBoss + ".");
                cm.dispose();
                return;
            } else {
                cm.sendSimple("Hi there. I am #b#nMu Young#n#k, the temple Keeper. This temple is currently under siege by the Balrog troops. We currently do not know who gave the orders. " +
                            "For a few weeks now, the #e#b Order of the Altair#n#k has been sending mercenaries, but they were eliminated every time." +
                            " So, traveler, would you like to try your luck at defeating this unspeakable horror?\r\n  #L1#What is the #eOrder of the Altair?");
                    
                status = 10;
            }
        } else if (status == 2) {
            if (selection == 1) {
                if (expedition == null) {
                    cm.sendOk("The expedition could not be loaded.");
                    cm.dispose();
                    return;
                }
                expedMembers = expedition.getMemberList();
                var size = expedMembers.size();
                if (size == 1) {
                    cm.sendOk("You are the only member of the expedition.");
                    cm.dispose();
                    return;
                }
                var text = "The following members make up your expedition (Click on them to expel them):\r\n";
                text += "\r\n\t\t1." + expedition.getLeader().getName();
                for (var i = 1; i < size; i++) {
                    text += "\r\n#b#L" + (i + 1) + "#" + (i + 1) + ". " + expedMembers.get(i).getValue() + "#l\n";
                }
                cm.sendSimple(text);
                status = 6;
            } else if (selection == 2) {
                var min = exped.getMinSize();
                var size = expedition.getMemberList().size();
                if (size < min) {
                    cm.sendOk("You need at least " + min + " players registered in your expedition.");
                    cm.dispose();
                    return;
                }
                
                cm.sendOk("The expedition will begin and you will now be escorted to the #b" + expedMap + "#k.");
                status = 4;
            } else if (selection == 3) {
                player.getMap().broadcastMessage(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.BlueText.getType, expedition.getLeader().getName() + " has ended the expedition."));
                cm.endExpedition(expedition);
                cm.sendOk("The expedition has now ended. Sometimes the best strategy is to run away.");
                cm.dispose();
                return;
            }
        } else if (status == 4) {
            if (em == null) {
                cm.sendOk("The event could not be initialized, please report this on the forum.");
                cm.dispose();
                return;
            }

            em.setProperty("leader", player.getName());
            em.setProperty("channel", player.getClient().getChannel());
            if(!em.startInstance(expedition)) {
                cm.sendOk("Another expedition has taken the initiative to challenge " + expedBoss + ", lets pray for those brave souls.");
                cm.dispose();
                return;
            }
            
            cm.dispose();
            return;
        } else if (status == 6) {
            if (selection > 0) {
                var banned = expedMembers.get(selection - 1);
                expedition.ban(banned);
                cm.sendOk("You have banned " + banned.getValue() + " from the expedition.");
                cm.dispose();
            } else {
                cm.sendSimple(list);
                status = 2;
            }
        } else if (status == 10) {
            cm.sendOk("The Order of the Altair is a group of elite mercenaries that oversee the world's economy and battle operations. It was founded 40 years ago right after Black Mage was defeated in hopes of forseeing the next possible attack.");
            cm.dispose();
        }
    }
}
