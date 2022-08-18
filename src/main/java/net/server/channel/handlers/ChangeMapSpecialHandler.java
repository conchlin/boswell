package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.MaplePortal;
import server.MapleTrade;
import server.MapleTrade.TradeResult;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class ChangeMapSpecialHandler extends AbstractMaplePacketHandler {
        @Override
        public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
                slea.readByte();
                String startwp = slea.readMapleAsciiString();
                slea.readShort();
                MaplePortal portal = c.getPlayer().getMap().getPortal(startwp);
                if (portal == null || c.getPlayer().portalDelay() > currentServerTime() || c.getPlayer().getBlockedPortals().contains(portal.getScriptName())) {
                        c.announce(MaplePacketCreator.enableActions());
                        return;
                }
                if (c.getPlayer().isChangingMaps() || c.getPlayer().isBanned()) {
                        c.announce(MaplePacketCreator.enableActions());
                        return;
                }
                if (c.getPlayer().getTrade() != null) {
                        MapleTrade.cancelTrade(c.getPlayer(), TradeResult.UNSUCCESSFUL_ANOTHER_MAP);
                }
                portal.enterPortal(c);
        }
}