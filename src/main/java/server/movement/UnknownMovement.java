package server.movement;

import java.awt.Point;

import tools.data.output.LittleEndianWriter;

/**
 *
 * @author Tyler
 */
public class UnknownMovement extends AbsoluteLifeMovement {
    private int fh;
    public UnknownMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }

    public void setFh(int unk) {
        this.fh = unk;
    }

    public int getFh() {
        return fh;
    }

    @Override
    public void serialize(LittleEndianWriter lew) {
        lew.write(getType());
        lew.writeShort(getPosition().x);
        lew.writeShort(getPosition().y);
        lew.writeShort(fh);
        lew.write(getNewstate());
        lew.writeShort(getDuration());
    }
}