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
package net.server.channel.handlers;

import enums.BroadcastMessageType;
import enums.PartyResultType;
import net.AbstractMaplePacketHandler;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.World;
import network.packet.context.BroadcastMsgPacket;
import network.packet.context.PartyPacket;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;
import client.MapleClient;
import net.server.coordinator.MapleInviteCoordinator;
import net.server.coordinator.MapleInviteCoordinator.InviteResult;
import net.server.coordinator.MapleInviteCoordinator.InviteType;
import tools.Pair;

import java.util.List;

public final class PartyRequestHandler extends AbstractMaplePacketHandler {
    
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int operation = slea.readByte();
        MapleCharacter player = c.getPlayer();
        World world = c.getWorldServer();
        MapleParty party = player.getParty();
        switch (operation) {
            case 1 -> // create
                    MapleParty.createParty(player, false);
            case 2 -> { // leave/disband
                if (party != null) {
                    List<MapleCharacter> partymembers = player.getPartyMembers();

                    MapleParty.leaveParty(party, c);
                    player.updatePartySearchAvailability(true);
                    player.partyOperationUpdate(party, partymembers);
                }
            }
            case 3 -> { // join
                int partyid = slea.readInt();

                Pair<InviteResult, MapleCharacter> inviteRes = MapleInviteCoordinator.answerInvite(InviteType.PARTY, player.getId(), partyid, true);
                InviteResult res = inviteRes.getLeft();
                if (res == InviteResult.ACCEPTED) {
                    MapleParty.joinParty(player, partyid, false);
                    player.receivePartyMemberHP();
                    player.updatePartyMemberHP();
                } else {
                    c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(),
                            "You couldn't join the party due to an expired invitation request."));
                }
            }
            case 4 -> { // invite
                String name = slea.readMapleAsciiString();
                MapleCharacter invited = world.getPlayerStorage().getCharacterByName(name);
                if (invited != null) {
                    if (player.isCheater() && !invited.isCheater() ||
                            !player.isCheater() && invited.isCheater()) {
                        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(),
                                "The player you have invited is not allowed to join your party. Please contact a moderator for assistance."));
                        return;
                    }
                    if (invited.getLevel() < 10 && !(player.getLevel() >= 10)) { //min requirement is level 10
                        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(),
                                "The player you have invited does not meet the requirements."));
                        return;
                    }
                    if (invited.getLevel() >= 10 && player.getLevel() < 10) {    //trying to invite high level
                        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(),
                                "The player you have invited does not meet the requirements."));
                        return;
                    }
                    if (invited.getParty() == null) {
                        if (party == null) {
                            if (!MapleParty.createParty(player, false)) {
                                return;
                            }

                            party = player.getParty();
                        }
                        if (party.getMembers().size() < 6) {
                            if (MapleInviteCoordinator.createInvite(InviteType.PARTY, player, party.getId(), invited.getId())) {
                                invited.getClient().announce(PartyPacket.Packet.onPartyResult(player, PartyResultType.Invite.getResult()));
                            } else {
                                c.announce(PartyPacket.Packet.onPartyMessage(PartyResultType.UserHasOtherInvite.getResult(), invited.getName()));
                            }
                        } else {
                            c.announce(PartyPacket.Packet.onPartyMessage(PartyResultType.Full.getResult()));
                        }
                    } else {
                        c.announce(PartyPacket.Packet.onPartyMessage(PartyResultType.AlreadyJoined.getResult()));
                    }
                } else {
                    c.announce(PartyPacket.Packet.onPartyMessage(PartyResultType.CannotFindUser.getResult()));
                }
            }
            case 5 -> { // expel
                int cid = slea.readInt();
                MapleParty.expelFromParty(party, c, cid);
            }
            case 6 -> { // change leader
                int newLeader = slea.readInt();
                MaplePartyCharacter newLeadr = party.getMemberById(newLeader);
                world.updateParty(party.getId(), PartyResultType.ChangeLeader.getResult(), newLeadr);
            }
        }
    }
}