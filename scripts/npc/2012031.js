/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana
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
/* Harp String D
	Hidden Street - Eliza’s Garden (200010303)
 */

importPackage(Packages.network.packet.field);

var status;
var harpNote = 'G';
var harpSounds = ["do", "re", "mi", "pa", "sol", "la", "si"];   // musical order detected thanks to Arufonsu
var harpSong = "CCGGAAGFFEEDDC|GGFFEED|GGFFEED|CCGGAAGFFEEDDC|";

function start() {
        status = -1;
        action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.dispose();
        } else {
                if (mode == 0 && type > 0) {
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;

                if (status == 0) {
                        cm.getMap().broadcastMessage(MCField.Packet.onFieldEffect(4, "orbis/" + harpSounds[cm.getNpc() - 2012027]));

                        if (cm.isQuestStarted(3114)) {
                                var idx = -1 * cm.getQuestProgressInt(3114);

                                if (idx > -1) {
                                        var nextNote = harpSong[idx];

                                        if (harpNote != nextNote) {
                                                cm.setQuestProgress(3114, 0);

                                                cm.getPlayer().announce(CField.Packet.onFieldEffect(3, "quest/party/wrong_kor"));
                                                cm.getPlayer().announce(CField.Packet.onFieldEffect(4, "Party1/Failed"));

                                                cm.message("You've missed the note... Start over again.");
                                        } else {
                                                nextNote = harpSong[idx + 1];

                                                if (nextNote == '|') {
                                                        idx++;

                                                        if (idx == 45) {     // finished lullaby
                                                                cm.message("Twinkle, twinkle, little star, how I wonder what you are.");
                                                                cm.setQuestProgress(3114, 42);

                                                                cm.getPlayer().announce(CField.Packet.onFieldEffect(3, "quest/party/clear"));
                                                                cm.getPlayer().announce(CField.Packet.onFieldEffect(4, "Party1/Clear"));

                                                                cm.dispose();
                                                                return;
                                                        } else {
                                                                if (idx == 14) {
                                                                        cm.message("Twinkle, twinkle, little star, how I wonder what you are!");
                                                                } else if (idx == 22) {
                                                                        cm.message("Up above the world so high,");
                                                                } else if (idx == 30) {
                                                                        cm.message("like a diamond in the sky.");
                                                                }
                                                        }
                                                }

                                                cm.setQuestProgress(3114, -1 * (idx + 1));
                                        }
                                }
                        }

                        cm.dispose();
                }
        }
}