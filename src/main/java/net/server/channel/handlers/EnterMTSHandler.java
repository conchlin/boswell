package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import constants.GameConstants;
import constants.MapConstants;
import net.AbstractMaplePacketHandler;
import network.packet.WvsContext;
import server.maps.MapleMiniDungeonInfo;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/*
   @Author: Saffron
*/

public class EnterMTSHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();

        if (player.getEventInstance() != null) {
            player.dropMessage("You can't enter the FM when you are registered in an event.");
        } else if (player.getMapId() == 109050001) { // event maps that aren't registered as event maps
            player.dropMessage("You can't enter the FM from an event map.");
        } else if (MapleMiniDungeonInfo.isDungeonMap(player.getMapId())) {
            player.dropMessage("You can't enter the FM when you are inside a mini-dungeon.");
        } else if (!(player.isAlive())) {
            player.dropMessage("You can't enter the FM when you are dead.");
        } else if (player.getLevel() < 7) {
            player.dropMessage("You must be level 7 to access the FM.");
        } else if (MapConstants.isRestrictedFMMap(c.getPlayer().getMapId())) {
            player.dropMessage("You cannot enter the FM from this map.");
        } else if (c.getPlayer().getMapId() >= 910000000 && c.getPlayer().getMapId() <= 910000022) {
            player.dropMessage("You are already in the FM.");
        } else if (GameConstants.isDojo(c.getPlayer().getMapId())) {
            player.dropMessage("You cannot enter the FM when in Dojo.");
        } else {
            player.saveLocation("FREE_MARKET");
            player.saveLocationOnWarp();
            player.saveCharToDB();
            player.changeMap(910000000, "out00");
        }

        c.announce(WvsContext.Packet.enableActions());
    }
}
