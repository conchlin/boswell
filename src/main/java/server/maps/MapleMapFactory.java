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
import java.awt.Rectangle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataTool;
import server.PortalFactory;
import server.life.AbstractLoadedMapleLife;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MaplePlayerNPC;
import server.life.MaplePlayerNPCFactory;
import server.partyquest.GuardianSpawnPoint;
import net.database.DatabaseConnection;
import tools.Rect;
import tools.Size;
import tools.StringUtil;

public class MapleMapFactory {

    private static Map<Integer, Float> mapRecoveryRate = new HashMap<>();

    private MapleDataProvider source;
    private MapleData nameData;
    /*private EventInstanceManager event;*/
    private Map<Integer, MapleMap> maps = new HashMap<>();
    private ReadLock mapsRLock;
    private WriteLock mapsWLock;
    private int channel, world;
    private final Rect mbr;

    public MapleMapFactory(/*EventInstanceManager eim,*/ MapleDataProvider source, MapleDataProvider stringSource, int world, int channel) {
        this.source = source;
        this.nameData = stringSource.getData("Map.img");
        this.world = world;
        this.channel = channel;
        //this.event = eim;

        ReentrantReadWriteLock rrwl = new MonitoredReentrantReadWriteLock(MonitoredLockType.MAP_FACTORY);
        this.mapsRLock = rrwl.readLock();
        this.mapsWLock = rrwl.writeLock();
        this.mbr = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public MapleMap resetMap(int mapid) {
        mapsWLock.lock();
        try {
            maps.remove(Integer.valueOf(mapid));
        } finally {
            mapsWLock.unlock();
        }

        return getMap(mapid);
    }

    private void loadLifeFromWz(MapleMap map, MapleData mapData) {
        for (MapleData life : mapData.getChildByPath("life")) {
            life.getName();
            String id = MapleDataTool.getString(life.getChildByPath("id"));
            String type = MapleDataTool.getString(life.getChildByPath("type"));
            int team = MapleDataTool.getInt("team", life, -1);
            if (map.isCPQMap2() && type.equals("m")) {
                if ((Integer.parseInt(life.getName()) % 2) == 0) {
                    team = 0;
                } else {
                    team = 1;
                }
            }
            int cy = MapleDataTool.getInt(life.getChildByPath("cy"));
            MapleData dF = life.getChildByPath("f");
            int f = (dF != null) ? MapleDataTool.getInt(dF) : 0;
            int fh = MapleDataTool.getInt(life.getChildByPath("fh"));
            int rx0 = MapleDataTool.getInt(life.getChildByPath("rx0"));
            int rx1 = MapleDataTool.getInt(life.getChildByPath("rx1"));
            int x = MapleDataTool.getInt(life.getChildByPath("x"));
            int y = MapleDataTool.getInt(life.getChildByPath("y"));
            int hide = MapleDataTool.getInt("hide", life, 0);
            int mobTime = MapleDataTool.getInt("mobTime", life, 0);
            
            loadLifeRaw(map, Integer.parseInt(id), type, cy, f, fh, rx0, rx1, x, y, hide, mobTime, team);
        }
    }

    private void loadLifeFromDb(MapleMap map) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM plife WHERE map = ? and world = ?");) {
            ps.setInt(1, map.getId());
            ps.setInt(2, map.getWorld());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("life");
                    String type = rs.getString("type");
                    int cy = rs.getInt("cy");
                    int f = rs.getInt("f");
                    int fh = rs.getInt("fh");
                    int rx0 = rs.getInt("rx0");
                    int rx1 = rs.getInt("rx1");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int hide = rs.getInt("hide");
                    int mobTime = rs.getInt("mobtime");
                    int team = rs.getInt("team");

                    loadLifeRaw(map, id, type, cy, f, fh, rx0, rx1, x, y, hide, mobTime, team);
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    private void loadLifeRaw(MapleMap map, int id, String type, int cy, int f, int fh, int rx0, int rx1, int x, int y, int hide, int mobTime, int team) {
        AbstractLoadedMapleLife myLife = loadLife(id, type, cy, f, fh, rx0, rx1, x, y, hide);
        if (myLife instanceof MapleMonster monster) {
            if (mobTime == -1) { //does not respawn, force spawn once
                map.spawnMonster(monster);
            } else {
                map.addMonsterSpawn(monster, mobTime, team);
            }

            //should the map be reseted, use allMonsterSpawn list of monsters to spawn them again
            map.addAllMonsterSpawn(monster, mobTime, team);
        } else {
            map.addMapObject(myLife);
        }
    }

    private synchronized MapleMap loadMapFromWz(int mapid, Integer omapid, boolean cache) {
        MapleMap map;

        if (mapid < 0) { //performance ftw?
            return null;
        }

        if (cache) {
            mapsRLock.lock();
            try {
                map = maps.get(omapid);
            } finally {
                mapsRLock.unlock();
            }

            if (map != null) {
                return map;
            }
        }

        String mapName = getMapName(mapid);
        MapleData mapData = source.getData(mapName);    // source.getData issue with giving nulls in rare ocasions found thanks to MedicOP
        MapleData infoData = mapData.getChildByPath("info");
        String link = MapleDataTool.getString(infoData.getChildByPath("link"), "");

        if (!link.equals("")) { //nexon made hundreds of dojo maps so to reduce the size they added links.
            mapName = getMapName(Integer.parseInt(link));
            mapData = source.getData(mapName);
        }
        
        map = new MapleMap(mapid, world, channel, MapleDataTool.getInt("returnMap", infoData));
        //map.setEventInstance(event);

        String onFirstEnter = MapleDataTool.getString(infoData.getChildByPath("onFirstUserEnter"), String.valueOf(mapid));
        map.setOnFirstUserEnter(onFirstEnter.equals("") ? String.valueOf(mapid) : onFirstEnter);

        String onEnter = MapleDataTool.getString(infoData.getChildByPath("onUserEnter"), String.valueOf(mapid));
        map.setOnUserEnter(onEnter.equals("") ? String.valueOf(mapid) : onEnter);
        //map.setBGM(MapleDataTool.getString(mapData.getChildByPath("info/bgm")));
        map.setFieldLimit(MapleDataTool.getInt(infoData.getChildByPath("fieldLimit"), 0));
        map.setMobInterval((short) MapleDataTool.getInt(infoData.getChildByPath("createMobInterval"), 5000));
        map.setMobRate(MapleDataTool.getFloat(infoData.getChildByPath("mobRate")));
        
        boolean VRLimit = mapData.getChildByPath("info/VRLimit") != null;
        if (VRLimit) {
            map.setVRTop(MapleDataTool.getInt(mapData.getChildByPath("info/VRTop"), 0));
            map.setVRBottom(MapleDataTool.getInt(mapData.getChildByPath("info/VRBottom"), 0));
            map.setVRLeft(MapleDataTool.getInt(mapData.getChildByPath("info/VRLeft"), 0));
            map.setVRRight(MapleDataTool.getInt(mapData.getChildByPath("info/VRRight"), 0));
            if (map.getVRLeft() > 0 && mbr.left < map.getVRLeft() + 20)
                mbr.left = map.getVRLeft() + 20;
            if (map.getVRRight() > 0 && mbr.right > map.getVRRight())
                mbr.right = map.getVRRight();
            if (map.getVRTop() > 0 && mbr.top < map.getVRTop() + 65)
                mbr.top = map.getVRTop() + 65;
            if (map.getVRBottom() > 0 && mbr.bottom > map.getVRBottom())
                mbr.bottom = map.getVRBottom();
        }
        
        mbr.inflateRect(10, 10);
        map.setMapSize(new Size(mbr.left - mbr.right, mbr.bottom - mbr.top));
        
        //map.setRecoveryRate(MapleDataTool.getFloat(mapData.getChildByPath("info/recovery/"), 1));
        PortalFactory portalFactory = new PortalFactory();
        for (MapleData portal : mapData.getChildByPath("portal")) {
            map.addPortal(portalFactory.makePortal(MapleDataTool.getInt(portal.getChildByPath("pt")), portal));
        }
        MapleData timeMob = infoData.getChildByPath("timeMob");
        if (timeMob != null) {
          map.setTimeMob(MapleDataTool.getInt(timeMob.getChildByPath("id")),
                  MapleDataTool.getString(timeMob.getChildByPath("message")));
        }

        List<MapleFoothold> allFootholds = new LinkedList<>();
        Point lBound = new Point();
        Point uBound = new Point();
        for (MapleData footRoot : mapData.getChildByPath("foothold")) {
            for (MapleData footCat : footRoot) {
                for (MapleData footHold : footCat) {
                    int x1 = MapleDataTool.getInt(footHold.getChildByPath("x1"));
                    int y1 = MapleDataTool.getInt(footHold.getChildByPath("y1"));
                    int x2 = MapleDataTool.getInt(footHold.getChildByPath("x2"));
                    int y2 = MapleDataTool.getInt(footHold.getChildByPath("y2"));
                    MapleFoothold fh = new MapleFoothold(new Point(x1, y1), new Point(x2, y2),
                            Integer.parseInt(footHold.getName()));
                    fh.setPrev(MapleDataTool.getInt(footHold.getChildByPath("prev")));
                    fh.setNext(MapleDataTool.getInt(footHold.getChildByPath("next")));
                    if (fh.getX1() < lBound.x) {
                        lBound.x = fh.getX1();
                    }
                    if (fh.getX2() > uBound.x) {
                        uBound.x = fh.getX2();
                    }
                    if (fh.getY1() < lBound.y) {
                        lBound.y = fh.getY1();
                    }
                    if (fh.getY2() > uBound.y) {
                        uBound.y = fh.getY2();
                    }
                    allFootholds.add(fh);
                }
            }
        }
        MapleFootholdTree fTree = new MapleFootholdTree(lBound, uBound);
        for (MapleFoothold fh : allFootholds) {
            fTree.insert(fh);
        }
        map.setFootholds(fTree);
        if (mapData.getChildByPath("area") != null) {
            for (MapleData area : mapData.getChildByPath("area")) {
                int x1 = MapleDataTool.getInt(area.getChildByPath("x1"));
                int y1 = MapleDataTool.getInt(area.getChildByPath("y1"));
                int x2 = MapleDataTool.getInt(area.getChildByPath("x2"));
                int y2 = MapleDataTool.getInt(area.getChildByPath("y2"));
                map.addMapleArea(new Rectangle(x1, y1, (x2 - x1), (y2 - y1)));
            }
        }
         if (mapData.getChildByPath("seat") != null) {
            int seats = mapData.getChildByPath("seat").getChildren().size();
            map.setSeats(seats);
        }
        //if (event == null) {
            try (Connection con = DatabaseConnection.getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs WHERE map = ? AND world = ?")) {
                    ps.setInt(1, omapid);
                    ps.setInt(2, world);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            map.addPlayerNPCMapObject(new MaplePlayerNPC(rs));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            List<MaplePlayerNPC> dnpcs = MaplePlayerNPCFactory.getDeveloperNpcsFromMapid(mapid);
            if (dnpcs != null) {
                for (MaplePlayerNPC dnpc : dnpcs) {
                    map.addPlayerNPCMapObject(dnpc);
                }
            }
        //}

        loadLifeFromWz(map, mapData);
        loadLifeFromDb(map);

        if (map.isCPQMap()) {
            MapleData mcData = mapData.getChildByPath("monsterCarnival");
            if (mcData != null) {
                map.setDeathCP(MapleDataTool.getIntConvert("deathCP", mcData, 0));
                map.setMaxMobs(MapleDataTool.getIntConvert("mobGenMax", mcData, 0));
                map.setTimeDefault(MapleDataTool.getIntConvert("timeDefault", mcData, 0));
                map.setTimeExpand(MapleDataTool.getIntConvert("timeExpand", mcData, 0));
                map.setMaxReactors(MapleDataTool.getIntConvert("guardianGenMax", mcData, 0));
                MapleData guardianGenData = mcData.getChildByPath("guardianGenPos");
                for (MapleData node : guardianGenData.getChildren()) {
                    GuardianSpawnPoint pt = new GuardianSpawnPoint(new Point(MapleDataTool.getIntConvert("x", node), MapleDataTool.getIntConvert("y", node)));
                    pt.setTeam(MapleDataTool.getIntConvert("team", node, -1));
                    pt.setTaken(false);
                    map.addGuardianSpawnPoint(pt);
                }
                if (mcData.getChildByPath("skill") != null) {
                    for (MapleData area : mcData.getChildByPath("skill")) {
                        map.addSkillId(MapleDataTool.getInt(area));
                    }
                }

                if (mcData.getChildByPath("mob") != null) {
                    for (MapleData area : mcData.getChildByPath("mob")) {
                        map.addMobSpawn(MapleDataTool.getInt(area.getChildByPath("id")), MapleDataTool.getInt(area.getChildByPath("spendCP")));
                    }
                }
            }

        }

        if (mapData.getChildByPath("reactor") != null) {
            for (MapleData reactor : mapData.getChildByPath("reactor")) {
                String id = MapleDataTool.getString(reactor.getChildByPath("id"));
                if (id != null) {
                    MapleReactor newReactor = loadReactor(reactor, id, (byte) MapleDataTool.getInt(reactor.getChildByPath("f"), 0));
                    map.spawnReactor(newReactor);
                }
            }
        }
        try {
            map.setMapName(MapleDataTool.getString("mapName", nameData.getChildByPath(getMapStringName(omapid)), ""));
            map.setStreetName(MapleDataTool.getString("streetName", nameData.getChildByPath(getMapStringName(omapid)), ""));
        } catch (Exception e) {
            if (omapid / 1000 != 1020) {     // explorer job introducion scenes
                e.printStackTrace();
                System.err.println("Not found mapid " + omapid);
            }

            map.setMapName("");
            map.setStreetName("");
        }

        map.setClock(mapData.getChildByPath("clock") != null);
        boolean everlast = mapData.getChildByPath("info/everlast") != null;
        if (everlast)
            everlast = MapleDataTool.getIntConvert("info/everlast", mapData, 0) == 1;
        map.setEverlast(everlast);
        map.setTown(infoData.getChildByPath("town") != null);
        map.setHPDec(MapleDataTool.getIntConvert("decHP", infoData, 0));
        map.setHPDecProtect(MapleDataTool.getIntConvert("protectItem", infoData, 0));
        map.setForcedReturnMap(MapleDataTool.getInt(infoData.getChildByPath("forcedReturn"), 999999999));
        map.setBoat(mapData.getChildByPath("shipObj") != null);
        map.setTimeLimit(MapleDataTool.getIntConvert("timeLimit", infoData, -1));
        map.setFieldType(MapleDataTool.getIntConvert("fieldType", infoData, 0));
        map.setPartyBonusRate(MapleDataTool.getInt(mapData.getChildByPath("info/partyBonusR"), 0));
        map.setMobCapacity(MapleDataTool.getIntConvert("fixedMobCapacity",
                infoData, 500));//Is there a map that contains more than 500 mobs?

        MapleData recData = infoData.getChildByPath("recovery");
        if (recData != null) {
            float recoveryRate = MapleDataTool.getFloat(recData);
            mapRecoveryRate.put(mapid, recoveryRate);
        }

        HashMap<Integer, Integer> backTypes = new HashMap<>();
        try {
            for (MapleData layer : mapData.getChildByPath("back")) { // yolo
                int layerNum = Integer.parseInt(layer.getName());
                int btype = MapleDataTool.getInt(layer.getChildByPath("type"), 0);

                backTypes.put(layerNum, btype);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // swallow cause I'm cool
        }

        map.setBackgroundTypes(backTypes);
        //map.generateMapDropRangeCache();

        if (cache) {
            mapsWLock.lock();
            try {
                maps.put(omapid, map);
            } finally {
                mapsWLock.unlock();
            }
        }

        return map;
    }

    public MapleMap getMap(int mapid) {
        Integer omapid = Integer.valueOf(mapid);
        MapleMap map;

        mapsRLock.lock();
        try {
            map = maps.get(omapid);
        } finally {
            mapsRLock.unlock();
        }

        return (map != null) ? map : loadMapFromWz(mapid, omapid, true);
    }
    
    public MapleMap getDisposableMap(int mapid) {
        return loadMapFromWz(mapid, mapid, false);
    }

    public boolean isMapLoaded(int mapId) {
        mapsRLock.lock();
        try {
            return maps.containsKey(mapId);
        } finally {
            mapsRLock.unlock();
        }
    }

    private AbstractLoadedMapleLife loadLife(int id, String type, int cy, int f, int fh, int rx0, int rx1, int x, int y, int hide) {
        AbstractLoadedMapleLife myLife = MapleLifeFactory.getLife(id, type);
        myLife.setCy(cy);
        myLife.setF(f);
        myLife.setFh(fh);
        myLife.setRx0(rx0);
        myLife.setRx1(rx1);
        myLife.setPosition(new Point(x, y));
        if (hide == 1) {
            myLife.setHide(true);
        }
        return myLife;
    }

    private MapleReactor loadReactor(MapleData reactor, String id, final byte FacingDirection) {
        MapleReactor myReactor = new MapleReactor(MapleReactorFactory.getReactor(Integer.parseInt(id)), Integer.parseInt(id));
        int x = MapleDataTool.getInt(reactor.getChildByPath("x"));
        int y = MapleDataTool.getInt(reactor.getChildByPath("y"));
        myReactor.setFacingDirection(FacingDirection);
        myReactor.setPosition(new Point(x, y));
        myReactor.setDelay(MapleDataTool.getInt(reactor.getChildByPath("reactorTime")) * 1000);
        myReactor.setName(MapleDataTool.getString(reactor.getChildByPath("name"), ""));
        myReactor.resetReactorActions(0);
        return myReactor;
    }

    private String getMapName(int mapid) {
        String mapName = StringUtil.getLeftPaddedStr(Integer.toString(mapid), '0', 9);
        StringBuilder builder = new StringBuilder("Map/Map");
        int area = mapid / 100000000;
        builder.append(area);
        builder.append("/");
        builder.append(mapName);
        builder.append(".img");
        mapName = builder.toString();
        return mapName;
    }

    private String getMapStringName(int mapid) {
        StringBuilder builder = new StringBuilder();
        if (mapid < 100000000) {
            builder.append("maple");
        } else if (mapid >= 100000000 && mapid < 200000000) {
            builder.append("victoria");
        } else if (mapid >= 200000000 && mapid < 300000000) {
            builder.append("ossyria");
        } else if (mapid >= 300000000 && mapid < 400000000) {
            builder.append("elin");
        } else if (mapid >= 540000000 && mapid < 560000000) {
            builder.append("singapore");
        } else if (mapid >= 600000000 && mapid < 620000000) {
            builder.append("MasteriaGL");
        } else if (mapid >= 677000000 && mapid < 677100000) {
            builder.append("Episode1GL");
        } else if (mapid >= 670000000 && mapid < 682000000) {
            if ((mapid >= 674030000 && mapid < 674040000) || (mapid >= 680100000 && mapid < 680200000)) {
                builder.append("etc");
            } else {
                builder.append("weddingGL");
            }
        } else if (mapid >= 682000000 && mapid < 683000000) {
            builder.append("HalloweenGL");
        } else if (mapid >= 683000000 && mapid < 684000000) {
            builder.append("event");
        } else if (mapid >= 800000000 && mapid < 900000000) {
            if ((mapid >= 889100000 && mapid < 889200000)) {
                builder.append("etc");
            } else {
                builder.append("jp");
            }
        } else {
            builder.append("etc");
        }
        builder.append("/").append(mapid);
        return builder.toString();
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public void setWorld(int world) {
        this.channel = world;
    }

    public Map<Integer, MapleMap> getMaps() {
        mapsRLock.lock();
        try {
            return new HashMap<>(maps);
        } finally {
            mapsRLock.unlock();
        }
    }

    public void dispose() {
        Collection<MapleMap> mapValues = getMaps().values();

        for (MapleMap map : mapValues) {
            map.dispose();
        }

        //this.event = null;
    }

    public static float getMapRecoveryRate(int mapid) {
        Float recRate = mapRecoveryRate.get(mapid);
        return recRate != null ? recRate : 1.0f;
    }
}
