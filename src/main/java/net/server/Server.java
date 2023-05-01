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
package net.server;

import client.MapleCharacter;
import client.MapleClient;
import client.command.CommandsExecutor;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.newyear.NewYearCardRecord;
import constants.GameConstants;
import constants.ItemConstants;
import constants.OpcodeConstants;
import constants.ServerConstants;
import net.MapleServerHandler;
import net.database.Statements;
import net.mina.MapleCodecFactory;
import net.server.audit.ThreadTracker;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import net.server.channel.Channel;
import net.server.coordinator.MapleSessionCoordinator;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.rest.HttpServer;
import net.server.rest.HttpWorker;
import net.server.worker.*;
import net.server.world.World;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import server.*;
import server.cashshop.CashItemFactory;
import server.life.MaplePlayerNPCFactory;
import server.maps.NostalgicMap;
import server.quest.MapleQuest;
import server.skills.SkillFactory;
import tools.AutoJCE;
import database.DatabaseConnection;
import tools.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.Security;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class Server {

    private static Server instance = null;

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    private static HttpServer httpServer;
    public static HttpWorker httpWorker;

    private static final Set<Integer> activeFly = new HashSet<>();
    private static final Map<Integer, Integer> couponRates = new HashMap<>(30);
    private static final List<Integer> activeCoupons = new LinkedList<>();

    private IoAcceptor acceptor;
    private List<Map<Integer, String>> channels = new LinkedList<>();
    private List<World> worlds = new ArrayList<>();
    private final Properties subnetInfo = new Properties();
    private final Map<Integer, Set<Integer>> accountChars = new HashMap<>();
    private final Map<Integer, Short> accountCharacterCount = new HashMap<>();
    private final Map<Integer, Integer> worldChars = new HashMap<>();
    private final Map<String, Integer> transitioningChars = new HashMap<>();
    private List<Pair<Integer, String>> worldRecommendedList = new LinkedList<>();
    private final Map<Integer, MapleGuild> guilds = new HashMap<>(100);
    private final Map<MapleClient, Long> inLoginState = new HashMap<>(100);

    private final PlayerBuffStorage buffStorage = new PlayerBuffStorage();
    private final Map<Integer, MapleAlliance> alliances = new HashMap<>(100);
    private final Map<Integer, NewYearCardRecord> newyears = new HashMap<>();
    private final List<MapleClient> processDiseaseAnnouncePlayers = new LinkedList<>();
    private final List<MapleClient> registeredDiseaseAnnouncePlayers = new LinkedList<>();

    private final Lock srvLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.SERVER);
    private final Lock disLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.SERVER_DISEASES);

    private final ReentrantReadWriteLock wldLock = new MonitoredReentrantReadWriteLock(MonitoredLockType.SERVER_WORLDS, true);
    private final ReadLock wldRLock = wldLock.readLock();
    private final WriteLock wldWLock = wldLock.writeLock();

    private final ReentrantReadWriteLock lgnLock = new MonitoredReentrantReadWriteLock(MonitoredLockType.SERVER_LOGIN, true);
    private final ReadLock lgnRLock = lgnLock.readLock();
    private final WriteLock lgnWLock = lgnLock.writeLock();

    private final AtomicLong currentTime = new AtomicLong(0);
    private long serverCurrentTime = 0;

    private boolean availableDeveloperRoom = false;
    private boolean online = false;
    public static long uptime = System.currentTimeMillis();

    public int getCurrentTimestamp() {
        return (int) (Server.getInstance().getCurrentTime() - Server.uptime);
    }

    public long getCurrentTime() {  // returns a slightly delayed time value, under frequency of UPDATE_INTERVAL
        return serverCurrentTime;
    }

    public void updateCurrentTime() {
        serverCurrentTime = currentTime.addAndGet(ServerConstants.UPDATE_INTERVAL);
    }

    public long forceUpdateCurrentTime() {
        long timeNow = System.currentTimeMillis();
        serverCurrentTime = timeNow;
        currentTime.set(timeNow);

        return timeNow;
    }

    public boolean isOnline() {
        return online;
    }

    public List<Pair<Integer, String>> worldRecommendedList() {
        return worldRecommendedList;
    }

    public void setNewYearCard(NewYearCardRecord nyc) {
        newyears.put(nyc.getId(), nyc);
    }

    public NewYearCardRecord getNewYearCard(int cardid) {
        return newyears.get(cardid);
    }

    public NewYearCardRecord removeNewYearCard(int cardid) {
        return newyears.remove(cardid);
    }

    public void setAvailableDeveloperRoom() {
        availableDeveloperRoom = true;
    }

    public boolean canEnterDeveloperRoom() {
        return availableDeveloperRoom;
    }

    private void loadPlayerNpcMapStepFromDb() {
        List<World> wlist = this.getWorlds();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs_field")) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int world = rs.getInt("world"),
                            map = rs.getInt("map"),
                            step = rs.getInt("step"),
                            podium = rs.getInt("podium");
                    World w = wlist.get(world);
                    if (w != null) w.setPlayerNpcMapData(map, step, podium);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public World getWorld(int id) {
        wldRLock.lock();
        try {
            try {
                return worlds.get(id);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        } finally {
            wldRLock.unlock();
        }
    }

    public List<World> getWorlds() {
        wldRLock.lock();
        try {
            return Collections.unmodifiableList(worlds);
        } finally {
            wldRLock.unlock();
        }
    }

    public int getWorldsSize() {
        wldRLock.lock();
        try {
            return worlds.size();
        } finally {
            wldRLock.unlock();
        }
    }

    public Channel getChannel(int world, int channel) {
        try {
            return this.getWorld(world).getChannel(channel);
        } catch (NullPointerException npe) {
            return null;
        }
    }

    public List<Channel> getChannelsFromWorld(int world) {
        try {
            return this.getWorld(world).getChannels();
        } catch (NullPointerException npe) {
            return new ArrayList<>(0);
        }
    }

    public List<Channel> getAllChannels() {
        try {
            List<Channel> channelz = new ArrayList<>();
            for (World world : this.getWorlds()) {
                for (Channel ch : world.getChannels()) {
                    channelz.add(ch);
                }
            }
            return channelz;
        } catch (NullPointerException npe) {
            return new ArrayList<>(0);
        }
    }

    public Set<Integer> getOpenChannels(int world) {
        wldRLock.lock();
        try {
            return new HashSet<>(channels.get(world).keySet());
        } finally {
            wldRLock.unlock();
        }
    }

    private String getIP(int world, int channel) {
        wldRLock.lock();
        try {
            return channels.get(world).get(channel);
        } finally {
            wldRLock.unlock();
        }
    }

    public String[] getInetSocket(int world, int channel) {
        try {
            return getIP(world, channel).split(":");
        } catch (Exception e) {
            return null;
        }
    }


    private void dumpData() {
        wldWLock.lock();
        try {
            System.out.println(worlds);
            System.out.println(channels);
            System.out.println(worldRecommendedList);
            System.out.println();
            System.out.println("---------------------");
        } finally {
            wldWLock.unlock();
        }
    }

    public int addChannel(int worldid) {
        wldWLock.lock();
        try {
            if (worldid >= worlds.size()) return -3;

            Map<Integer, String> worldChannels = channels.get(worldid);
            if (worldChannels == null) return -3;

            int channelid = worldChannels.size();
            if (channelid >= ServerConstants.CHANNEL_SIZE) return -2;

            Properties p = loadWorldINI();
            if (p == null) {
                return -1;
            }

            channelid++;
            World world = this.getWorld(worldid);
            Channel channel = new Channel(worldid, channelid, getCurrentTime());

            channel.setServerMessage(p.getProperty("whyamirecommended" + worldid));

            world.addChannel(channel);
            worldChannels.put(channelid, channel.getIP());

            return channelid;
        } finally {
            wldWLock.unlock();
        }
    }

    public int addWorld() {
        Properties p = loadWorldINI();
        if (p == null) return -2;

        int newWorld = initWorld(p);
        if (newWorld > -1) {
            Set<Integer> accounts;
            lgnRLock.lock();
            try {
                accounts = new HashSet<>(accountChars.keySet());
            } finally {
                lgnRLock.unlock();
            }

            for (Integer accId : accounts) {
                loadAccountCharactersView(accId, 0, newWorld);
            }
        }

        return newWorld;
    }

    private static int getWorldProperty(Properties p, String property, int wid, double defaultValue) {
        String content = p.getProperty(property + wid);
        return (int) (content != null ? Integer.parseInt(content) : defaultValue);
    }

    private int initWorld(Properties p) {
        wldWLock.lock();
        try {
            int i = worlds.size();

            if (i >= ServerConstants.WLDLIST_SIZE) {
                return -1;
            }

            System.out.println("Starting world " + i);
            double exprate = getWorldProperty(p, "exprate", i, ServerConstants.EXP_RATE);
            double mesorate = getWorldProperty(p, "mesorate", i, ServerConstants.MESO_RATE);
            double droprate = getWorldProperty(p, "droprate", i, ServerConstants.DROP_RATE);
            double bossdroprate = getWorldProperty(p, "bossdroprate", i, ServerConstants.BOSS_DROP_RATE);
            double questrate = getWorldProperty(p, "questrate", i, ServerConstants.QUEST_RATE);

            World world = new World(i,
                    Integer.parseInt(p.getProperty("flag" + i)),
                    p.getProperty("eventmessage" + i),
                    exprate, droprate, bossdroprate, mesorate, questrate);

            worldRecommendedList.add(new Pair<>(i, p.getProperty("whyamirecommended" + i)));
            worlds.add(world);

            Map<Integer, String> channelInfo = new HashMap<>();
            long bootTime = getCurrentTime();
            for (int j = 1; j <= Integer.parseInt(p.getProperty("channels" + i)); j++) {
                int channelid = j;
                Channel channel = new Channel(i, channelid, bootTime);

                world.addChannel(channel);
                channelInfo.put(channelid, channel.getIP());
            }

            channels.add(i, channelInfo);

            world.setServerMessage(p.getProperty("servermessage" + i));
            System.out.println("Finished loading world " + i + "\r\n");

            return i;
        } finally {
            wldWLock.unlock();
        }
    }

    public boolean removeChannel(int worldid) {   //lol don't!
        wldWLock.lock();
        try {
            if (worldid >= worlds.size()) return false;

            World world = worlds.get(worldid);
            if (world != null) {
                int channel = world.removeChannel();

                Map<Integer, String> m = channels.get(worldid);
                if (m != null) m.remove(channel);

                return channel > -1;
            }
        } finally {
            wldWLock.unlock();
        }

        return false;
    }

    public boolean removeWorld() {   //lol don't!
        World w;
        int worldid;

        wldRLock.lock();
        try {
            worldid = worlds.size() - 1;
            if (worldid < 0) {
                return false;
            }

            w = worlds.get(worldid);
        } finally {
            wldRLock.unlock();
        }

        if (w == null || !w.canUninstall()) {
            return false;
        }

        wldWLock.lock();
        try {
            if (worldid == worlds.size() - 1) {
                w.shutdown();

                worlds.remove(worldid);
                channels.remove(worldid);
                worldRecommendedList.remove(worldid);
            } else {
                return false;
            }
        } finally {
            wldWLock.unlock();
        }

        return true;
    }

    private void resetServerWorlds() {
        wldWLock.lock();
        try {
            worlds.clear();
            channels.clear();
            worldRecommendedList.clear();
        } finally {
            wldWLock.unlock();
        }
    }

    public static Properties loadWorldINI() {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream("./world.ini"));
            return p;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[SEVERE] Could not find/open 'world.ini'.");
            return null;
        }
    }

    private static long getTimeLeftForNextHour() {
        Calendar nextHour = Calendar.getInstance();
        nextHour.add(Calendar.HOUR, 1);
        nextHour.set(Calendar.MINUTE, 0);
        nextHour.set(Calendar.SECOND, 0);

        return Math.max(0, nextHour.getTimeInMillis() - System.currentTimeMillis());
    }

    private static long getTimeLeftForNextDay() {
        Calendar nextDay = Calendar.getInstance();
        nextDay.add(Calendar.DAY_OF_MONTH, 1);
        nextDay.set(Calendar.HOUR, 0);
        nextDay.set(Calendar.MINUTE, 0);
        nextDay.set(Calendar.SECOND, 0);

        return Math.max(0, nextDay.getTimeInMillis() - System.currentTimeMillis());
    }

    /*
      returns short value with january being 0 and decembee being 11
      */
    private static int getCurrentMonth() {
        int month = Calendar.getInstance().get(Calendar.MONTH);

        return month;
    }

    public int getCurrentSeason(short season) {
        switch (getCurrentMonth()) {
            case 11:
            case 0:
            case 1:
                season = 0; // winter
            case 2:
            case 3:
            case 4:
                season = 1; // spring
            case 5:
            case 6:
            case 7:
                season = 2; // summer
            case 8:
            case 9:
            case 10:
                season = 3; // fall
            default:
                break;
        }
        return season;
    }

    public Map<Integer, Integer> getCouponRates() {
        return couponRates;
    }

    public static void cleanNxcodeCoupons(Connection con) throws SQLException {
        if (!ServerConstants.USE_CLEAR_OUTDATED_COUPONS) return;

        long timeClear = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000;

        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM nxcode WHERE expiration <= ?")) {
            ps.setLong(1, timeClear);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.isLast()) {
                    try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM nxcode_items WHERE codeid = ?")) {
                        while (rs.next()) {
                            ps2.setInt(1, rs.getInt("id"));
                            ps2.addBatch();
                        }
                        ps2.executeBatch();
                    }

                    try (PreparedStatement ps3 = con.prepareStatement("DELETE FROM nxcode WHERE expiration <= ?")) {
                        ps3.setLong(1, timeClear);
                        ps3.executeUpdate();
                    }
                }
            }
        }
    }

    private void loadCouponRates(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT couponid, rate FROM nxcoupons")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int cid = rs.getInt("couponid");
                    int rate = rs.getInt("rate");

                    couponRates.put(cid, rate);
                }
            }
        }
    }

    public List<Integer> getActiveCoupons() {
        synchronized (activeCoupons) {
            return activeCoupons;
        }
    }

    public void commitActiveCoupons() {
        for (World world : getWorlds()) {
            for (MapleCharacter chr : world.getPlayerStorage().getAllCharacters()) {
                if (!chr.isLoggedin()) continue;

                chr.updateCouponRates();
            }
        }
    }

    public void toggleCoupon(Integer couponId) {
        if (ItemConstants.isRateCoupon(couponId)) {
            synchronized (activeCoupons) {
                if (activeCoupons.contains(couponId)) {
                    activeCoupons.remove(couponId);
                } else {
                    activeCoupons.add(couponId);
                }

                commitActiveCoupons();
            }
        }
    }

    public void updateActiveCoupons() {
        synchronized (activeCoupons) {
            activeCoupons.clear();
            Calendar c = Calendar.getInstance();

            int weekDay = c.get(Calendar.DAY_OF_WEEK);
            int hourDay = c.get(Calendar.HOUR_OF_DAY);

            try (Connection con = DatabaseConnection.getConnection()) {
                int weekdayMask = (1 << weekDay);
                try (PreparedStatement ps = con.prepareStatement("SELECT couponid FROM nxcoupons WHERE (activeday & ?) = ? AND starthour <= ? AND endhour > ?")) {
                    ps.setInt(1, weekdayMask);
                    ps.setInt(2, weekdayMask);
                    ps.setInt(3, hourDay);
                    ps.setInt(4, hourDay);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            activeCoupons.add(rs.getInt("couponid"));
                        }
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void runAnnouncePlayerDiseasesSchedule() {
        List<MapleClient> processDiseaseAnnounceClients;
        disLock.lock();
        try {
            processDiseaseAnnounceClients = new LinkedList<>(processDiseaseAnnouncePlayers);
            processDiseaseAnnouncePlayers.clear();
        } finally {
            disLock.unlock();
        }

        while (!processDiseaseAnnounceClients.isEmpty()) {
            MapleClient c = processDiseaseAnnounceClients.remove(0);
            MapleCharacter player = c.getPlayer();
/*            if (player != null && player.isLoggedinWorld()) {
                player.announceDiseases();
            }*/
        }

        disLock.lock();
        try {
            // this is to force the system to wait for at least one complete tick before releasing disease info for the registered clients
            while (!registeredDiseaseAnnouncePlayers.isEmpty()) {
                MapleClient c = registeredDiseaseAnnouncePlayers.remove(0);
                processDiseaseAnnouncePlayers.add(c);
            }
        } finally {
            disLock.unlock();
        }
    }

    public void registerAnnouncePlayerDiseases(MapleClient c) {
        disLock.lock();
        try {
            registeredDiseaseAnnouncePlayers.add(c);
        } finally {
            disLock.unlock();
        }
    }

    public void init() {
        Properties p = loadWorldINI();
        if (p == null) {
            System.exit(0);
        }

        System.out.println("Boswell v" + ServerConstants.VERSION + " starting up.\r\n");

        if (ServerConstants.SHUTDOWNHOOK)
            Runtime.getRuntime().addShutdownHook(new Thread(shutdown(false)));

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // by calling getConnection it will initialize the database
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Update("accounts").set("loggedin", 0).execute(con);
            Statements.Update("characters").set("hasmerchant", false).execute(con);

            cleanNxcodeCoupons(con);
            loadCouponRates(con);
            updateActiveCoupons();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        //MaplePet.clearMissingPetsFromDb();

        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));

        ThreadManager.getInstance().start();
        TimerManager tMan = TimerManager.getInstance();
        tMan.start();
        tMan.register(tMan.purge(), ServerConstants.PURGING_INTERVAL);//Purging ftw...
        disconnectIdlesOnLoginTask();

        long timeLeft = getTimeLeftForNextHour();
        tMan.register(new CharacterDiseaseWorker(), ServerConstants.UPDATE_INTERVAL, ServerConstants.UPDATE_INTERVAL);
        tMan.register(new ReleaseLockWorker(), 2 * 60 * 1000, 2 * 60 * 1000);
        tMan.register(new CouponWorker(), ServerConstants.COUPON_INTERVAL, timeLeft);
        tMan.register(new LoginCoordinatorWorker(), 60 * 60 * 1000, timeLeft);
        tMan.register(new EventRecallCoordinatorWorker(), 60 * 60 * 1000, timeLeft);
        tMan.register(new LoginStorageWorker(), 2 * 60 * 1000, 2 * 60 * 1000);
        tMan.register(new DueyFredrickWorker(), 60 * 60 * 1000, timeLeft);
        tMan.register(new InvitationWorker(), 30 * 1000, 30 * 1000);

        tMan.scheduleAtMidnight(new BossLogWorker());
        tMan.scheduleAtMidnight(new BossQuestWorker());

        long timeToTake = System.currentTimeMillis();
        NostalgicMap.populateNostalgicMobList();

        timeToTake = System.currentTimeMillis();
        SkillFactory.loadAllSkills();
        System.out.println("Skills loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");

        timeToTake = System.currentTimeMillis();
        MapleItemInformationProvider.getInstance().loadAesthetics();
        MapleItemInformationProvider.getInstance().getAllItems();
        CashItemFactory.getSpecialCashItems();
        System.out.println("Items loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");

        timeToTake = System.currentTimeMillis();
        MapleQuest.loadAllQuest();
        System.out.println("Quest loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");
        
        timeToTake = System.currentTimeMillis();
        CashItemFactory.loadCashItems();
        System.out.println("Cashshop loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds \r\n");

        NewYearCardRecord.startPendingNewYearCardRequests();

        if (ServerConstants.USE_THREAD_TRACKER) ThreadTracker.getInstance().registerThreadTrackerTask();

        try {
            Integer worldCount = Math.min(GameConstants.WORLD_NAMES.length, Integer.parseInt(p.getProperty("worlds")));

            for (int i = 0; i < worldCount; i++) {
                initWorld(p);
            }

            MaplePlayerNPCFactory.loadFactoryMetadata();
            loadPlayerNpcMapStepFromDb();
        } catch (Exception e) {
            e.printStackTrace();//For those who get errors
            System.out.println("[SEVERE] Syntax error in 'world.ini'.");
            System.exit(0);
        }

        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
        acceptor.setHandler(new MapleServerHandler());
        try {
            acceptor.bind(new InetSocketAddress(8484));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (ServerConstants.HTTP_SERVER) {
            httpServer = new HttpServer();
            httpWorker = new HttpWorker();
        }
        System.out.println("Listening on port 8484\r\n\r\n");

        System.out.println("Boswell is now online.\r\n");
        online = true;

        MapleSkillbookInformationProvider.getInstance();
        OpcodeConstants.generateOpcodeNames();
        CommandsExecutor.getInstance();
    }

    public static void main(String[] args) {
        System.setProperty("wzpath", "wz");
        Security.setProperty("crypto.policy", "unlimited");
        AutoJCE.removeCryptographyRestrictions();
        Server.getInstance().init();
    }

    public Properties getSubnetInfo() {
        return subnetInfo;
    }

    public MapleAlliance getAlliance(int id) {
        synchronized (alliances) {
            if (alliances.containsKey(id)) {
                return alliances.get(id);
            }
            return null;
        }
    }

    public void addAlliance(int id, MapleAlliance alliance) {
        synchronized (alliances) {
            if (!alliances.containsKey(id)) {
                alliances.put(id, alliance);
            }
        }
    }

    public void disbandAlliance(int id) {
        synchronized (alliances) {
            MapleAlliance alliance = alliances.get(id);
            if (alliance != null) {
                for (Integer gid : alliance.getGuilds()) {
                    guilds.get(gid).setAllianceId(0);
                }
                alliances.remove(id);
            }
        }
    }

    public void allianceMessage(int id, final byte[] packet, int exception, int guildex) {
        MapleAlliance alliance = alliances.get(id);
        if (alliance != null) {
            for (Integer gid : alliance.getGuilds()) {
                if (guildex == gid) {
                    continue;
                }
                MapleGuild guild = guilds.get(gid);
                if (guild != null) {
                    guild.broadcast(packet, exception);
                }
            }
        }
    }

    public boolean addGuildtoAlliance(int aId, int guildId) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.addGuild(guildId);
            guilds.get(guildId).setAllianceId(aId);
            return true;
        }
        return false;
    }

    public boolean removeGuildFromAlliance(int aId, int guildId) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.removeGuild(guildId);
            guilds.get(guildId).setAllianceId(0);
            return true;
        }
        return false;
    }

    public boolean setAllianceRanks(int aId, String[] ranks) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.setRankTitle(ranks);
            return true;
        }
        return false;
    }

    public boolean setAllianceNotice(int aId, String notice) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.setNotice(notice);
            return true;
        }
        return false;
    }

    public boolean increaseAllianceCapacity(int aId, int inc) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.increaseCapacity(inc);
            return true;
        }
        return false;
    }

    public int createGuild(int leaderId, String name) {
        return MapleGuild.createGuild(leaderId, name);
    }

    public MapleGuild getGuildByName(String name) {
        synchronized (guilds) {
            for (MapleGuild mg : guilds.values()) {
                if (mg.getName().equalsIgnoreCase(name)) {
                    return mg;
                }
            }

            return null;
        }
    }

    public MapleGuild getGuild(int id) {
        synchronized (guilds) {
            if (guilds.get(id) != null) {
                return guilds.get(id);
            }

            return null;
        }
    }

    public MapleGuild getGuild(int id, int world) {
        return getGuild(id, world, null);
    }

    public MapleGuild getGuild(int id, int world, MapleCharacter mc) {
        synchronized (guilds) {
            if (guilds.get(id) != null) {
                return guilds.get(id);
            }
            MapleGuild g = new MapleGuild(id, world);
            if (g.getId() == -1) {
                return null;
            }

            if (mc != null) {
                mc.setMGC(g.getMGC(mc.getId()));
                if (g.getMGC(mc.getId()) == null)
                    System.out.println("null for " + mc.getName() + " when loading guild " + id);
                g.getMGC(mc.getId()).setCharacter(mc);
                g.setOnline(mc.getId(), true, mc.getClient().getChannel());
            }

            guilds.put(id, g);
            return g;
        }
    }

    public void setGuildMemberOnline(MapleCharacter mc, boolean bOnline, int channel) {
        MapleGuild g = getGuild(mc.getGuildId(), mc.getWorld(), mc);
        g.setOnline(mc.getId(), bOnline, channel);
    }

    public int addGuildMember(MapleGuildCharacter mgc, MapleCharacter chr) {
        MapleGuild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            return g.addGuildMember(mgc, chr);
        }
        return 0;
    }

    public boolean setGuildAllianceId(int gId, int aId) {
        MapleGuild guild = guilds.get(gId);
        if (guild != null) {
            guild.setAllianceId(aId);
            return true;
        }
        return false;
    }

    public void resetAllianceGuildPlayersRank(int gId) {
        guilds.get(gId).resetAllianceGuildPlayersRank();
    }

    public void leaveGuild(MapleGuildCharacter mgc) {
        MapleGuild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            g.leaveGuild(mgc);
        }
    }

    public void guildChat(int gid, String name, int cid, String msg) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.guildChat(name, cid, msg);
        }
    }

    public void changeRank(int gid, int cid, int newRank) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.changeRank(cid, newRank);
        }
    }

    public void expelMember(MapleGuildCharacter initiator, String name, int cid) {
        MapleGuild g = guilds.get(initiator.getGuildId());
        if (g != null) {
            g.expelMember(initiator, name, cid);
        }
    }

    public void setGuildNotice(int gid, String notice) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.setGuildNotice(notice);
        }
    }

    public void memberLevelJobUpdate(MapleGuildCharacter mgc) {
        MapleGuild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            g.memberLevelJobUpdate(mgc);
        }
    }

    public void changeRankTitle(int gid, String[] ranks) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.changeRankTitle(ranks);
        }
    }

    public void setGuildEmblem(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.setGuildEmblem(bg, bgcolor, logo, logocolor);
        }
    }

    public void disbandGuild(int gid) {
        synchronized (guilds) {
            MapleGuild g = guilds.get(gid);
            g.disbandGuild();
            guilds.remove(gid);
        }
    }

    public boolean increaseGuildCapacity(int gid) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            return g.increaseCapacity();
        }
        return false;
    }

    public void gainGP(int gid, int amount) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.gainGP(amount);
        }
    }

    public void guildMessage(int gid, byte[] packet) {
        guildMessage(gid, packet, -1);
    }

    public void guildMessage(int gid, byte[] packet, int exception) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.broadcast(packet, exception);
        }
    }

    public PlayerBuffStorage getPlayerBuffStorage() {
        return buffStorage;
    }

    public void deleteGuildCharacter(MapleCharacter mc) {
        setGuildMemberOnline(mc, false, (byte) -1);
        if (mc.getMGC().getGuildRank() > 1) {
            leaveGuild(mc.getMGC());
        } else {
            disbandGuild(mc.getMGC().getGuildId());
        }
    }

    public void deleteGuildCharacter(MapleGuildCharacter mgc) {
        if (mgc.getCharacter() != null) setGuildMemberOnline(mgc.getCharacter(), false, (byte) -1);
        if (mgc.getGuildRank() > 1) {
            leaveGuild(mgc);
        } else {
            disbandGuild(mgc.getGuildId());
        }
    }

    public void reloadGuildCharacters(int world) {
        World worlda = getWorld(world);
        for (MapleCharacter mc : worlda.getPlayerStorage().getAllCharacters()) {
            if (mc.getGuildId() > 0) {
                setGuildMemberOnline(mc, true, worlda.getId());
                memberLevelJobUpdate(mc.getMGC());
            }
        }
        worlda.reloadGuildSummary();
    }

    public void broadcastMessage(int world, final byte[] packet) {
        for (Channel ch : getChannelsFromWorld(world)) {
            ch.broadcastPacket(packet);
        }
    }

    public void broadcastGMMessage(int world, final byte[] packet) {
        for (Channel ch : getChannelsFromWorld(world)) {
            ch.broadcastGMPacket(packet);
        }
    }

    public boolean isGmOnline(int world) {
        for (Channel ch : getChannelsFromWorld(world)) {
            for (MapleCharacter player : ch.getPlayerStorage().getAllCharacters()) {
                if (player.isGM()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void changeFly(Integer accountid, boolean canFly) {
        if (canFly) {
            activeFly.add(accountid);
        } else {
            activeFly.remove(accountid);
        }
    }

    public boolean canFly(Integer accountid) {
        return activeFly.contains(accountid);
    }

    public int getCharacterWorld(Integer chrid) {
        lgnRLock.lock();
        try {
            Integer worldid = worldChars.get(chrid);
            return worldid != null ? worldid : -1;
        } finally {
            lgnRLock.unlock();
        }
    }

    public boolean haveCharacterEntry(Integer accountid, Integer chrid) {
        lgnRLock.lock();
        try {
            Set<Integer> accChars = accountChars.get(accountid);
            return accChars.contains(chrid);
        } finally {
            lgnRLock.unlock();
        }
    }

    public short getAccountCharacterCount(Integer accountid) {
        lgnRLock.lock();
        try {
            return accountCharacterCount.get(accountid);
        } finally {
            lgnRLock.unlock();
        }
    }

    public short getAccountWorldCharacterCount(Integer accountid, Integer worldid) {
        lgnRLock.lock();
        try {
            short count = 0;

            for (Integer chr : accountChars.get(accountid)) {
                if (worldChars.get(chr).equals(worldid)) {
                    count++;
                }
            }

            return count;
        } finally {
            lgnRLock.unlock();
        }
    }

    private Set<Integer> getAccountCharacterEntries(Integer accountid) {
        lgnRLock.lock();
        try {
            return new HashSet<>(accountChars.get(accountid));
        } finally {
            lgnRLock.unlock();
        }
    }

    public void updateCharacterEntry(MapleCharacter chr) {
        MapleCharacter chrView = chr.generateCharacterEntry();

        lgnWLock.lock();
        try {
            World wserv = this.getWorld(chrView.getWorld());
            if (wserv != null) wserv.registerAccountCharacterView(chrView.getAccountID(), chrView);
        } finally {
            lgnWLock.unlock();
        }
    }

    public void createCharacterEntry(MapleCharacter chr) {
        Integer accountid = chr.getAccountID(), chrid = chr.getId(), world = chr.getWorld();

        lgnWLock.lock();
        try {
            accountCharacterCount.put(accountid, (short) (accountCharacterCount.get(accountid) + 1));

            Set<Integer> accChars = accountChars.get(accountid);
            accChars.add(chrid);

            worldChars.put(chrid, world);

            MapleCharacter chrView = chr.generateCharacterEntry();

            World wserv = this.getWorld(chrView.getWorld());
            if (wserv != null) wserv.registerAccountCharacterView(chrView.getAccountID(), chrView);
        } finally {
            lgnWLock.unlock();
        }
    }

    public void deleteCharacterEntry(Integer accountid, Integer chrid) {
        lgnWLock.lock();
        try {
            accountCharacterCount.put(accountid, (short) (accountCharacterCount.get(accountid) - 1));

            Set<Integer> accChars = accountChars.get(accountid);
            accChars.remove(chrid);

            Integer world = worldChars.remove(chrid);
            if (world != null) {
                World wserv = this.getWorld(world);
                if (wserv != null) wserv.unregisterAccountCharacterView(accountid, chrid);
            }
        } finally {
            lgnWLock.unlock();
        }
    }

    public void transferWorldCharacterEntry(MapleCharacter chr, Integer toWorld) { // used before setting the new worldid on the character object
        lgnWLock.lock();
        try {
            Integer chrid = chr.getId(), accountid = chr.getAccountID(), world = worldChars.get(chr.getId());
            if (world != null) {
                World wserv = this.getWorld(world);
                if (wserv != null) wserv.unregisterAccountCharacterView(accountid, chrid);
            }

            worldChars.put(chrid, toWorld);

            MapleCharacter chrView = chr.generateCharacterEntry();

            World wserv = this.getWorld(toWorld);
            if (wserv != null) wserv.registerAccountCharacterView(chrView.getAccountID(), chrView);
        } finally {
            lgnWLock.unlock();
        }
    }
    
    /*
    public void deleteAccountEntry(Integer accountid) { is this even a thing?
        lgnWLock.lock();
        try {
            accountCharacterCount.remove(accountid);
            accountChars.remove(accountid);
        } finally {
            lgnWLock.unlock();
        }
    }
    */

    public Pair<Pair<Integer, List<MapleCharacter>>, List<Pair<Integer, List<MapleCharacter>>>> loadAccountCharlist(Integer accountId, int visibleWorlds) {
        List<World> wlist = this.getWorlds();
        if (wlist.size() > visibleWorlds) wlist = wlist.subList(0, visibleWorlds);

        List<Pair<Integer, List<MapleCharacter>>> accChars = new ArrayList<>(wlist.size() + 1);
        int chrTotal = 0;
        List<MapleCharacter> lastwchars = null;

        lgnRLock.lock();
        try {
            for (World w : wlist) {
                List<MapleCharacter> wchars = w.getAccountCharactersView(accountId);
                if (wchars == null) {
                    if (!accountChars.containsKey(accountId)) {
                        accountCharacterCount.put(accountId, (short) 0);
                        accountChars.put(accountId, new HashSet<Integer>());    // not advisable at all to write on the map on a read-protected environment
                    }                                                           // yet it's known there's no problem since no other point in the source does
                } else if (!wchars.isEmpty()) {                                  // this action.
                    lastwchars = wchars;

                    accChars.add(new Pair<>(w.getId(), wchars));
                    chrTotal += wchars.size();
                }
            }
        } finally {
            lgnRLock.unlock();
        }

        return new Pair<>(new Pair<>(chrTotal, lastwchars), accChars);
    }

    private static Pair<Short, List<List<MapleCharacter>>> loadAccountCharactersViewFromDb(int accId, int wlen) {
        short characterCount = 0;
        List<List<MapleCharacter>> wchars = new ArrayList<>(wlen);
        for (int i = 0; i < wlen; i++) wchars.add(i, new LinkedList<>());

        List<MapleCharacter> chars = new LinkedList<>();
        int curWorld = 0;
        try {
            List<Pair<Item, Integer>> accEquips = ItemFactory.loadEquippedItems(accId, true, true);
            Map<Integer, List<Item>> accPlayerEquips = new HashMap<>();

            for (Pair<Item, Integer> ae : accEquips) {
                List<Item> playerEquips = accPlayerEquips.get(ae.getRight());
                if (playerEquips == null) {
                    playerEquips = new LinkedList<>();
                    accPlayerEquips.put(ae.getRight(), playerEquips);
                }

                playerEquips.add(ae.getLeft());
            }

            try (Connection con = DatabaseConnection.getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE accountid = ? ORDER BY world, id")) {
                    ps.setInt(1, accId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            characterCount++;

                            int cworld = rs.getByte("world");
                            if (cworld >= wlen) continue;

                            if (cworld > curWorld) {
                                wchars.add(curWorld, chars);

                                curWorld = cworld;
                                chars = new LinkedList<>();
                            }

                            Integer cid = rs.getInt("id");
                            chars.add(MapleCharacter.loadCharacterEntryFromDB(rs, accPlayerEquips.get(cid)));
                        }
                    }
                }
            }

            wchars.add(curWorld, chars);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return new Pair<>(characterCount, wchars);
    }

    public void loadAllAccountsCharactersView() {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM accounts")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int accountId = rs.getInt("id");
                        if (isFirstAccountLogin(accountId)) {
                            loadAccountCharactersView(accountId, 0, 0);
                        }
                    }
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private boolean isFirstAccountLogin(Integer accId) {
        lgnRLock.lock();
        try {
            return !accountChars.containsKey(accId);
        } finally {
            lgnRLock.unlock();
        }
    }

    public void loadAccountCharacters(MapleClient c) {
        Integer accId = c.getAccID();
        if (!isFirstAccountLogin(accId)) {
            Set<Integer> accWorlds = new HashSet<>();

            lgnRLock.lock();
            try {
                for (Integer chrid : getAccountCharacterEntries(accId)) {
                    accWorlds.add(worldChars.get(chrid));
                }
            } finally {
                lgnRLock.unlock();
            }

            int gmLevel = 0;
            for (Integer aw : accWorlds) {
                World wserv = this.getWorld(aw);

                if (wserv != null) {
                    for (MapleCharacter chr : wserv.getAllCharactersView()) {
                        if (gmLevel < chr.gmLevel()) gmLevel = chr.gmLevel();
                    }
                }
            }

            c.setGMLevel(gmLevel);
            return;
        }

        int gmLevel = loadAccountCharactersView(c.getAccID(), 0, 0);
        c.setGMLevel(gmLevel);
    }

    private int loadAccountCharactersView(Integer accId, int gmLevel, int fromWorldid) {    // returns the maximum gmLevel found
        List<World> wlist = this.getWorlds();
        Pair<Short, List<List<MapleCharacter>>> accCharacters = loadAccountCharactersViewFromDb(accId, wlist.size());

        lgnWLock.lock();
        try {
            List<List<MapleCharacter>> accChars = accCharacters.getRight();
            accountCharacterCount.put(accId, accCharacters.getLeft());

            Set<Integer> chars = accountChars.get(accId);
            if (chars == null) {
                chars = new HashSet<>(5);
            }

            for (int wid = fromWorldid; wid < wlist.size(); wid++) {
                World w = wlist.get(wid);
                List<MapleCharacter> wchars = accChars.get(wid);
                w.loadAccountCharactersView(accId, wchars);

                for (MapleCharacter chr : wchars) {
                    int cid = chr.getId();
                    if (gmLevel < chr.gmLevel()) gmLevel = chr.gmLevel();

                    chars.add(cid);
                    worldChars.put(cid, wid);
                }
            }

            accountChars.put(accId, chars);
        } finally {
            lgnWLock.unlock();
        }

        return gmLevel;
    }

    private static String getRemoteIp(IoSession session) {
        return MapleSessionCoordinator.getSessionRemoteAddress(session);
    }

    public void setCharacteridInTransition(IoSession session, int charId) {
        String remoteIp = getRemoteIp(session);

        lgnWLock.lock();
        try {
            transitioningChars.put(remoteIp, charId);
        } finally {
            lgnWLock.unlock();
        }
    }

    public boolean validateCharacteridInTransition(IoSession session, int charId) {
        if (!ServerConstants.USE_IP_VALIDATION) {
            return true;
        }

        String remoteIp = getRemoteIp(session);

        lgnWLock.lock();
        try {
            Integer cid = transitioningChars.remove(remoteIp);
            return cid != null && cid.equals(charId);
        } finally {
            lgnWLock.unlock();
        }
    }

    public Integer freeCharacteridInTransition(IoSession session) {
        if (!ServerConstants.USE_IP_VALIDATION) {
            return null;
        }

        String remoteIp = getRemoteIp(session);

        lgnWLock.lock();
        try {
            return transitioningChars.remove(remoteIp);
        } finally {
            lgnWLock.unlock();
        }
    }

    public boolean hasCharacteridInTransition(IoSession session) {
        if (!ServerConstants.USE_IP_VALIDATION) {
            return true;
        }

        String remoteIp = getRemoteIp(session);

        lgnRLock.lock();
        try {
            return transitioningChars.containsKey(remoteIp);
        } finally {
            lgnRLock.unlock();
        }
    }

    public void registerLoginState(MapleClient c) {
        srvLock.lock();
        try {
            inLoginState.put(c, System.currentTimeMillis() + 600000);
        } finally {
            srvLock.unlock();
        }
    }

    public void unregisterLoginState(MapleClient c) {
        srvLock.lock();
        try {
            inLoginState.remove(c);
        } finally {
            srvLock.unlock();
        }
    }

    private void disconnectIdlesOnLoginState() {
        List<MapleClient> toDisconnect = new LinkedList<>();

        srvLock.lock();
        try {
            long timeNow = System.currentTimeMillis();

            for (Entry<MapleClient, Long> mc : inLoginState.entrySet()) {
                if (timeNow > mc.getValue()) {
                    toDisconnect.add(mc.getKey());
                }
            }

            for (MapleClient c : toDisconnect) {
                inLoginState.remove(c);
            }
        } finally {
            srvLock.unlock();
        }

        for (MapleClient c : toDisconnect) {    // thanks Lei for pointing a deadlock issue with srvLock
            if (c.isLoggedIn()) {
                c.disconnect(false, false);
            } else {
                MapleSessionCoordinator.getInstance().closeSession(c.getSession(), true);
            }
        }
    }

    private void disconnectIdlesOnLoginTask() {
        TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                disconnectIdlesOnLoginState();
            }
        }, 300000);
    }

    public final Runnable shutdown(final boolean restart) {//no player should be online when trying to shutdown!
        return new Runnable() {
            @Override
            public void run() {
                shutdownInternal(restart);
            }
        };
    }

    private synchronized void shutdownInternal(boolean restart) {
        System.out.println((restart ? "Restarting" : "Shutting down") + " the server!\r\n");
        if (getWorlds() == null) return;//already shutdown
        for (World w : getWorlds()) {
            w.shutdown();
        }

        List<Channel> allChannels = getAllChannels();

        if (ServerConstants.USE_THREAD_TRACKER) ThreadTracker.getInstance().cancelThreadTrackerTask();

        for (Channel ch : allChannels) {
            while (!ch.finishedShutdown()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                    System.err.println("FUCK MY LIFE");
                }
            }
        }

        resetServerWorlds();

        ThreadManager.getInstance().stop();
        TimerManager.getInstance().purge();
        TimerManager.getInstance().stop();

        System.out.println("Worlds + Channels are offline.");
        acceptor.unbind();
        acceptor = null;
        if (!restart) {  // shutdown hook deadlocks if System.exit() method is used within its body chores, thanks MIKE for pointing that out
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }).start();
        } else {
            System.out.println("\r\nRestarting the server....\r\n");
            try {
                instance.finalize();//FUU I CAN AND IT'S FREE
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
            instance = null;
            System.gc();
            getInstance().init();//DID I DO EVERYTHING?! D:
        }
    }
}
