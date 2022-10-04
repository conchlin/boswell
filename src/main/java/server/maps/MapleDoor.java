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
package server.maps;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;

import client.MapleClient;
import client.MapleCharacter;
import net.server.world.MaplePartyCharacter;
import network.packet.TownPortalPool;
import network.packet.WvsContext;
import server.MaplePortal;
import server.MapleStatEffect;
import tools.MaplePacketCreator;

/**
 *
 * @author Matze
 * @author Zygon
 */
public class MapleDoor extends AbstractMapleMapObject {

    private MapleMap town, target;
    private MapleCharacter owner;
    private MapleDoorObject townDoor, targetDoor;
    private MapleStatEffect doorEffect; // hold this to make sure we can easily kill the old door
    private ArrayList<MaplePortal> freePortals = new ArrayList<>();
    private long creationTime = System.currentTimeMillis();

    public MapleDoor(MapleStatEffect effect, MapleMap target, MapleCharacter owner, Point pTarget) {
        this.doorEffect = effect;
        this.owner = owner;
        this.town = target.getReturnMap();
        this.populatePortals();
        this.target = target;

        townDoor = new MapleDoorObject(this, null, true);
        targetDoor = new MapleDoorObject(this, pTarget, false);
    }

    // only required to destroy portal really
    public void destroyDoor() {
        owner.getClient().announce(TownPortalPool.Packet.onTownPortalRemoved(owner.getId()));
        owner.getClient().announce(MaplePacketCreator.removePortal());

        owner.silentPartyUpdate();
    }

    public long getCreationTime() {
        return creationTime;
    }

    public boolean canCreateNew() {
        return System.currentTimeMillis() - creationTime >= 5000;
    }

    public MapleStatEffect getDoorEffect() {
        return doorEffect;
    }

    public MapleMap getTown() {
        return town;
    }

    public MapleMap getTarget() {
        return target;
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public Point getTownPosition() {
        return getTownPortal().getPosition();
    }

    public Point getTargetPosition() {
        return targetDoor.getPosition();
    }

    public MapleDoorObject getTownDoor() {
        return townDoor;
    }

    public MapleDoorObject getTargetDoor() {
        return targetDoor;
    }

    private MaplePortal getTownPortal() {
        // compute on fly
        if (owner.getParty() != null) {
            int index = 0;
            // portal for party member will appear at portl: (128 + n)
            // where n = join order of party
            for (MaplePartyCharacter mpc : owner.getParty().getMembers()) {
                if (mpc.getId() == owner.getId()) break;

                index++;
            }

            // looking at # of portals that are free,
            if (index < freePortals.size() - 1) {
                return freePortals.get(index);
            } else if (freePortals.size() > 0) {
                return freePortals.get(freePortals.size() - 1);
            }
        } else if (freePortals.size() > 0)
            return freePortals.get(0);
        return null;
    }

    public int getTargetOid() {
        return targetDoor.getObjectId();
    }

    public int getTownOid() {
        return townDoor.getObjectId();
    }

    private void populatePortals() {
        for (MaplePortal port : town.getPortals()) {
            if (port.getType() == MaplePortal.DOOR_PORTAL) {
                freePortals.add(port);
            }
        }

        freePortals.sort(Comparator.comparingInt(MaplePortal::getId));
    }

    public void warp(MapleCharacter chr, boolean toTown) {
        if (chr == owner || owner.getParty() != null && owner.getParty().containsMembers(chr.getMPC())) {
            if (!toTown) {
                chr.changeMap(target, getTownPortal()); //  getTargetPosition()
            } else {
                chr.changeMap(town, getTownPortal());
            }
        } else {
            chr.getClient().announce(WvsContext.Packet.enableActions());
        }
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.DOOR;
    }

    @Override
    public void sendSpawnData(MapleClient c) {
        if (c.getPlayer().getMapId() == town.getId()) {
            townDoor.sendSpawnData(c);
        } else {
            targetDoor.sendSpawnData(c);
        }
        //return true;
    }

    @Override
    public void sendDestroyData(MapleClient c) {
        if (c.getPlayer().getMapId() == town.getId()) {
            townDoor.sendDestroyData(c);
        } else {
            targetDoor.sendDestroyData(c);
        }
    }

    public class MapleDoorObject extends AbstractMapleMapObject {

        private MapleDoor door;
        private boolean town;

        private MapleDoorObject() {
        }

        private MapleDoorObject(MapleDoor door, Point pos, boolean town) {
            this.door = door;
            this.town = town;

            if (pos != null)
                this.setPosition(pos);
        }

        @Override
        public Point getPosition() {
            if (!town) return super.getPosition();

            return getTownPosition();
        }

        public void warp(MapleCharacter c, boolean mode) {
            // we should technically know the "mode"
            // from this portal, but send it anyways
            door.warp(c, mode);
        }

        public int getOwnerId() {
            return owner.getId();
        }

        public int getTarget() {
            return door.getTarget().getId();
        }

        public int getTown() {
            return door.getTown().getId();
        }

        public void initialSpawn(MapleClient c) {
            c.announce(TownPortalPool.Packet.onTownPortalCreated(owner.getId(), getPosition(), false));

            if (c.getPlayer().getId() == owner.getId()) {
                c.announce(MaplePacketCreator.spawnPortal(getTown(), getTarget(), getPosition()));
            }
        }

        @Override
        public MapleMapObjectType getType() {
            return MapleMapObjectType.DOOR;
        }

        @Override
        public void sendSpawnData(MapleClient c) {
            if (!town) {
                c.announce(TownPortalPool.Packet.onTownPortalCreated(owner.getId(), getPosition(), true));
            } else if (owner.getParty() == null &&
                    c.getPlayer().getId() == owner.getId()) {
                c.announce(TownPortalPool.Packet.onTownPortalCreated(owner.getId(), getPosition(), true));
            }
            //return true;
        }

        @Override
        public void sendDestroyData(MapleClient c) {
            c.announce(TownPortalPool.Packet.onTownPortalRemoved(owner.getId()));
        }

    }

}
