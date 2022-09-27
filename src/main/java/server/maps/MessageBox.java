package server.maps;

import java.awt.Point;
import client.MapleCharacter;
import client.MapleClient;
import network.packet.MessageBoxPool;

public class MessageBox extends AbstractMapleMapObject {

    private Point pos;
    private MapleCharacter owner;
    private String text;
    private int ft;
    private int itemid;

    public MessageBox(MapleCharacter owner, String text, int itemid) {
        this.owner = owner;
        this.pos = owner.getPosition();
        this.ft = owner.getFh();
        this.text = text;
        this.itemid = itemid;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.KITE;
    }

    @Override
    public Point getPosition() {
        return pos.getLocation();
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(makeDestroyData());
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.announce(makeSpawnData());
    }

    public final byte[] makeSpawnData() {
        return MessageBoxPool.Packet.onMessageBoxEnterField(getObjectId(), itemid, owner.getName(), text, pos, ft);
    }

    public final byte[] makeDestroyData() {
        return MessageBoxPool.Packet.onMessageBoxLeaveField(getObjectId(), 0);
    }
}