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
package client;

import java.io.*;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

import com.google.gson.JsonObject;
import enums.BroadcastMessageType;
import enums.PartyResultType;
import database.DatabaseConnection;
import net.database.Statements;
import network.packet.CCashShop;
import network.packet.CLogin;
import network.packet.UserLocal;
import network.packet.context.BroadcastMsgPacket;
import network.packet.context.WvsContext;
import network.packet.context.GuildPacket;
import tools.*;

import javax.script.ScriptEngine;

import net.server.Server;
import net.server.coordinator.MapleSessionCoordinator;
import net.server.coordinator.MapleSessionCoordinator.AntiMulticlientResult;
import net.server.channel.Channel;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.world.MapleMessengerCharacter;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.World;

import org.apache.mina.core.session.IoSession;

import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ServerConstants;
import server.life.MapleMonster;
import server.ThreadManager;
import server.maps.*;
import server.quest.MapleQuest;

import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;

public class MapleClient {

    public static final int LOGIN_NOTLOGGEDIN = 0;
    public static final int LOGIN_SERVER_TRANSITION = 1;
    public static final int LOGIN_LOGGEDIN = 2;
    public static final String CLIENT_KEY = "CLIENT";
    public static final String CLIENT_HWID = "HWID";
    public static final String CLIENT_NIBBLEHWID = "HWID2";
    public static final String CLIENT_REMOTE_ADDRESS = "REMOTE_IP";
    public static final String CLIENT_TRANSITION = "TRANSITION";
    private MapleAESOFB send;
    private MapleAESOFB receive;
    private final IoSession session;
    private MapleCharacter player;
    private int channel = 1;
    private int accId = -4;
    private boolean loggedIn = false;
    private boolean serverTransition = false;
    private Calendar birthday = null;
    private String accountName = null;
    private int world;
    private long lastPong;
    private int gmlevel;
    private Set<String> macs = new HashSet<>();
    private Map<String, ScriptEngine> engines = new HashMap<>();
    private byte characterSlots = 3;
    private byte loginattempt = 0;
    private String pin = "";
    private int pinattempt = 0;
    private String pic = "";
    private String hwid = null;
    private int picattempt = 0;
    private byte csattempt = 0;
    private byte gender = -1;
    private boolean disconnecting = false;
    private final Semaphore actionsSemaphore = new Semaphore(7);
    private final Lock lock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CLIENT, true);
    private final Lock encoderLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CLIENT_ENCODER, true);
    private static final Lock loginLocks = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CLIENT_LOGIN, true);//new Lock[200];  // thanks Masterrulax & try2hack for pointing out a bottleneck issue here
    private int visibleWorlds;
    private long lastNpcClick;
    private long sessionId;
    private int lang = 0;
    private byte clearance = 0;
    private int trophy = 0;
    private byte cygnusBuff = 0;

    static {
        /*for (int i = 0; i < 200; i++) {
			//loginLocks[i] = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CLIENT_LOGIN, true);
		}*/
    }

    public MapleClient(MapleAESOFB send, MapleAESOFB receive, IoSession session) {
        this.send = send;
        this.receive = receive;
        this.session = session;
    }

    public MapleAESOFB getReceiveCrypto() {
        return receive;
    }

    public MapleAESOFB getSendCrypto() {
        return send;
    }

    public IoSession getSession() {
        return session;
    }

    /*public EventManager getEventManager(String event) {
        return getChannelServer().getEventSM().getEventManager(event);
    }*/

    public MapleCharacter getPlayer() {
        return player;
    }

    public void setPlayer(MapleCharacter player) {
        this.player = player;
    }

    //public AbstractPlayerInteraction getAbstractPlayerInteraction() {
    //    return new AbstractPlayerInteraction(this);
    //}

    public void sendCharList(int server) {
        this.announce(CLogin.Packet.getCharList(this, server));
    }

    public List<MapleCharacter> loadCharacters(int serverId) {
        List<MapleCharacter> chars = new ArrayList<>(15);
        try {
            for (CharNameAndId cni : loadCharactersInternal(serverId)) {
                chars.add(MapleCharacter.loadCharFromDB(cni.id, this, false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chars;
    }

    public List<String> loadCharacterNames(int worldId) {
        List<String> chars = new ArrayList<>(15);
        for (CharNameAndId cni : loadCharactersInternal(worldId)) {
            chars.add(cni.name);
        }
        return chars;
    }

    private List<CharNameAndId> loadCharactersInternal(int worldId) {
        List<CharNameAndId> chars = new ArrayList<>(15);
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, name FROM characters WHERE accountid = ? AND world = ?")) {
            ps.setInt(1, this.getAccID());
            ps.setInt(2, worldId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chars.add(new CharNameAndId(rs.getString("name"), rs.getInt("id")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chars;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean hasBannedIP() {
        boolean ret = false;
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM ip_bans WHERE ? LIKE CONCAT(ip, '%')")) {
                ps.setString(1, session.getRemoteAddress().toString());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        ret = true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public boolean hasBannedHWID() {
        if (hwid == null) {
            return false;
        }

        boolean ret = false;
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM hwid_bans WHERE hwid LIKE ?")) {
                ps.setString(1, hwid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs != null && rs.next()) {
                        if (rs.getInt(1) > 0) {
                            ret = true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public boolean hasBannedMac() {
        if (macs.isEmpty()) {
            return false;
        }
        boolean ret = false;
        int i;
        try {
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM mac_bans WHERE mac IN (");
            for (i = 0; i < macs.size(); i++) {
                sql.append("?");
                if (i != macs.size() - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")");

            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql.toString())) {
                i = 0;
                for (String mac : macs) {
                    i++;
                    ps.setString(i, mac);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        ret = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private void loadHWIDIfNescessary() throws SQLException {
        if (hwid == null) {
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT hwid FROM accounts WHERE id = ?")) {
                ps.setInt(1, accId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        hwid = rs.getString("hwid");
                    }
                }
            }
        }
    }

    // TODO: Recode to close statements...
    private void loadMacsIfNescessary() throws SQLException {
        if (macs.isEmpty()) {
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT macs FROM accounts WHERE id = ?")) {
                ps.setInt(1, accId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        for (String mac : rs.getString("macs").split(", ")) {
                            if (!mac.equals("")) {
                                macs.add(mac);
                            }
                        }
                    }
                }
            }
        }
    }

    public void banHWID() {
        try (Connection con = DatabaseConnection.getConnection()) {
            loadHWIDIfNescessary();
            Statements.Insert.into("hwid_bans").add("hwid", hwid).execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void banMacs() {
        try (Connection con = DatabaseConnection.getConnection()) {
            loadMacsIfNescessary();

            List<String> filtered = new LinkedList<>();
            try (PreparedStatement ps = con.prepareStatement("SELECT filter FROM mac_filters"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filtered.add(rs.getString("filter"));
                }
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO mac_bans (mac, aid) VALUES (?, ?)")) {
                for (String mac : macs) {
                    boolean matched = false;
                    for (String filter : filtered) {
                        if (mac.matches(filter)) {
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        ps.setString(1, mac);
                        ps.setString(2, String.valueOf(getAccID()));
                        ps.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int finishLogin() {
        //Lock loginLock = loginLocks;[this.getAccID() % 200];
        loginLocks.lock();
        try {
            if (getLoginState() > LOGIN_NOTLOGGEDIN) { // 0 = LOGIN_NOTLOGGEDIN, 1= LOGIN_SERVER_TRANSITION, 2 = LOGIN_LOGGEDIN
                loggedIn = false;
                return 7;
            }
            updateLoginState(LOGIN_LOGGEDIN);
        } finally {
            loginLocks.unlock();
        }

        return 0;
    }

    public void setPin(String pin) {
        this.pin = pin;
        try {
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("UPDATE accounts SET pin = ? WHERE id = ?")) {
                ps.setString(1, pin);
                ps.setInt(2, accId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPin() {
        return pin;
    }

    public boolean checkPin(String other) {
        pinattempt++;
        if (pinattempt > 5) {
            MapleSessionCoordinator.getInstance().closeSession(session, false);
        }
        if (pin.equals(other)) {
            pinattempt = 0;
            return true;
        }
        return false;
    }

    public void setPic(String pic) {
        this.pic = pic;
        try {
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("UPDATE accounts SET pic = ? WHERE id = ?")) {
                ps.setString(1, pic);
                ps.setInt(2, accId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPic() {
        return pic;
    }

    public boolean checkPic(String other) {
        picattempt++;
        if (picattempt > 5) {
            MapleSessionCoordinator.getInstance().closeSession(session, false);
        }
        if (pic.equals(other)) {
            picattempt = 0;
            return true;
        }
        return false;
    }

    public int login(String login, String pwd, String nibbleHwid) {
        int loginok = 5;

        loginattempt++;
        if (loginattempt > 4) {
            loggedIn = false;
            MapleSessionCoordinator.getInstance().closeSession(session, false);
            return 6;   // thanks Survival_Project for finding out an issue with AUTOMATIC_REGISTER here
        }

        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT id, password, gender, banned, gm, pin, pic, characterslots, tos, clearance, trophy FROM accounts WHERE name = ?")) {
                ps.setString(1, login.toLowerCase());
                try (ResultSet rs = ps.executeQuery()) {
                    accId = -2;
                    if (rs.next()) {
                        accId = rs.getInt("id");
                        if (accId <= 0) {
                            FilePrinter.printError(FilePrinter.LOGIN_EXCEPTION, "Tried to login with accid " + accId);
                            return 15;
                        }

                        boolean banned = (rs.getBoolean("banned"));
                        gmlevel = rs.getInt("gm");
                        pin = rs.getString("pin");
                        pic = rs.getString("pic");
                        gender = rs.getByte("gender");
                        characterSlots = rs.getByte("characterslots");
                        String passhash = rs.getString("password");
                        boolean tos = rs.getBoolean("tos");
                        clearance = rs.getByte("clearance");
                        trophy = rs.getInt("trophy");

                        if (banned) return 3;
                        if (ServerConstants.GM_SERVER && gmlevel == 0) return 15;

                        if (getLoginState() > LOGIN_NOTLOGGEDIN) { // already loggedin
                            loggedIn = false;
                            loginok = 7;
                        } else if (passhash.charAt(0) == '$' && passhash.charAt(1) == '2' && BCrypt.checkpw(pwd, passhash)) {
                            loginok = (tos) ? 23 : 0;
                        } else if (pwd.equals(passhash) || checkHash(passhash, "SHA-1", pwd) || checkHash(passhash, "SHA-512", pwd)) {
                            // thanks GabrielSin for detecting some no-bcrypt inconsistencies here
                            loginok = (tos) ? (!ServerConstants.BCRYPT_MIGRATION ? 23 : -23) : (!ServerConstants.BCRYPT_MIGRATION ? 0 : -10); // migrate to bcrypt
                        } else {
                            loggedIn = false;
                            loginok = 4;
                        }
                    } else {
                        accId = -3;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (loginok == 0 || loginok == 4) {
            AntiMulticlientResult res = MapleSessionCoordinator.getInstance().attemptLoginSession(session, nibbleHwid, accId, loginok == 4);

            switch (res) {
                case SUCCESS:
                    if (loginok == 0) {
                        loginattempt = 0;
                    }

                    return loginok;

                case REMOTE_LOGGEDIN:
                    return 17;

                case REMOTE_REACHED_LIMIT:
                    return 13;

                case REMOTE_PROCESSING:
                    return 10;

                case MANY_ACCOUNT_ATTEMPTS:
                    return 16;

                default:
                    return 8;
            }
        } else {
            return loginok;
        }
    }

    public Calendar getTempBanCalendar() {
        final Calendar lTempban = Calendar.getInstance();
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT tempban FROM accounts WHERE id = ?")) {
                ps.setInt(1, getAccID());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    long blubb = rs.getTimestamp("tempban").getTime();
                    if (blubb == 0) { // basically if timestamp in db is 0000-00-00
                        return null;
                    }
                    lTempban.setTimeInMillis(rs.getTimestamp("tempban").getTime());
                }
            }
            return lTempban;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;//why oh why!?!
    }

    public static long dottedQuadToLong(String dottedQuad) throws RuntimeException {
        String[] quads = dottedQuad.split("\\.");
        if (quads.length != 4) {
            throw new RuntimeException("Invalid IP Address format.");
        }
        long ipAddress = 0;
        for (int i = 0; i < 4; i++) {
            int quad = Integer.parseInt(quads[i]);
            ipAddress += (long) (quad % 256) * (long) Math.pow(256, (double) (4 - i));
        }
        return ipAddress;
    }

    public void updateHWID(String newHwid) {
        String[] split = newHwid.split("_");
        if (split.length > 1 && split[1].length() == 8) {
            StringBuilder hwid = new StringBuilder();
            String convert = split[1];

            int len = convert.length();
            for (int i = len - 2; i >= 0; i -= 2) {
                hwid.append(convert, i, i + 2);
            }
            hwid.insert(4, "-");

            this.hwid = hwid.toString();

            try (Connection con = DatabaseConnection.getConnection()) {
                Statements.Update("accounts").set("hwid", this.hwid).where("id", accId).execute(con);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            this.disconnect(false, false); // Invalid HWID...
        }
    }

    public void updateMacs(String macData) {
        macs.addAll(Arrays.asList(macData.split(", ")));
        StringBuilder newMacData = new StringBuilder();
        Iterator<String> iter = macs.iterator();
        while (iter.hasNext()) {
            String cur = iter.next();
            newMacData.append(cur);
            if (iter.hasNext()) {
                newMacData.append(", ");
            }
        }
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Update("accounts").set("macs", newMacData.toString()).where("id", accId).execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setAccID(int id) {
        this.accId = id;
    }

    public int getAccID() {
        return accId;
    }

    public void updateLoginState(int newstate) {
        // rules out possibility of multiple account entries
        if (newstate == LOGIN_LOGGEDIN) {
            MapleSessionCoordinator.getInstance().updateOnlineSession(this.getSession());
        }


        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, lastlogin = now() WHERE id = ?")) {
                // using sql currenttime here could potentially break the login, thanks Arnah for pointing this out

                ps.setInt(1, newstate);
                //ps.setTimestamp(2, new java.sql.Timestamp(Server.getInstance().getCurrentTime()));
                ps.setInt(2, getAccID());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (getAccID() >= 0 && ServerConstants.HTTP_SERVER) {
            Server.httpWorker.add("http://localhost:17003/api/connections_changed");
        }

        if (newstate == LOGIN_NOTLOGGEDIN) {
            loggedIn = false;
            serverTransition = false;
            setAccID(0);
        } else {
            serverTransition = (newstate == LOGIN_SERVER_TRANSITION);
            loggedIn = !serverTransition;
        }
    }

    public int getLoginState() {  // 0 = LOGIN_NOTLOGGEDIN, 1= LOGIN_SERVER_TRANSITION, 2 = LOGIN_LOGGEDIN
        try (Connection con = DatabaseConnection.getConnection()) {
            int state;
            try (PreparedStatement ps = con.prepareStatement("SELECT loggedin, lastlogin, birthday FROM accounts WHERE id = ?")) {
                ps.setInt(1, getAccID());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("getLoginState - MapleClient AccID: " + getAccID());
                    }

                    birthday = Calendar.getInstance();
                    try {
                        birthday.setTime(rs.getDate("birthday"));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }


                    state = rs.getInt("loggedin");
                    if (state == LOGIN_SERVER_TRANSITION) {
                        if (rs.getTimestamp("lastlogin").getTime() + 30000 < Server.getInstance().getCurrentTime()) {
                            int accountId = accId;
                            state = LOGIN_NOTLOGGEDIN;
                            updateLoginState(LOGIN_NOTLOGGEDIN);   // ACCID = 0, issue found thanks to Tochi & K u ssss o & Thora & Omo Oppa
                            this.setAccID(accountId);
                        }
                    }
                }
            }
            if (state == LOGIN_LOGGEDIN) {
                loggedIn = true;
            } else if (state == LOGIN_SERVER_TRANSITION) {
                Statements.Update("accounts").set("loggedin", 0).where("id", getAccID()).execute(con);
            } else {
                loggedIn = false;
            }

            return state;
        } catch (SQLException e) {
            loggedIn = false;
            e.printStackTrace();
            throw new RuntimeException("login state");
        }
    }

    public boolean checkBirthDate(Calendar date) {
        return date.get(Calendar.YEAR) == birthday.get(Calendar.YEAR)
                && date.get(Calendar.MONTH) == birthday.get(Calendar.MONTH)
                && date.get(Calendar.DAY_OF_MONTH) == birthday.get(Calendar.DAY_OF_MONTH);
    }

    private void removePartyPlayer(World wserv) {
        MapleMap map = player.getMap();
        final MapleParty party = player.getParty();
        final int idz = player.getId();
        final MaplePartyCharacter chrp = new MaplePartyCharacter(player);

        if (party != null) {
            chrp.setOnline(false);
            wserv.updateParty(party.getId(), PartyResultType.SilentUpdate.getResult(), chrp);
            if (party.getLeader().getId() == idz && map != null) {
                MaplePartyCharacter lchr = null;
                for (MaplePartyCharacter pchr : party.getMembers()) {
                    if (pchr != null && pchr.getId() != idz && (lchr == null || lchr.getLevel() <= pchr.getLevel()) && map.getCharacterById(pchr.getId()) != null) {
                        lchr = pchr;
                    }
                }
                if (lchr != null) {
                    wserv.updateParty(party.getId(), PartyResultType.ChangeLeader.getResult(), lchr);
                }
            }
        }
    }

    private void removePlayer(World wserv, boolean serverTransition) {
        try {
            player.setDisconnectedFromChannelWorld();
            player.notifyMapTransferToPartner(-1);
            player.removeIncomingInvites();
            player.cancelAllBuffs(true);

            player.closePlayerInteractions();
            player.closePartySearchInteractions();

            if (player.getDoor() != null) {
                player.cancelMagicDoor();
            }

            if (!serverTransition) {    // thanks MedicOP for detecting an issue with party leader change on changing channels
                removePartyPlayer(wserv);

                //EventInstanceManager eim = player.getEventInstance();
                //if (eim != null) {
                //    eim.playerDisconnected(player);
                //}

                if (player.getMonsterCarnival() != null) {
                    player.getMonsterCarnival().playerDisconnected(getPlayer().getId());
                }

                if (player.getAriantColiseum() != null) {
                    player.getAriantColiseum().playerDisconnected(getPlayer());
                }
            }

            if (player.getMap() != null) {
                int mapId = player.getMapId();
                player.getMap().removePlayer(player);
                if (GameConstants.isDojo(mapId)) {
                    this.getChannelServer().freeDojoSectionIfEmpty(mapId);
                }
            }

        } catch (final Throwable t) {
            FilePrinter.printError(FilePrinter.ACCOUNT_STUCK, t);
        }
    }

    public final void disconnect(final boolean shutdown, final boolean cashshop) {
        if (canDisconnect()) {
            ThreadManager.getInstance().newTask(() -> disconnectInternal(shutdown, cashshop));
        }
    }

    public final void forceDisconnect() {
        if (canDisconnect()) {
            disconnectInternal(true, false);
        }
    }

    private synchronized boolean canDisconnect() {
        if (disconnecting) {
            return false;
        }

        disconnecting = true;
        return true;
    }

    private void disconnectInternal(boolean shutdown, boolean cashshop) {//once per MapleClient instance
        if (player != null && player.isLoggedin() && player.getClient() != null) {
            final int messengerid = player.getMessenger() == null ? 0 : player.getMessenger().getId();
            //final int fid = player.getFamilyId();
            final BuddyList bl = player.getBuddylist();
            final MapleMessengerCharacter chrm = new MapleMessengerCharacter(player, 0);
            final MapleGuildCharacter chrg = player.getMGC();
            final MapleGuild guild = player.getGuild();

            //player.cancelMagicDoor();

            final World wserv = getWorldServer();   // obviously wserv is NOT null if this player was online on it
            try {
                removePlayer(wserv, this.serverTransition);

                if (!(channel == -1 || shutdown)) {
                    if (!cashshop) {
                        if (!this.serverTransition) { // meaning not changing channels
                            if (messengerid > 0) {
                                wserv.leaveMessenger(messengerid, chrm);
                            }
                            /*      
                                                        if (fid > 0) {
                                                                final MapleFamily family = worlda.getFamily(fid);
                                                                family.
                                                        }
                             */
                            for (MapleQuestStatus status : player.getStartedQuests()) { //This is for those quests that you have to stay logged in for a certain amount of time
                                MapleQuest quest = status.getQuest();
                                if (quest.getTimeLimit() > 0) {
                                    MapleQuestStatus newStatus = new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
                                    newStatus.setForfeited(player.getQuest(quest).getForfeited() + 1);
                                    player.updateQuest(newStatus);
                                }
                            }
                            if (guild != null) {
                                final Server server = Server.getInstance();
                                server.setGuildMemberOnline(player, false, player.getClient().getChannel());
                                player.getClient().announce(GuildPacket.Packet.showGuildInfo(player));
                            }
                            if (bl != null) {
                                wserv.loggedOff(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
                            }
                        }
                    } else if (!this.serverTransition) { // if dc inside of cash shop.
                        if (bl != null) {
                            wserv.loggedOff(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
                        }
                    }
                }
            } catch (final Exception e) {
                FilePrinter.printError(FilePrinter.ACCOUNT_STUCK, e);
            } finally {
                if (!this.serverTransition) {
                    if (chrg != null) {
                        chrg.setCharacter(null);
                    }

                    JsonObject json = new JsonObject();
                    json.addProperty("down", "" + player.getAccountID());

                    wserv.removePlayer(player);

                    //getChannelServer().removePlayer(player); already being done
                    player.saveCooldowns();
                    player.cancelAllDebuffs();
                    player.saveCharToDB(true);

                    player.logOff();
                    clear();

                    if (ServerConstants.HTTP_SERVER) {
                        JsonObject players = new JsonObject();
                        for (MapleCharacter _c : wserv.getPlayerStorage().getAllCharacters()) {
                            players.addProperty("" + _c.getAccountID(), _c.getName());
                        }
                        json.add("players", players);

                        Server.httpWorker.add("http://localhost:17003/api/character_connection", json);
                    }
                } else {
                    getChannelServer().removePlayer(player);

                    player.saveCooldowns();
                    player.cancelAllDebuffs();
                    player.saveCharToDB();
                }
            }
        }
        if (!serverTransition && isLoggedIn()) {
            MapleSessionCoordinator.getInstance().closeSession(session, false);
            updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
            session.removeAttribute(MapleClient.CLIENT_KEY); // prevents double dcing during login

            clear();
        } else {
            if (session.containsAttribute(MapleClient.CLIENT_KEY)) {
                MapleSessionCoordinator.getInstance().closeSession(session, false);
                session.removeAttribute(MapleClient.CLIENT_KEY);
            }
            if (!Server.getInstance().hasCharacteridInTransition(session)) {
                updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
            }

            engines = null; // thanks Tochi for pointing out a NPE here
        }
    }

    private void clear() {
        // player hard reference removal thanks to Steve (kaito1410)
        if (this.player != null) {
            this.player.empty(true); // clears schedules and stuff
        }

        Server.getInstance().unregisterLoginState(this);

        this.accountName = null;
        this.macs = null;
        this.hwid = null;
        this.birthday = null;
        this.engines = null;
        this.player = null;
        this.receive = null;
        this.send = null;
        //this.session = null;
    }

    public int getChannel() {
        return channel;
    }

    public Channel getChannelServer() {
        return Server.getInstance().getChannel(world, channel);
    }

    public World getWorldServer() {
        return Server.getInstance().getWorld(world);
    }

    public Channel getChannelServer(byte channel) {
        return Server.getInstance().getChannel(world, channel);
    }

    public boolean deleteCharacter(int cid, int senderAccId) {
        try {
            MapleCharacter chr = MapleCharacter.loadCharFromDB(cid, this, false);

            Integer partyid = chr.getWorldServer().getCharacterPartyid(cid);
            if (partyid != null) {
                this.setPlayer(chr);

                MapleParty party = chr.getWorldServer().getParty(partyid);
                chr.setParty(party);
                chr.getMPC();
                chr.leaveParty();   // thanks Vcoc for pointing out deleted characters would still stay in a party

                this.setPlayer(null);
            }

            return MapleCharacter.deleteCharFromDB(chr, senderAccId);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String a) {
        this.accountName = a;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getWorld() {
        return world;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public void pongReceived() {
        lastPong = Server.getInstance().getCurrentTime();
    }

    public void testPing(long timeThen) {
        try {
            if (lastPong < timeThen) {
                if (session != null && session.isConnected()) {
                    MapleSessionCoordinator.getInstance().closeSession(session, false);
                    updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
                    session.removeAttribute(MapleClient.CLIENT_KEY);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public String getHWID() {
        return hwid;
    }

    public void setHWID(String hwid) {
        this.hwid = hwid;
    }

    public Set<String> getMacs() {
        return Collections.unmodifiableSet(macs);
    }

    public int getGMLevel() {
        return gmlevel;
    }

    public void setGMLevel(int level) {
        gmlevel = level;
    }

    public void setScriptEngine(String name, ScriptEngine e) {
        engines.put(name, e);
    }

    public ScriptEngine getScriptEngine(String name) {
        return engines.get(name);
    }

    public void removeScriptEngine(String name) {
        engines.remove(name);
    }

    /*public NPCConversationManager getCM() {
        return NPCScriptManager.getInstance().getCM(this);
    }*/

    /*public QuestActionManager getQM() {
        return QuestScriptManager.getInstance().getQM(this);
    }*/

    public boolean acceptToS() {
        boolean disconnect = false;
        if (accountName == null) {
            return true;
        }
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT tos FROM accounts WHERE id = ?")) {
                ps.setInt(1, accId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getBoolean("tos")) {
                            disconnect = true;
                        }
                    }
                }
            }

            try (PreparedStatement psa = con.prepareStatement("UPDATE accounts SET tos = true WHERE id = ?")) {
                psa.setInt(1, accId);
                psa.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return disconnect;
    }

    public void lockClient() {
        lock.lock();
    }

    public void unlockClient() {
        lock.unlock();
    }

    public boolean tryacquireClient() {
        if (actionsSemaphore.tryAcquire()) {
            lockClient();
            return true;
        } else {
            return false;
        }
    }

    public void releaseClient() {
        unlockClient();
        actionsSemaphore.release();
    }

    public void lockEncoder() {
        encoderLock.lock();
    }

    public void unlockEncoder() {
        encoderLock.unlock();
    }

    private static class CharNameAndId {

        public String name;
        public int id;

        public CharNameAndId(String name, int id) {
            super();
            this.name = name;
            this.id = id;
        }
    }
    
    public void checkChar(int accid) {  /// issue with multiple chars from same account login found by shavit

        for (World w : Server.getInstance().getWorlds()) {
            for (MapleCharacter chr : w.getPlayerStorage().getAllCharacters()) {
                if (accid == chr.getAccountID()) {
                    FilePrinter.print(FilePrinter.LOGIN_ATTEMPTS, "Player:  " + chr.getName() + " has been removed from "
                            + GameConstants.WORLD_NAMES[w.getId()] + ". Possible double login attempt.");
                    chr.getClient().forceDisconnect();
                    w.getPlayerStorage().removePlayer(chr.getId());
                }
            }
        }
    }

    private static boolean checkHash(String hash, String type, String password) {
        try {
            MessageDigest digester = MessageDigest.getInstance(type);
            digester.update(password.getBytes("UTF-8"), 0, password.length());
            return HexTool.toString(digester.digest()).replace(" ", "").toLowerCase().equals(hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding the string failed", e);
        }
    }

    public short getAvailableCharacterSlots() {
        return (short) Math.max(0, characterSlots - Server.getInstance().getAccountCharacterCount(accId));
    }

    public short getAvailableCharacterWorldSlots() {
        return (short) Math.max(0, characterSlots - Server.getInstance().getAccountWorldCharacterCount(accId, world));
    }

    public short getCharacterSlots() {
        return characterSlots;
    }

    public void setCharacterSlots(byte slots) {
        characterSlots = slots;
    }

    public synchronized boolean gainCharacterSlot() {
        if (characterSlots < 15) {
            try (Connection con = DatabaseConnection.getConnection()) {
                Statements.Update("accounts").set("characterslots", this.characterSlots + 1).where("id", accId).execute(con);
                this.characterSlots += 1;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public final byte getGReason() {

        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT greason FROM accounts WHERE id = ?")) {
                ps.setInt(1, accId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getByte("greason");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public byte getGender() {
        return gender;
    }

    public void setGender(byte m) {
        this.gender = m;
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Update("accounts").set("gender", gender).where("id", accId).execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void announceDisableServerMessage() {
        if (!this.getWorldServer().registerDisabledServerMessage(player.getId())) {
            announce(BroadcastMsgPacket.Packet.onBroadcastBanner(""));
        }
    }

    public void announceServerMessage() {
        announce(BroadcastMsgPacket.Packet.onBroadcastBanner(this.getChannelServer().getServerMessage()));
    }

    public synchronized void announceBossHpBar(MapleMonster mm, final int mobHash, final byte[] packet) {
        long timeNow = System.currentTimeMillis();
        int targetHash = player.getTargetHpBarHash();

        if (mobHash != targetHash) {
            if (timeNow - player.getTargetHpBarTime() >= 5 * 1000) {
                // is there a way to INTERRUPT this annoying thread running on the client that drops the boss bar after some time at every attack?
                announceDisableServerMessage();
                announce(packet);

                player.setTargetHpBarHash(mobHash);
                player.setTargetHpBarTime(timeNow);
            }
        } else {
            announceDisableServerMessage();
            announce(packet);

            player.setTargetHpBarTime(timeNow);
        }
    }

    public synchronized void announce(final byte[] packet) {//MINA CORE IS A FUCKING BITCH AND I HATE IT <3
        session.write(packet);
    }

    public void announceHint(String msg, int length) {
        announce(UserLocal.Packet.onBalloonMessage(msg, length, 10));
        announce(WvsContext.Packet.enableActions());
    }

    public void changeChannel(int channel) {
        Server server = Server.getInstance();
        if (player.isBanned()) {
            disconnect(false, false);
            return;
        }
        if (!player.isAlive() || FieldLimit.CANNOTMIGRATE.check(player.getMap().getFieldLimit())) {
            announce(WvsContext.Packet.enableActions());
            return;
        } else if (MapleMiniDungeonInfo.isDungeonMap(player.getMapId())) {
            announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(),
                    "Changing channels or entering Cash Shop or MTS are disabled when inside a Mini-Dungeon."));
            announce(WvsContext.Packet.enableActions());
            return;
        }

        String[] socket = Server.getInstance().getInetSocket(getWorld(), channel);
        if (socket == null) {
            announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(),
                    "Channel " + channel + " is currently disabled. Try another channel."));
            announce(WvsContext.Packet.enableActions());
            return;
        }

        player.closePlayerInteractions();
        player.closePartySearchInteractions();

        //player.unregisterChairBuff();
        server.getPlayerBuffStorage().addBuffsToStorage(player.getId(), player.getAllBuffs());
        //server.getPlayerBuffStorage().addDiseasesToStorage(player.getId(), player.getAllDiseases());
        player.cancelMagicDoor();
        player.cancelBuffEffects();
        if (player.getBuffedValue(MapleBuffStat.PUPPET) != null) {
            player.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        }
        if (player.getBuffedValue(MapleBuffStat.COMBO) != null) {
            player.cancelEffectFromBuffStat(MapleBuffStat.COMBO);
        }
        player.setDisconnectedFromChannelWorld();
        player.notifyMapTransferToPartner(-1);
        player.removeIncomingInvites();
        player.cancelAllBuffs(true);
        player.cancelAllDebuffs();
        player.cancelBuffExpireTask();
        player.cancelDiseaseExpireTask();
        player.cancelSkillCooldownTask();
        player.cancelQuestExpirationTask();
        //Cancelling magicdoor? Nope
        //Cancelling mounts? Noty

        player.getInventory(MapleInventoryType.EQUIPPED).checked(false); //test
        player.getMap().removePlayer(player);
        player.clearBanishPlayerData();
        player.getClient().getChannelServer().removePlayer(player);

        player.saveCharToDB();
        player.getClient().updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
        player.setSessionTransitionState();
        try {
            announce(CLogin.Packet.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public boolean canRequestCharlist() {
        return lastNpcClick + 877 < Server.getInstance().getCurrentTime();
    }

    public boolean canClickNPC() {
        return lastNpcClick + 500 < Server.getInstance().getCurrentTime();
    }

    public void setClickedNPC() {
        lastNpcClick = Server.getInstance().getCurrentTime();
    }

    public void removeClickedNPC() {
        lastNpcClick = 0;
    }

    public int getVisibleWorlds() {
        return visibleWorlds;
    }

    public void requestedServerlist(int worlds) {
        visibleWorlds = worlds;
        setClickedNPC();
    }

    public void closePlayerScriptInteractions() {
        this.removeClickedNPC();
        /*NPCScriptManager.getInstance().dispose(this);
        QuestScriptManager.getInstance().dispose(this);*/
    }

    public boolean attemptCsCoupon() {
        if (csattempt > 2) {
            resetCsCoupon();
            return false;
        }

        csattempt++;
        return true;
    }

    public void resetCsCoupon() {
        csattempt = 0;
    }

    public void enableCSActions() {
        announce(CCashShop.Packet.onQueryCashResult(player));
    }

    public String getNibbleHWID() {
        return (String) session.getAttribute(MapleClient.CLIENT_NIBBLEHWID);
    }

    public int getLanguage() {
        return lang;
    }

    public void setLanguage(int lingua) {
        this.lang = lingua;
    }

    public final byte getClearance() {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT clearance FROM accounts WHERE id = ?")) {
                ps.setInt(1, accId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getByte("clearance");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void setClearance(byte c) {
        this.clearance = c;
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Update("accounts").set("clearance", clearance).where("id", accId).execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public final int getTrophy() {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT trophy FROM accounts WHERE id = ?")) {
                ps.setInt(1, accId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("trophy");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void setTrophy(int t) {
        this.trophy = t;
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Update("accounts").set("trophy", trophy).where("id", accId).execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
