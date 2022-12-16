// Author: Ronan
importPackage(Packages.network.packet.field);

var mapId = 200090000;

function start(ms) {
	var map = ms.getClient().getChannelServer().getMapFactory().getMap(mapId);

	if(map.getDocked()) {
		//onFieldEffect needs to have the 6 passed for result type to designate a music change
		ms.getClient().announce(CField.Packet.onFieldEffect(6, "Bgm04/ArabPirate"));
		ms.getClient().announce(MaplePacketCreator.crogBoatPacket(true));
	}

	return(true);
}