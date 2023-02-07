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

package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import enums.BroadcastMessageType;
import enums.CashItemResultType;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import network.packet.CCashShop;
import network.packet.CStage;
import network.packet.context.BroadcastMsgPacket;
import network.packet.context.WvsContext;
import server.maps.MapleMiniDungeonInfo;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Flav
 */
public class MigrateToCashShopRequestHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        try {
            MapleCharacter mc = c.getPlayer();

            if (mc.cannotEnterCashShop()) {
                c.announce(WvsContext.Packet.enableActions());
                return;
            }
            
            if(mc.getEventInstance() != null) {
                c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(),
                        "Entering Cash Shop or MTS are disabled when registered on an event."));
                c.announce(WvsContext.Packet.enableActions());
                return;
            }
            
            if(MapleMiniDungeonInfo.isDungeonMap(mc.getMapId())) {
                c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(),
                        "Changing channels or entering Cash Shop or MTS are disabled when inside a Mini-Dungeon."));
                c.announce(WvsContext.Packet.enableActions());
                return;
            }
            
            if (mc.getCashShop().isOpened()) {
                return;
            }

            mc.closePlayerInteractions();
            mc.closePartySearchInteractions();

            //mc.unregisterChairBuff();
            Server.getInstance().getPlayerBuffStorage().addBuffsToStorage(mc.getId(), mc.getAllBuffs());
            //Server.getInstance().getPlayerBuffStorage().addDiseasesToStorage(mc.getId(), mc.getAllDiseases());
            mc.setAwayFromChannelWorld();
            mc.notifyMapTransferToPartner(-1);
            mc.removeIncomingInvites();
            mc.cancelAllBuffs(true);
            mc.cancelAllDebuffs();
            mc.cancelBuffExpireTask();
            mc.cancelDiseaseExpireTask();
            mc.cancelSkillCooldownTask();
            mc.cancelExpirationTask();
            
            mc.forfeitExpirableQuests();
            mc.cancelQuestExpirationTask();
            
            c.announce(CStage.Packet.onSetCashShop(c, false));
            c.announce(CCashShop.Packet.onCashItemResult(CashItemResultType.LoadInventory.getResult(), mc));
            c.announce(CCashShop.Packet.onCashItemResult(CashItemResultType.LoadGifts.getResult(), mc.getCashShop().loadGifts()));
            c.announce(CCashShop.Packet.onCashItemResult(CashItemResultType.LoadWishList.getResult(), mc));
            c.announce(CCashShop.Packet.onQueryCashResult(mc));

            c.getChannelServer().removePlayer(mc);
            mc.getMap().removePlayer(mc);
            mc.getCashShop().open(true);
            mc.saveCharToDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
