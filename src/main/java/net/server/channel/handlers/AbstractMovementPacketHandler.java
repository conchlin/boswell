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

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.AbstractMaplePacketHandler;
import server.maps.AnimatedMapleMapObject;
import server.movement.*;
import tools.data.input.LittleEndianAccessor;
import tools.exceptions.EmptyMovementException;

public abstract class AbstractMovementPacketHandler extends AbstractMaplePacketHandler {

    protected List<LifeMovementFragment> parseMovement(LittleEndianAccessor lea) {
        List<LifeMovementFragment> res = new ArrayList<>();
        byte numCommands = lea.readByte();

        for (byte i = 0; i < numCommands; i++) {
            byte command = lea.readByte();
            switch (command) {
                case 0: // normal move
                case 5:
                case 17: { // Float
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short velocityX = lea.readShort();
                    short velocityY = lea.readShort();
                    short fhId = lea.readShort();
                    byte bMoveAction = lea.readByte();
                    short tElapsed = lea.readShort();
                    AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, new Point(xpos, ypos), tElapsed, bMoveAction);
                    alm.setFh(fhId);
                    alm.setPixelsPerSecond(new Point(velocityX, velocityY));
                    res.add(alm);
                    break;
                }
                case 1:
                case 2:
                case 6: // fj
                case 12:
                case 13: // Shot-jump-back thing
                case 16: // Float
                case 18:
                case 19: // Springs on maps
                case 20: // Aran Combat Step
                case 22: {
                    short velocityX = lea.readShort();
                    short velocityY = lea.readShort();
                    byte bMoveAction = lea.readByte();
                    short tElapsed = lea.readShort();
                    RelativeLifeMovement rlm = new RelativeLifeMovement(command, new Point(velocityX, velocityY), tElapsed, bMoveAction);
                    res.add(rlm);
                    break;
                }
                case 3:
                case 4: // tele... -.-
                case 7: // assaulter
                case 8: // assassinate
                case 9: // rush
                case 11: //chair
                {
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short fhId = lea.readShort();
                    byte bMoveAction = lea.readByte();
                    short tElapsed = lea.readShort();
                    TeleportMovement tm = new TeleportMovement(command, new Point(xpos, ypos), fhId, tElapsed, bMoveAction);
                    res.add(tm);
                    break;
                }
                case 10: // Change Equip
                    res.add(new ChangeEquip(lea.readByte()));
                    break;
                case 14: { //This case causes map objects to disappear and reappear
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short fhId = lea.readShort();
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();

				    /*	UnknownMovement unkM = new UnknownMovement(command, new Point(xpos, ypos), duration, newstate);
					    unkM.setUnk(fhId);
					    res.add(unkM); */
                    break;
                }
                case 15: {
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short xwobble = lea.readShort();
                    short ywobble = lea.readShort();
                    //One would expect that it would be the to and from fh id, but apparently it isnt
                    //once the jump down is finished fhId becomes 0 and the fh becomes the fhid
                    //But at the start of it, its going to be the same fh id
                    short fh = lea.readShort();
                    short ofh = lea.readShort();
                    byte bMoveAction = lea.readByte();
                    short tElapsed = lea.readShort();
                    JumpDownMovement jdm = new JumpDownMovement(command, new Point(xpos, ypos), tElapsed, bMoveAction);
                    jdm.setFh(fh);
                    jdm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    jdm.setOriginFh(ofh);
                    res.add(jdm);
                    break;
                }
                case 21: {//Causes aran to do weird stuff when attacking o.o
                    /*byte newstate = lea.readByte();
                     short unk = lea.readShort();
                     AranMovement am = new AranMovement(command, null, unk, newstate);
                     res.add(am);*/
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();
                    break;
                }
                default:
                    System.out.println("Unhandled Case:" + command);
                    return null;
            }
        }
        return res;
    }

    protected void updatePosition(List<LifeMovementFragment> movement, AnimatedMapleMapObject target, int yoffset) {
        for (LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof AbsoluteLifeMovement) {
                    Point position = ((LifeMovement) move).getPosition();
                    position.y += yoffset;
                    target.setPosition(position);
                }
                target.setStance(((LifeMovement) move).getNewstate());
            }
        }
    }
}
