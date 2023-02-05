/* 
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any otheer version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; witout even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.


 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client;

import client.listeners.DamageEvent;
import client.listeners.DamageListener;
import client.listeners.MobKilledEvent;
import client.listeners.MobKilledListener;
import constants.*;
import enums.*;
import net.database.DatabaseConnection;
import net.database.Statements;
import network.packet.*;
import network.packet.context.*;
import network.packet.field.CField;
import network.packet.field.MonsterCarnivalPacket;
import server.cashshop.CashShop;
import client.autoban.AutobanFactory;
import client.autoban.AutobanManager;
import client.autoban.AutobanTracker;
import client.creator.CharacterFactoryRecipe;
import client.inventory.*;
import client.inventory.Equip.StatUpgrade;
import client.inventory.manipulator.MapleInventoryManipulator;
import client.newyear.NewYearCardRecord;
import client.processor.FredrickProcessor;
import constants.skills.*;
import net.server.PlayerBuffValueHolder;
import net.server.PlayerCoolDownValueHolder;
import net.server.Server;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import net.server.coordinator.MapleInviteCoordinator;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.world.*;
import org.apache.mina.util.ConcurrentHashSet;
import scripting.AbstractPlayerInteraction;
import scripting.event.EventInstanceManager;
import scripting.item.ItemScriptManager;
import server.*;
import server.MapleItemInformationProvider.ScriptedItem;
import server.achievements.WorldTour;
import server.events.MapleEvents;
import server.events.RescueGaga;
import server.events.gm.MapleFitness;
import server.events.gm.MapleOla;
import server.life.*;
import server.loot.MapleLootManager;
import server.maps.*;
import server.maps.MapleMiniGame.MiniGameResult;
import server.maps.event.FishingLagoon;
import server.partyquest.*;
import server.quest.MapleQuest;
import server.skills.*;
import tools.*;
import tools.packets.Wedding;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.sql.*;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MapleCharacter extends AbstractMapleCharacterObject {
    private static final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    private static final String LEVEL_200 = "[Congrats] %s has reached Level %d! Congratulate %s on such an amazing achievement!";
    private static final String[] BLOCKED_NAMES = {"admin", "owner", "moderator", "intern", "donor", "administrator", "FREDRICK", "help", "helper", "alert", "notice", "maplestory", "fuck", "wizet", "fucking", "negro", "fuk", "fuc", "penis", "pussy", "asshole", "gay",
            "nigger", "homo", "suck", "cum", "shit", "shitty", "condom", "security", "official", "rape", "nigga", "sex", "tit", "boner", "orgy", "clit", "asshole", "fatass", "bitch", "support", "gamemaster", "cock", "gaay", "gm",
            "operate", "master", "sysop", "party", "GameMaster", "community", "message", "event", "test", "meso", "Scania", "yata", "AsiaSoft", "henesys"};

    private int world;
    private int accountid, id, level;
    private int playerRank, rankMove, jobRank, jobRankMove;
    private int gender, hair, face;
    private int fame, quest_fame;
    private int initialSpawnPoint;
    private int mapid;
    private int currentPage, currentType = 0, currentTab = 1;
    private int itemEffect;
    private int guildid, guildRank, allianceRank;
    private int messengerposition = 4;
    private int slots = 0;
    private int energybar;
    private int gmLevel;
    private int charClearance;
    private int charTrophy;
    private int ci = 0;
    private MapleFamily family;
    private int familyId;
    private int bookCover;
    private int battleshipHp = 0;
    private int mesosTraded = 0;
    private int possibleReports = 10;
    private int ariantPoints, dojoPoints, vanquisherStage, dojoStage, dojoEnergy, vanquisherKills;
    private int dropRate = 1, expCoupon = 1, mesoCoupon = 1, dropCoupon = 1;
    private int omokwins, omokties, omoklosses, matchcardwins, matchcardties, matchcardlosses;
    private int owlSearch;
    private long lastfametime, lastUsedCashItem, lastExpression = 0, lastHealed, lastBuyback = 0, lastDeathtime, jailExpiration = -1;
    private transient ScheduledFuture<?> fishing;
    private transient int localstr, localdex, localluk, localint_, localmagic, localwatk;
    private transient int equipmaxhp, equipmaxmp, equipstr, equipdex, equipluk, equipint_, equipmagic, equipwatk, localchairhp, localchairmp;
    private int localchairrate;
    private double expRate = 1.0, mesoRate = 1.0;
    private boolean hidden, equipchanged = true, berserk, hasMerchant, hasSandboxItem = false, whiteChat = false, canRecvPartySearchInvite = true, boomProtection = false;
    private boolean equippedMesoMagnet = false, equippedItemPouch = false, equippedPetItemIgnore = false;
    private int linkedLevel = 0;
    private String linkedName = null;
    private boolean finishedDojoTutorial;
    private boolean usedSafetyCharm = false;
    private boolean usedStorage = false;
    private String name;
    private String chalktext;
    private String commandtext;
    private String dataString;
    private String search = null;
    private AtomicBoolean mapTransitioning = new AtomicBoolean(true);  // player client is currently trying to change maps or log in the game map
    private AtomicBoolean awayFromWorld = new AtomicBoolean(true);  // player is online, but on cash shop or mts
    private AtomicInteger exp = new AtomicInteger();
    private AtomicInteger gachaexp = new AtomicInteger();
    private AtomicInteger meso = new AtomicInteger();
    private AtomicInteger chair = new AtomicInteger(-1);
    private int merchantmeso;
    private BuddyList buddylist;
    private EventInstanceManager eventInstance = null;
    private MapleHiredMerchant hiredMerchant = null;
    private MapleClient client;
    private BossQuest bossQuest;
    private MapleGuildCharacter mgc = null;
    private MaplePartyCharacter mpc = null;
    private MapleInventory[] inventory;
    private MapleJob job = MapleJob.BEGINNER;
    private MapleMessenger messenger = null;
    private MapleMiniGame miniGame;
    private MapleMount maplemount;
    private MapleParty party;
    private MaplePet[] pets = new MaplePet[3]; // max of 3 at once
    private MaplePlayerShop playerShop = null;
    private MapleShop shop = null;
    private MapleSkinColor skinColor = MapleSkinColor.NORMAL;
    private MapleStorage storage = null;
    private MapleTrade trade = null;
    private MonsterBook monsterbook;
    private CashShop cashshop;
    private Set<NewYearCardRecord> newyears = new LinkedHashSet<>();
    private SavedLocation[] savedLocations;
    private SkillMacro[] skillMacros = new SkillMacro[5];
    private List<Integer> lastmonthfameids;
    private List<WeakReference<MapleMap>> lastVisitedMaps = new LinkedList<>();
    private final Map<Short, MapleQuestStatus> quests;
    private Set<MapleMonster> controlled = new LinkedHashSet<>();
    private Map<Integer, String> entered = new LinkedHashMap<>();
    private Set<MapleMapObject> visibleMapObjects = new ConcurrentHashSet<>();
    private Map<PlayerSkill, SkillEntry> skills = new LinkedHashMap<>();
    private Map<Integer, Integer> activeCoupons = new LinkedHashMap<>();
    private Map<Integer, Integer> activeCouponRates = new LinkedHashMap<>();
    protected EnumMap<MapleBuffStat, MapleBuffStatValueHolder> effects = new EnumMap<>(MapleBuffStat.class);
    private Map<MapleDisease, Long> diseaseExpires = new LinkedHashMap<>();
    private Map<Integer, Long> buffExpires = new LinkedHashMap<>();
    private Map<Integer, MapleKeyBinding> keymap = new LinkedHashMap<>();
    protected ConcurrentMap<Integer, MapleSummon> summons = new ConcurrentHashMap<>();
    protected ConcurrentMap<Integer, MapleCoolDownValueHolder> coolDowns = new ConcurrentHashMap<>();
    private EnumMap<MapleDisease, Pair<MapleDiseaseValueHolder, MobSkill>> diseases = new EnumMap<>(MapleDisease.class);
    private Map<MapleQuest, Long> questExpirations = new LinkedHashMap<>();
    private ScheduledFuture<?> dragonBloodSchedule;
    private ScheduledFuture<?> hpDecreaseTask;
    private ScheduledFuture<?> beholderHealingSchedule, beholderBuffSchedule, berserkSchedule;
    private ScheduledFuture<?> skillCooldownTask = null;
    private ScheduledFuture<?> buffExpireTask = null;
    private ScheduledFuture<?> itemExpireTask = null;
    private ScheduledFuture<?> diseaseExpireTask = null;
    private ScheduledFuture<?> questExpireTask = null;
    private ScheduledFuture<?> recoveryTask = null;
    private ScheduledFuture<?> extraRecoveryTask = null;
    private ScheduledFuture<?> chairRecoveryTask = null;
    private ScheduledFuture<?> pendantOfSpirit = null; //1122017
    private ScheduledFuture<?> cpqSchedule = null;
    private Lock chrLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHARACTER_CHR, true);
    private Lock evtLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHARACTER_EVT, true);
    private Lock petLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHARACTER_PET, true);
    private Lock prtLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHARACTER_PRT);
    private Lock cpnLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHARACTER_CPN);
    private Map<Integer, Set<Integer>> excluded = new LinkedHashMap<>();
    private Set<Integer> excludedItems = new LinkedHashSet<>();
    private Set<Integer> disabledPartySearchInvites = new LinkedHashSet<>();
    private static String[] ariantroomleader = new String[3];
    private static int[] ariantroomslot = new int[3];
    private long portaldelay = 0, lastcombo = 0;
    private short combocounter = 0;
    private List<String> blockedPortals = new ArrayList<>();
    private Map<Short, String> area_info = new LinkedHashMap<>();
    private AutobanManager autoban;
    private AutobanTracker tracker;
    protected MapleDoor door;
    private boolean isbanned = false;
    private boolean ischeater = false;
    private boolean blockCashShop = false;
    private boolean allowExpGain = true;
    private byte pendantExp = 0, lastmobcount = 0;
    private List<Integer> trockmaps = new ArrayList<>();
    private List<Integer> viptrockmaps = new ArrayList<>();
    private Map<String, MapleEvents> events = new LinkedHashMap<>();
    private PartyQuest partyQuest = null;
    private MapleDragon dragon = null;
    private FishingLagoon fish;
    private MapleRing marriageRing;
    private int marriageItemid = -1;
    private int partnerId = -1;
    private List<MapleRing> crushRings = new ArrayList<>();
    private List<MapleRing> friendshipRings = new ArrayList<>();
    private boolean loggedIn = false;
    private long npcCd;
    private long petLootCd;
    private long lastHpDec = 0;
    private int newWarpMap = -1;
    private boolean canWarpMap = true;  //only one "warp" must be used per call, and this will define the right one.
    private int canWarpCounter = 0;     //counts how many times "inner warps" have been called.
    private byte extraHpRec = 0, extraMpRec = 0;
    private short extraRecInterval;
    private int targetHpBarHash = 0;
    private long targetHpBarTime = 0;
    private long nextWarningTime = 0;
    private int banishMap = -1;
    private int banishSp = -1;
    private int bossPoints = 0;
    private int bossRepeats = 0;
    private long banishTime = 0;
    private List<String> finishedWorldTour = new ArrayList<>();
    private WeakReference<MapleMap> ownedMap = new WeakReference<>(null);
    protected long login_time = System.currentTimeMillis();
    private int distanceHackCounter = 0;
    //listeners
    protected List<DamageListener> damage_listeners = new ArrayList<DamageListener>();
    protected ArrayList<MobKilledListener> mob_killed_listeners = new ArrayList<MobKilledListener>();
    //protected List<DropListener> drop_listeners = new ArrayList<DropListener>();

    private MapleCharacter() {
        super.setListener(new AbstractCharacterListener() {
            @Override
            public void onHpChanged(int oldHp) {
                hpChangeAction(oldHp);
            }

            @Override
            public void onHpmpPoolUpdate() {
                List<Pair<MapleStat, Integer>> hpmpupdate = recalcLocalStats();
                for (Pair<MapleStat, Integer> p : hpmpupdate) {
                    statUpdates.put(p.getLeft(), p.getRight());
                }

                if (hp > localmaxhp) {
                    setHp(localmaxhp);
                    statUpdates.put(MapleStat.HP, hp);
                }

                if (mp > localmaxmp) {
                    setMp(localmaxmp);
                    statUpdates.put(MapleStat.MP, mp);
                }
            }

            @Override
            public void onStatUpdate() {
                recalcLocalStats();
            }


            @Override
            public void onAnnounceStatPoolUpdate() {
                List<Pair<MapleStat, Integer>> statup = new ArrayList<>(8);
                for (Map.Entry<MapleStat, Integer> s : statUpdates.entrySet()) {
                    statup.add(new Pair<>(s.getKey(), s.getValue()));
                }

                announce(WvsContext.Packet.updatePlayerStats(statup, true, MapleCharacter.this));
            }
        });

        setStance(0);
        inventory = new MapleInventory[MapleInventoryType.values().length];
        savedLocations = new SavedLocation[SavedLocationType.values().length];

        for (MapleInventoryType type : MapleInventoryType.values()) {
            byte b = 24;
            if (type == MapleInventoryType.CASH) {
                b = 96;
            }
            inventory[type.ordinal()] = new MapleInventory(this, type, b);
        }
        inventory[MapleInventoryType.CANHOLD.ordinal()] = new MapleInventoryProof(this);

        for (int i = 0; i < SavedLocationType.values().length; i++) {
            savedLocations[i] = null;
        }
        quests = new LinkedHashMap<>();
        setPosition(new Point(0, 0));

        petLootCd = Server.getInstance().getCurrentTime();
    }

    private static MapleJob getJobStyleInternal(int jobid, byte opt) {
        int jobtype = jobid / 100;

        if (jobtype == MapleJob.WARRIOR.getId() / 100 || jobtype == MapleJob.DAWNWARRIOR1.getId() / 100 || jobtype == MapleJob.ARAN1.getId() / 100) {
            return (MapleJob.WARRIOR);
        } else if (jobtype == MapleJob.MAGICIAN.getId() / 100 || jobtype == MapleJob.BLAZEWIZARD1.getId() / 100 || jobtype == MapleJob.EVAN1.getId() / 100) {
            return (MapleJob.MAGICIAN);
        } else if (jobtype == MapleJob.BOWMAN.getId() / 100 || jobtype == MapleJob.WINDARCHER1.getId() / 100) {
            if (jobid / 10 == MapleJob.CROSSBOWMAN.getId() / 10) {
                return (MapleJob.CROSSBOWMAN);
            } else {
                return (MapleJob.BOWMAN);
            }
        } else if (jobtype == MapleJob.THIEF.getId() / 100 || jobtype == MapleJob.NIGHTWALKER1.getId() / 100) {
            return (MapleJob.THIEF);
        } else if (jobtype == MapleJob.PIRATE.getId() / 100 || jobtype == MapleJob.THUNDERBREAKER1.getId() / 100) {
            if (opt == (byte) 0x80) {
                return (MapleJob.BRAWLER);
            } else {
                return (MapleJob.GUNSLINGER);
            }
        }

        return (MapleJob.BEGINNER);
    }

    public MapleJob getJobStyle(byte opt) {
        return getJobStyleInternal(this.getJob().getId(), opt);
    }

    public MapleJob getJobStyle() {
        return getJobStyle((byte) ((this.getStr() > this.getDex()) ? 0x80 : 0x40));
    }

    public static MapleCharacter getDefault(MapleClient c) {
        MapleCharacter ret = new MapleCharacter();
        ret.client = c;
        ret.gmLevel = 0;
        ret.hp = 50;
        ret.setMaxHp(50);
        ret.mp = 5;
        ret.setMaxMp(5);
        ret.str = 12;
        ret.dex = 5;
        ret.int_ = 4;
        ret.luk = 4;
        ret.map = null;
        ret.job = MapleJob.BEGINNER;
        ret.level = 1;
        ret.accountid = c.getAccID();
        ret.buddylist = new BuddyList(20);
        ret.maplemount = null;
        ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(48);
        ret.getInventory(MapleInventoryType.USE).setSlotLimit(48);
        ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(48);
        ret.getInventory(MapleInventoryType.ETC).setSlotLimit(48);

        // Select a keybinding method
        int[] selectedKey;
        int[] selectedType;
        int[] selectedAction;

        selectedKey = GameConstants.getCustomKey(false);
        selectedType = GameConstants.getCustomType(false);
        selectedAction = GameConstants.getCustomAction(false);

        for (int i = 0; i < selectedKey.length; i++) {
            ret.keymap.put(selectedKey[i], new MapleKeyBinding(selectedType[i], selectedAction[i]));
        }


        //to fix the map 0 lol
        for (int i = 0; i < 5; i++) {
            ret.trockmaps.add(999999999);
        }
        for (int i = 0; i < 10; i++) {
            ret.viptrockmaps.add(999999999);
        }

        return ret;
    }

    public boolean isLoggedinWorld() {
        return this.isLoggedin() && !this.isAwayFromWorld();
    }

    public boolean isAwayFromWorld() {
        return awayFromWorld.get();
    }

    public void setEnteredChannelWorld() {
        awayFromWorld.set(false);
        client.getChannelServer().removePlayerAway(id);
        if (canRecvPartySearchInvite) {
            this.getWorldServer().getPartySearchCoordinator().attachPlayer(this);
        }
    }

    public void setAwayFromChannelWorld() {
        setAwayFromChannelWorld(false);
    }

    public void setDisconnectedFromChannelWorld() {
        setAwayFromChannelWorld(true);
    }

    private void setAwayFromChannelWorld(boolean disconnect) {
        awayFromWorld.set(true);

        if (!disconnect) {
            client.getChannelServer().insertPlayerAway(id);
        } else {
            client.getChannelServer().removePlayerAway(id);
        }
    }

    public void setSessionTransitionState() {
        client.getSession().setAttribute(MapleClient.CLIENT_TRANSITION);
    }

    public long getPetLootCd() {
        return petLootCd;
    }

    public void setPetLootCd(long cd) {
        petLootCd = cd;
    }

    public long getNpcCooldown() {
        return npcCd;
    }

    public void setNpcCooldown(long d) {
        npcCd = d;
    }

    public void setOwlSearch(int id) {
        owlSearch = id;
    }

    public int getOwlSearch() {
        return owlSearch;
    }

    public void addCooldown(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
        this.coolDowns.put(skillId, new MapleCoolDownValueHolder(skillId, startTime, length, timer));
    }

    public void addCrushRing(MapleRing r) {
        crushRings.add(r);
    }

    public MapleRing getRingById(int id) {
        for (MapleRing ring : getCrushRings()) {
            if (ring.getRingId() == id) {
                return ring;
            }
        }
        for (MapleRing ring : getFriendshipRings()) {
            if (ring.getRingId() == id) {
                return ring;
            }
        }

        if (marriageRing != null) {
            if (marriageRing.getRingId() == id) {
                return marriageRing;
            }
        }

        return null;
    }

    public int getMarriageItemId() {
        return marriageItemid;
    }

    public void setMarriageItemId(int itemid) {
        marriageItemid = itemid;
    }

    public int getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(int partnerid) {
        partnerId = partnerid;
    }

    public int getRelationshipId() {
        return getWorldServer().getRelationshipId(id);
    }

    public boolean isMarried() {
        return marriageRing != null && partnerId > 0;
    }

    public boolean hasJustMarried() {
        EventInstanceManager eim = getEventInstance();
        if (eim != null) {
            String prop = eim.getProperty("groomId");

            if (prop != null) {
                return (Integer.parseInt(prop) == id || eim.getIntProperty("brideId") == id) && (mapid == 680000110 || mapid == 680000210);
            }
        }

        return false;
    }

    public int addDojoPointsByMap(int mapid) {
        int pts = 0;
        if (dojoPoints < 17000) {
            pts = 1 + ((mapid - 1) / 100 % 100) / 6;
            if (!getDojoParty()) {
                pts++;
            }
            this.dojoPoints += pts;
        }
        return pts;
    }

    public void setDoor(MapleDoor door) {
        this.door = door;
    }

    public void addFame(int famechange) {
        this.fame += famechange;
    }

    public void addFriendshipRing(MapleRing r) {
        friendshipRings.add(r);
    }

    public void addMarriageRing(MapleRing r) {
        marriageRing = r;
    }

    public void addMesosTraded(int gain) {
        this.mesosTraded += gain;
    }

    public void addPet(MaplePet pet) {
        petLock.lock();
        try {
            for (int i = 0; i < 3; i++) {
                if (pets[i] == null) {
                    pets[i] = pet;
                    return;
                }
            }
        } finally {
            petLock.unlock();
        }
    }

    public void addSummon(int id, MapleSummon summon) {
        summons.put(id, summon);
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.add(mo);
    }

    public void cheating(String reason) {
        this.ischeater = true;
        if (getClient().getChannel() != GameConstants.CHEATER_CHANNEL) this.getClient().changeChannel(GameConstants.CHEATER_CHANNEL);
        System.out.println("[CHEATING] " + this.name + ": " + reason);
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Update("accounts").set("cheater", true).set("banreason", reason).where("id", accountid).execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void ban(String reason) {
        this.isbanned = true;
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Update("accounts").set("banned", true).set("banreason", reason).where("id", accountid).execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean ban(String id, String reason, boolean accountId) {
        try (Connection con = DatabaseConnection.getConnection()) {
            if (id.matches("/[0-9]{1,3}\\..*")) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO ip_bans VALUES (DEFAULT, ?)")) {
                    ps.setString(1, id);
                    ps.executeUpdate();
                    return true;
                }
            }

            String sql;
            if (accountId) {
                sql = "SELECT id FROM accounts WHERE name = ?";
            } else {
                sql = "SELECT accountid FROM characters WHERE name = ?";
            }

            boolean ret = false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Statements.Update("accounts").set("banned", true).set("banreason", reason).where("id", rs.getInt(1)).execute(con);
                        ret = true;
                    }
                }
            }
            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public int calculateMaxBaseDamage(int watk) {
        int maxbasedamage;
        Item weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
        if (weapon_item != null) {
            MapleWeaponType weapon = ii.getWeaponType(weapon_item.getItemId());
            int mainstat, secondarystat;
            if (getJob().isA(MapleJob.THIEF) && weapon == MapleWeaponType.DAGGER_OTHER) {
                weapon = MapleWeaponType.DAGGER_THIEVES;
            }

            if (weapon == MapleWeaponType.BOW || weapon == MapleWeaponType.CROSSBOW || weapon == MapleWeaponType.GUN) {
                mainstat = localdex;
                secondarystat = localstr;
            } else if (weapon == MapleWeaponType.CLAW || weapon == MapleWeaponType.DAGGER_THIEVES) {
                mainstat = localluk;
                secondarystat = localdex + localstr;
            } else {
                mainstat = localstr;
                secondarystat = localdex;
            }
            maxbasedamage = (int) (((weapon.getMaxDamageMultiplier() * mainstat + secondarystat) / 100.0) * watk);
        } else {
            if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
                double weapMulti = 3;
                if (job.getId() % 100 != 0) {
                    weapMulti = 4.2;
                }

                int attack = (int) Math.min(Math.floor((2 * getLevel() + 31) / 3), 31);
                maxbasedamage = (int) (localstr * weapMulti + localdex) * attack / 100;
            } else {
                maxbasedamage = 1;
            }
        }
        return maxbasedamage;
    }

    public int calculateMaxBaseMagicDamage() {
        int maxbasedamage = getTotalMagic();
        int totalint = getTotalInt();

        if (totalint > 2000) {
            maxbasedamage -= 2000;
            maxbasedamage += (int) ((0.09033024267 * totalint) + 3823.8038);
        } else {
            maxbasedamage -= totalint;

            if (totalint > 1700) {
                maxbasedamage += (int) (0.1996049769 * Math.pow(totalint, 1.300631341));
            } else {
                maxbasedamage += (int) (0.1996049769 * Math.pow(totalint, 1.290631341));
            }
        }

        return (maxbasedamage * 107) / 100;
    }

    public void setCombo(short count) {
        if (count < combocounter) {
            cancelEffectFromBuffStat(MapleBuffStat.ARAN_COMBO);
        }
        combocounter = (short) Math.min(30000, count);
        if (count > 0) {
            announce(UserLocal.Packet.onIncComboResponse(combocounter));
        }
    }

    public void setLastCombo(long time) {
        lastcombo = time;
    }

    public short getCombo() {
        return combocounter;
    }

    public long getLastCombo() {
        return lastcombo;
    }

    public int getLastMobCount() { //Used for skills that have mobCount at 1. (a/b)
        return lastmobcount;
    }

    public void setLastMobCount(byte count) {
        lastmobcount = count;
    }

    public boolean cannotEnterCashShop() {
        return blockCashShop;
    }

    public void toggleBlockCashShop() {
        blockCashShop = !blockCashShop;
    }

    public void toggleExpGain() {
        allowExpGain = !allowExpGain;
    }

    public void setClient(MapleClient c) {
        this.client = c;
    }

    public void newClient(MapleClient c) {
        this.loggedIn = true;
        c.setAccountName(this.client.getAccountName());//No null's for accountName
        this.setClient(c);
        this.map = c.getChannelServer().getMapFactory().getMap(getMapId());
        MaplePortal portal = map.findClosestPlayerSpawnpoint(getPosition());
        if (portal == null) {
            portal = map.getPortal(0);
        }
        this.setPosition(portal.getPosition());
        this.initialSpawnPoint = portal.getId();
    }

    public String getMedalText() {
        String medal = "";
        final Item medalItem = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -49);
        if (medalItem != null) {
            medal = "<" + ii.getName(medalItem.getItemId()) + "> ";
        }
        return medal;
    }

    public void Hide(boolean hide, boolean login) {
        if (isGM() && hide != this.hidden) {
            if (!hide) {
                this.hidden = false;
                announce(CField.Packet.onAdminResult(0x10, (byte) 0));
                List<MapleBuffStat> dsstat = Collections.singletonList(MapleBuffStat.DARKSIGHT);
                getMap().broadcastGMMessage(this, UserRemote.Packet.cancelForeignBuff(id, dsstat), false);
                getMap().broadcastSpawnPlayerMapObjectMessage(this, this, false);
                for (MapleSummon ms : this.getSummonsValues()) {
                    getMap().broadcastNONGMMessage(this, SummonedPool.Packet.onSummonCreated(ms, false), false);
                }
                for (MapleMonster mon : this.getControlledMonsters()) {
                    mon.setController(null);
                    mon.setControllerHasAggro(false);
                    mon.setControllerKnowsAboutAggro(false);
                    mon.getMap().updateMonsterController(mon);
                }
            } else {
                this.hidden = true;
                announce(CField.Packet.onAdminResult(0x10, (byte) 1));
                if (!login) {
                    getMap().broadcastNONGMMessage(this, UserPool.Packet.onUserLeaveField(getId()), false);
                }
                List<Pair<MapleBuffStat, BuffValueHolder>> ldsstat = Collections.singletonList(
                        new Pair<>(MapleBuffStat.DARKSIGHT, new BuffValueHolder(0, 0, 0)));
                getMap().broadcastGMMessage(this, UserRemote.Packet.giveForeignBuff(id, ldsstat), false);
                //this.releaseControlledMonsters();
            }
            announce(WvsContext.Packet.enableActions());
        }
    }

    public void Hide(boolean hide) {
        Hide(hide, false);
    }

    public void toggleHide(boolean login) {
        Hide(!hidden);
    }

    public void cancelMagicDoor() {
        for (MapleBuffStatValueHolder mbsvh : new ArrayList<>(effects.values())) {
            if (mbsvh.effect.isMagicDoor()) {
                cancelEffect(mbsvh.effect, false, -1);
            }
        }
    }

    public void cancelPlayerBuffs(List<MapleBuffStat> buffstats) {
        if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
//            dropMessage("recalcLocalStats() in cancelPlayerBuffs()");
            recalcLocalStats();
            enforceMaxHpMp();
            client.announce(WvsContext.Packet.cancelBuff(buffstats));
            if (buffstats.size() > 0) {
                getMap().broadcastMessage(
                        this,
                        UserRemote.Packet.cancelForeignBuff(getId(), buffstats),
                        false);
            }
        }
    }

    public static boolean canCreateChar(String name) {
        String lname = name.toLowerCase();
        for (String nameTest : BLOCKED_NAMES) {
            if (lname.contains(nameTest)) {
                return false;
            }
        }
        return getIdByName(name) < 0 && Pattern.compile("[a-zA-Z0-9]{3,12}").matcher(name).matches();
    }

    public void setHasSandboxItem() {
        hasSandboxItem = true;
    }

    public void removeSandboxItems() {  // sandbox idea thanks to Morty
        if (!hasSandboxItem) {
            return;
        }

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (MapleInventoryType invType : MapleInventoryType.values()) {
            MapleInventory inv = this.getInventory(invType);

            inv.lockInventory();
            try {
                for (Item item : new ArrayList<>(inv.list())) {
                    if (MapleInventoryManipulator.isSandboxItem(item)) {
                        MapleInventoryManipulator.removeFromSlot(client, invType, item.getPosition(), item.getQuantity(), false);
                        dropMessage(5, "[" + ii.getName(item.getItemId()) + "] has passed its trial conditions and will be removed from your inventory.");
                    }
                }
            } finally {
                inv.unlockInventory();
            }
        }

        hasSandboxItem = false;
    }

    public boolean canDoor() {
        if (door != null) {
            return door.canCreateNew();
        }
        return true;
    }

    public int canGiveFame(MapleCharacter from) {
        if (this.isGM()) {
            return PopularityResponseType.GiveSuccess.getValue();
        } else if (lastfametime >= System.currentTimeMillis() - 3600000 * 24) {
            return PopularityResponseType.AlreadyUsedDay.getValue();
        } else if (lastmonthfameids.contains(from.getId())) {
            return PopularityResponseType.AlreadyUsedMonth.getValue();
        } else if (this.getLevel() < 15) {
            return PopularityResponseType.UnderLevel15.getValue();
        } else {
            return PopularityResponseType.GiveSuccess.getValue();
        }
    }

    public void changeCI(int type) {
        this.ci = type;
    }

    public void setMasteries(int jobId) {
        int[] skills = new int[4];
        for (int i = 0; i > skills.length; i++) {
            skills[i] = 0; //that initialization meng
        }
        if (jobId == 112) {
            skills[0] = Hero.ACHILLES;
            skills[1] = Hero.MONSTER_MAGNET;
            skills[2] = Hero.BRANDISH;
        } else if (jobId == 122) {
            skills[0] = Paladin.ACHILLES;
            skills[1] = Paladin.MONSTER_MAGNET;
            skills[2] = Paladin.BLAST;
        } else if (jobId == 132) {
            skills[0] = DarkKnight.BEHOLDER;
            skills[1] = DarkKnight.ACHILLES;
            skills[2] = DarkKnight.MONSTER_MAGNET;
        } else if (jobId == 212) {
            skills[0] = FPArchMage.BIG_BANG;
            skills[1] = FPArchMage.MANA_REFLECTION;
            skills[2] = FPArchMage.PARALYZE;
        } else if (jobId == 222) {
            skills[0] = ILArchMage.BIG_BANG;
            skills[1] = ILArchMage.MANA_REFLECTION;
            skills[2] = ILArchMage.CHAIN_LIGHTNING;
        } else if (jobId == 232) {
            skills[0] = Bishop.BIG_BANG;
            skills[1] = Bishop.MANA_REFLECTION;
            skills[2] = Bishop.HOLY_SHIELD;
        } else if (jobId == 312) {
            skills[0] = Bowmaster.BOW_EXPERT;
            skills[1] = Bowmaster.HAMSTRING;
            skills[2] = Bowmaster.SHARP_EYES;
        } else if (jobId == 322) {
            skills[0] = Marksman.MARKSMAN_BOOST;
            skills[1] = Marksman.BLIND;
            skills[2] = Marksman.SHARP_EYES;
        } else if (jobId == 412) {
            skills[0] = NightLord.SHADOW_STARS;
            skills[1] = NightLord.SHADOW_SHIFTER;
            skills[2] = NightLord.VENOMOUS_STAR;
        } else if (jobId == 422) {
            skills[0] = Shadower.SHADOW_SHIFTER;
            skills[1] = Shadower.VENOMOUS_STAB;
            skills[2] = Shadower.BOOMERANG_STEP;
        } else if (jobId == 512) {
            skills[0] = Buccaneer.BARRAGE;
            skills[1] = Buccaneer.ENERGY_ORB;
            skills[2] = Buccaneer.SPEED_INFUSION;
            skills[3] = Buccaneer.DRAGON_STRIKE;
        } else if (jobId == 522) {
            skills[0] = Corsair.ELEMENTAL_BOOST;
            skills[1] = Corsair.BULLSEYE;
            skills[2] = Corsair.WRATH_OF_THE_OCTOPI;
            skills[3] = Corsair.RAPID_FIRE;
        } else if (jobId == 2112) {
            skills[0] = Aran.OVER_SWING;
            skills[1] = Aran.HIGH_MASTERY;
            skills[2] = Aran.FREEZE_STANDING;
        } else if (jobId == 2217) {
            skills[0] = Evan.MAPLE_WARRIOR;
            skills[1] = Evan.ILLUSION;
        } else if (jobId == 2218) {
            skills[0] = Evan.BLESSING_OF_THE_ONYX;
            skills[1] = Evan.BLAZE;
        }
        for (Integer skillId : skills) {
            if (skillId != 0) {
                PlayerSkill skill = SkillFactory.getSkill(skillId);
                final int skilllevel = getSkillLevel(skill);
                if (skilllevel > 0) {
                    continue;
                }

                changeSkillLevel(skill, (byte) 0, 10, -1);
            }
        }
    }

    public synchronized void changeJob(MapleJob newJob) {
        if (newJob == null) {
            return;//the fuck you doing idiot!
        }

        if (canRecvPartySearchInvite && getParty() == null) {
            this.updatePartySearchAvailability(false);
            this.job = newJob;
            this.updatePartySearchAvailability(true);
        } else {
            this.job = newJob;
        }

        int spGain = 1;
        if (GameConstants.hasSPTable(newJob)) {
            spGain += 2;
        } else {
            if (newJob.getId() % 10 == 2) {
                spGain += 2;
            }
        }

        if (spGain > 0) {
            gainSp(spGain, GameConstants.getSkillBook(newJob.getId()), true);
        }

        // thanks xinyifly for finding out missing AP awards (AP Reset can be used as a compass)
        if (newJob.getId() % 100 >= 1) {
            if (this.isCygnus()) {
                gainAp(7, true);
            } else {
                gainAp(5, true);
            }
        }

        boolean firstJob = getJob().getId() % 100 == 0;
        if (!isGM() && !firstJob) {
            for (int i = 1; i < 6; i++) { // 5 inv types 1-5
                gainSlots(i, 16);

            }
        }

        int addhp = 0, addmp = 0;
        int job_ = job.getId() % 1000; // lame temp "fix"
        if (job_ == 100) {                      // 1st warrior
            addhp += Randomizer.rand(200, 250);
        } else if (job_ == 200) {               // 1st mage
            addmp += Randomizer.rand(100, 150);
        } else if (job_ % 100 == 0) {           // 1st others
            addhp += Randomizer.rand(100, 150);
            addhp += Randomizer.rand(25, 50);
        } else if (job_ > 0 && job_ < 200) {    // 2nd~4th warrior
            addhp += Randomizer.rand(300, 350);
        } else if (job_ < 300) {                // 2nd~4th mage
            addmp += Randomizer.rand(450, 500);
        } else if (job_ > 0) {                  // 2nd~4th others
            addhp += Randomizer.rand(300, 350);
            addmp += Randomizer.rand(150, 200);
        }
        
        /*
        //aran perks?
        int newJobId = newJob.getId();
        if(newJobId == 2100) {          // become aran1
            addhp += 275;
            addmp += 15;
        } else if(newJobId == 2110) {   // become aran2
            addmp += 275;
        } else if(newJobId == 2111) {   // become aran3
            addhp += 275;
            addmp += 275;
        }
        */

        effLock.lock();
        statWlock.lock();
        try {
            addMaxMPMaxHP(addhp, addmp, true);
            recalcLocalStats();

            List<Pair<MapleStat, Integer>> statup = new ArrayList<>(7);
            statup.add(new Pair<>(MapleStat.HP, hp));
            statup.add(new Pair<>(MapleStat.MP, mp));
            statup.add(new Pair<>(MapleStat.MAXHP, clientmaxhp));
            statup.add(new Pair<>(MapleStat.MAXMP, clientmaxmp));
            statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
            statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp[GameConstants.getSkillBook(job.getId())]));
            statup.add(new Pair<>(MapleStat.JOB, job.getId()));
            client.announce(WvsContext.Packet.updatePlayerStats(statup, true, this));
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }

        setMPC(new MaplePartyCharacter(this));
        silentPartyUpdate();

        if (dragon != null) {
            getMap().broadcastMessage(DragonPacket.Packet.onRemoveField(dragon.getObjectId()));
            dragon = null;
        }

        if (this.guildid > 0) {
            getGuild().broadcast(WvsContext.Packet.onNotifyJobChange(0, job.getId(), name), this.getId());
        }
        setMasteries(this.job.getId());
        guildUpdate();

        getMap().broadcastMessage(this, UserRemote.Packet.showForeignEffect(this.getId(), 8), false);

        if (GameConstants.hasSPTable(newJob) && newJob.getId() != 2001) {
            if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
                cancelBuffStats(MapleBuffStat.MONSTER_RIDING);
            }
            createDragon();
        }

        if (!this.isGM()) {
            broadcastAcquaintances(6, "[" + GameConstants.ordinal(GameConstants.getJobBranch(newJob)) + " Job] " + name + " has just become a " + GameConstants.getJobName(this.job.getId()) + ".");    // thanks Vcoc for noticing job name appearing in uppercase here
        }
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if (!monster.isControllerHasAggro()) {
            if (monster.getController() == this) {
                monster.setControllerHasAggro(true);
            } else {
                monster.switchController(this, true);
            }
        }
    }

    public void controlMonster(MapleMonster monster, boolean aggro) {
        monster.setController(this);
        controlled.add(monster);
        client.announce(MaplePacketCreator.controlMonster(monster, MobSpawnType.NORMAL.getType(), aggro));
    }

    public void broadcastAcquaintances(int type, String message) {
        broadcastAcquaintances(MaplePacketCreator.serverNotice(type, message));
    }

    public void broadcastAcquaintances(byte[] packet) {
        buddylist.broadcast(packet, getWorldServer().getPlayerStorage());

        if (family != null) {
            //family.broadcast(packet, id); not yet implemented
        }

        MapleGuild guild = getGuild();
        if (guild != null) {
            guild.broadcast(packet, id);
        }
        
        /*
        if(partnerid > 0) {
            partner.announce(packet); not yet implemented
        }
        */
        announce(packet);
    }

    public void changeKeybinding(int key, MapleKeyBinding keybinding) {
        if (keybinding.getType() != 0) {
            keymap.put(Integer.valueOf(key), keybinding);
        } else {
            keymap.remove(Integer.valueOf(key));
        }
    }

    public MapleMap getWarpMap(int map) {
        MapleMap warpMap;
        EventInstanceManager eim = getEventInstance();
        if (eim != null) {
            warpMap = eim.getMapInstance(map);
        } else if (this.getMonsterCarnival() != null && this.getMonsterCarnival().getEventMap().getId() == map) {
            warpMap = this.getMonsterCarnival().getEventMap();
        } else {
            warpMap = client.getChannelServer().getMapFactory().getMap(map);
        }
        return warpMap;
    }

    // for use ONLY inside OnUserEnter map scripts that requires a player to change map while still moving between maps.
    public void warpAhead(int map) {
        newWarpMap = map;
    }

    private void eventChangedMap(int map) {
        EventInstanceManager eim = getEventInstance();
        if (eim != null) {
            eim.changedMap(this, map);
        }
    }

    private void eventAfterChangedMap(int map) {
        EventInstanceManager eim = getEventInstance();
        if (eim != null) {
            eim.afterChangedMap(this, map);
        }
    }

    public boolean canRecoverLastBanish() {
        return System.currentTimeMillis() - this.banishTime < 5 * 60 * 1000;
    }

    public Pair<Integer, Integer> getLastBanishData() {
        return new Pair<>(this.banishMap, this.banishSp);
    }

    public void clearBanishPlayerData() {
        this.banishMap = -1;
        this.banishSp = -1;
        this.banishTime = 0;
    }

    public void setBanishPlayerData(int banishMap, int banishSp, long banishTime) {
        this.banishMap = banishMap;
        this.banishSp = banishSp;
        this.banishTime = banishTime;
    }

    public void changeMapBanish(int mapid, String portal, String msg) {
        int banMap = this.getMapId();
        int banSp = this.getMap().findClosestPlayerSpawnpoint(this.getPosition()).getId();
        long banTime = System.currentTimeMillis();

        if (msg != null) {
            dropMessage(5, msg);
        }

        MapleMap map_ = getWarpMap(mapid);
        MaplePortal portal_ = map_.getPortal(portal);
        changeMap(map_, portal_ != null ? portal_ : map_.getRandomPlayerSpawnpoint());

        setBanishPlayerData(banMap, banSp, banTime);
    }

    public void changeMap(int map) {
        MapleMap warpMap;
        EventInstanceManager eim = getEventInstance();

        if (eim != null) {
            warpMap = eim.getMapInstance(map);
        } else {
            warpMap = client.getChannelServer().getMapFactory().getMap(map);
        }

        changeMap(warpMap, warpMap.getRandomPlayerSpawnpoint());
    }

    public void changeMap(int map, int portal) {
        MapleMap warpMap;
        EventInstanceManager eim = getEventInstance();

        if (eim != null) {
            warpMap = eim.getMapInstance(map);
        } else {
            warpMap = client.getChannelServer().getMapFactory().getMap(map);
        }

        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, String portal) {
        MapleMap warpMap;
        EventInstanceManager eim = getEventInstance();

        if (eim != null) {
            warpMap = eim.getMapInstance(map);
        } else {
            warpMap = client.getChannelServer().getMapFactory().getMap(map);
        }

        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, MaplePortal portal) {
        MapleMap warpMap;
        EventInstanceManager eim = getEventInstance();

        if (eim != null) {
            warpMap = eim.getMapInstance(map);
        } else {
            warpMap = client.getChannelServer().getMapFactory().getMap(map);
        }

        changeMap(warpMap, portal);
    }

    public void changeMap(MapleMap to) {
        changeMap(to, 0);
    }

    public void changeMap(MapleMap to, int portal) {
        changeMap(to, to.getPortal(portal));
    }

    public void changeMap(final MapleMap target, final MaplePortal pto) {
        canWarpCounter++;

        eventChangedMap(target.getId());    // player can be dropped from an event here, hence the new warping target.
        MapleMap to = getWarpMap(target.getId());
        changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId(), this));
        canWarpMap = false;

        canWarpCounter--;
        if (canWarpCounter == 0) {
            canWarpMap = true;
        }

        eventAfterChangedMap(this.getMapId());
    }

    public void changeMap(final MapleMap target, final Point pos) {
        canWarpCounter++;

        eventChangedMap(target.getId());
        MapleMap to = getWarpMap(target.getId());
        changeMapInternal(to, pos, MaplePacketCreator.getWarpToMap(to, 0x80, pos, this));
        canWarpMap = false;

        canWarpCounter--;
        if (canWarpCounter == 0) {
            canWarpMap = true;
        }

        eventAfterChangedMap(this.getMapId());
    }

    public void forceChangeMap(final MapleMap target, final MaplePortal pto) {
        // will actually enter the map given as parameter, regardless of being an eventmap or whatnot

        canWarpCounter++;
        eventChangedMap(999999999);

        EventInstanceManager mapEim = target.getEventInstance();
        if (mapEim != null) {
            EventInstanceManager playerEim = this.getEventInstance();
            if (playerEim != null) {
                playerEim.exitPlayer(this);
                if (playerEim.getPlayerCount() == 0) {
                    playerEim.dispose();
                }
            }

            // thanks Thora for finding an issue with players not being actually warped into the target event map (rather sent to the event starting map)
            mapEim.registerPlayer(this, false);
        }

        MapleMap to = target; // warps directly to the target intead of the target's map id, this allows GMs to patrol players inside instances.
        changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId(), this));
        canWarpMap = false;

        canWarpCounter--;
        if (canWarpCounter == 0) {
            canWarpMap = true;
        }

        eventAfterChangedMap(this.getMapId());
    }

    public List<Integer> getLastVisitedMapids() {
        List<Integer> lastVisited = new ArrayList<>(5);

        petLock.lock();
        try {
            for (WeakReference<MapleMap> lv : lastVisitedMaps) {
                MapleMap lvm = lv.get();

                if (lvm != null) {
                    lastVisited.add(lvm.getId());
                }
            }
        } finally {
            petLock.unlock();
        }

        return lastVisited;
    }

    public void partyOperationUpdate(MapleParty party, List<MapleCharacter> exPartyMembers) {
        List<WeakReference<MapleMap>> mapids;

        petLock.lock();
        try {
            mapids = new LinkedList<>(lastVisitedMaps);
        } finally {
            petLock.unlock();
        }

        List<MapleCharacter> partyMembers = new LinkedList<>();
        for (MapleCharacter mc : (exPartyMembers != null) ? exPartyMembers : this.getPartyMembers()) {
            if (mc.isLoggedinWorld()) {
                partyMembers.add(mc);
            }
        }

        MapleCharacter partyLeaver = null;
        if (exPartyMembers != null) {
            partyMembers.remove(this);
            partyLeaver = this;
        }

        MapleMap map = this.getMap();
        List<MapleMapItem> partyItems = null;

        int partyId = exPartyMembers != null ? -1 : this.getPartyId();
        for (WeakReference<MapleMap> mapRef : mapids) {
            MapleMap mapObj = mapRef.get();

            if (mapObj != null) {
                List<MapleMapItem> partyMapItems = mapObj.updatePlayerItemDropsToParty(partyId, id, partyMembers, partyLeaver);
                if (map.hashCode() == mapObj.hashCode()) {
                    partyItems = partyMapItems;
                }
            }
        }

        if (partyItems != null && exPartyMembers == null) {
            map.updatePartyItemDropsToNewcomer(this, partyItems);
        }

        //updatePartyTownDoors(party, this, partyLeaver, partyMembers);
    }

    private Integer getVisitedMapIndex(MapleMap map) {
        int idx = 0;

        for (WeakReference<MapleMap> mapRef : lastVisitedMaps) {
            if (map.equals(mapRef.get())) {
                return idx;
            }

            idx++;
        }

        return -1;
    }

    public void visitMap(MapleMap map) {
        petLock.lock();
        try {
            int idx = getVisitedMapIndex(map);

            if (idx == -1) {
                if (lastVisitedMaps.size() == ServerConstants.MAP_VISITED_SIZE) {
                    lastVisitedMaps.remove(0);
                }
            } else {
                WeakReference<MapleMap> mapRef = lastVisitedMaps.remove(idx);
                lastVisitedMaps.add(mapRef);
                return;
            }

            lastVisitedMaps.add(new WeakReference<>(map));
        } finally {
            petLock.unlock();
        }
    }

    public void notifyMapTransferToPartner(int mapid) {
        if (partnerId > 0) {
            final MapleCharacter partner = getWorldServer().getPlayerStorage().getCharacterById(partnerId);
            if (partner != null && !partner.isAwayFromWorld()) {
                partner.announce(Wedding.OnNotifyWeddingPartnerTransfer(id, mapid));
            }
        }
    }

    public void removeIncomingInvites() {
        MapleInviteCoordinator.removePlayerIncomingInvites(id);
    }

    private void changeMapInternal(final MapleMap to, final Point pos, final byte[] warpPacket) {
        if (!canWarpMap) {
            return;
        }

        this.mapTransitioning.set(true);

        //this.unregisterChairBuff();
        this.clearBanishPlayerData();
        MapleTrade.cancelTrade(this, MapleTrade.TradeResult.UNSUCCESSFUL_ANOTHER_MAP);
        this.closePlayerInteractions();

        MapleParty e = null;
        if (this.getParty() != null && this.getParty().getEnemy() != null) {
            e = this.getParty().getEnemy();
        }
        final MapleParty k = e;

        client.announce(warpPacket);
        map.removePlayer(this);
        if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            map = to;
            setPosition(pos);
            map.addPlayer(this);
            visitMap(map);

            prtLock.lock();
            try {
                if (party != null) {
                    mpc.setMapId(to.getId());
                    client.announce(PartyPacket.Packet.onPartySilentUpdate(party, client.getChannel()));
                    //client.announce(MaplePacketCreator.updateParty(client.getChannel(), party, PartyOperation.SILENT_UPDATE, null));
                    updatePartyMemberHPInternal();
                }
            } finally {
                prtLock.unlock();
            }
            if (MapleCharacter.this.getParty() != null) {
                MapleCharacter.this.getParty().setEnemy(k);
            }
            silentPartyUpdate();  // EIM script calls inside

            if (getMap().getHPDec() > 0) {
                resetHpDecreaseTask();
            }
        } else {
            FilePrinter.printError(FilePrinter.MAPLE_MAP, "Character " + this.getName() + " got stuck when moving to map " + map.getId() + ".");
        }

        notifyMapTransferToPartner(map.getId());

        //alas, new map has been specified when a warping was being processed...
        if (newWarpMap != -1) {
            canWarpMap = true;

            int temp = newWarpMap;
            newWarpMap = -1;
            changeMap(temp);
        } else {
            // if this event map has a gate already opened, render it
            EventInstanceManager eim = getEventInstance();
            if (eim != null) {
                eim.recoverOpenedGate(this, map.getId());
            }

            // if this map has obstacle components moving, make it do so for this client
            announce(MaplePacketCreator.environmentMoveList(map.getEnvironment().entrySet()));
        }
    }

    public boolean isChangingMaps() {
        return this.mapTransitioning.get();
    }

    public void setMapTransitionComplete() {
        this.mapTransitioning.set(false);
    }

    public void changePage(int page) {
        this.currentPage = page;
    }

    public void changeSkillLevel(PlayerSkill skill, byte newLevel, int newMasterlevel, long expiration) {
        if (newLevel > -1) {
            skills.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration));
            if (!GameConstants.isHiddenSkills(skill.getId())) {
                this.client.announce(WvsContext.Packet.onChangeSkillRecordResult(skill.getId(), newLevel, newMasterlevel, expiration));
            }
        } else {
            skills.remove(skill);
            this.client.announce(WvsContext.Packet.onChangeSkillRecordResult(skill.getId(), newLevel, newMasterlevel, -1));
            try (Connection con = DatabaseConnection.getConnection()) {
                Statements.Delete.from("skills").where("skillid", skill.getId()).where("characterid", id).execute(con);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void changeTab(int tab) {
        this.currentTab = tab;
    }

    public void changeType(int type) {
        this.currentType = type;
    }

    public void checkBerserk(final boolean isHidden) {
        if (berserkSchedule != null) {
            berserkSchedule.cancel(false);
        }
        final MapleCharacter chr = this;
        if (job.equals(MapleJob.DARKKNIGHT)) {
            PlayerSkill BerserkX = SkillFactory.getSkill(DarkKnight.BERSERK);
            final int skilllevel = getSkillLevel(BerserkX);
            if (skilllevel > 0) {
                berserk = chr.getHp() * 100 / chr.getCurrentMaxHp() < BerserkX.getEffect(skilllevel).getX();
                berserkSchedule = TimerManager.getInstance().register(() -> {
                    if (awayFromWorld.get()) {
                        return;
                    }

                    client.announce(MaplePacketCreator.showOwnBerserk(skilllevel, berserk));
                    if (!isHidden) {
                        getMap().broadcastMessage(MapleCharacter.this, UserRemote.Packet.showBerserk(getId(), skilllevel, berserk), false);
                    } else {
                        getMap().broadcastGMMessage(MapleCharacter.this, UserRemote.Packet.showBerserk(getId(), skilllevel, berserk), false);
                    }
                }, 5000, 3000);
            }
        }
    }

    public void checkMessenger() {
        if (messenger != null && messengerposition < 4 && messengerposition > -1) {
            World worldz = getWorldServer();
            worldz.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(this, messengerposition), messengerposition);
            worldz.updateMessenger(getMessenger().getId(), name, client.getChannel());
        }
    }

    public void controlMonster(MapleMonster monster) {
        if (cpnLock.tryLock()) {
            try {
                controlled.add(monster);
            } finally {
                cpnLock.unlock();
            }
        }
    }

    public void stopControllingMonster(MapleMonster monster) {
        if (cpnLock.tryLock()) {
            try {
                controlled.remove(monster);
            } finally {
                cpnLock.unlock();
            }
        }
    }

    public int getNumControlledMonsters() {
        cpnLock.lock();
        try {
            return controlled.size();
        } finally {
            cpnLock.unlock();
        }
    }

    public Collection<MapleMonster> getControlledMonsters() {
        cpnLock.lock();
        try {
            return new ArrayList<>(controlled);
        } finally {
            cpnLock.unlock();
        }
    }

    public boolean applyConsumeOnPickup(final int itemid) {
        if (itemid / 1000000 == 2) {
            if (ii.isConsumeOnPickup(itemid)) {
                if (ItemConstants.isPartyItem(itemid)) {
                    List<MapleCharacter> pchr = this.getPartyMembersOnSameMap();

                    if (!ItemConstants.isPartyAllcure(itemid)) {
                        MapleStatEffect mse = ii.getItemEffect(itemid);

                        if (!pchr.isEmpty()) {
                            for (MapleCharacter mc : pchr) {
                                mse.applyTo(mc);
                            }
                        } else {
                            mse.applyTo(this);
                        }
                    } else {
                        if (!pchr.isEmpty()) {
                            for (MapleCharacter mc : pchr) {
                                mc.dispelDebuffs();
                            }
                        } else {
                            this.dispelDebuffs();
                        }
                    }
                } else {
                    ii.getItemEffect(itemid).applyTo(this);
                }

                if (itemid / 10000 == 238) {
                    this.getMonsterBook().addCard(client, itemid);
                }
                return true;
            }
        }
        return false;
    }

    public final void pickupItem(MapleMapObject ob) {
        pickupItem(ob, -1);
    }

    public final void pickupItem(MapleMapObject ob, int petIndex) {     // yes, one picks the MapleMapObject, not the MapleMapItem
        if (ob == null) {                                               // pet index refers to the one picking up the item
            return;
        }

        if (ob instanceof MapleMapItem mapitem) {
            boolean pickup_c = mapitem.canBePickedBy(this);
            if (!pickup_c) {
                client.announce(MaplePacketCreator.showItemUnavailable());
                client.announce(WvsContext.Packet.enableActions());
                return;
            }

            if (System.currentTimeMillis() - mapitem.getDropTime() < 400) {
                client.announce(WvsContext.Packet.enableActions());
                return;
            }

            List<MapleCharacter> mpcs = new LinkedList<>();
            if (mapitem.getMeso() > 0 && !mapitem.isPickedUp()) {
                mpcs = getPartyMembersOnSameMap();
            }

            ScriptedItem itemScript = null;
            mapitem.lockItem();
            try {
                if (mapitem.isPickedUp()) {
                    client.announce(MaplePacketCreator.showItemUnavailable());
                    client.announce(WvsContext.Packet.enableActions());
                    return;
                }

                /*final double distance = pos.distanceSq(mapitem.getPosition());
                if(distance > 5000 && (mapitem.getMeso() > 0 || mapitem.getItemId() != 4001025)){
                    chr.getAutobanManager().addPoint(AutobanFactory.ITEM_VAC, "Item Vac " + distance + " distance.");
                }else if(distance > 640000.0){
                    chr.getAutobanManager().addPoint(AutobanFactory.ITEM_VAC, "Item Vac " + distance + " distance.");
                }*/

                boolean isPet = petIndex > -1;
                final byte[] pickupPacket = DropPool.Packet.onDropLeaveField(mapitem.getObjectId(), (isPet) ? 5 : 2, this.getId(), isPet, petIndex);

                Item mItem = mapitem.getItem();
                boolean hasSpaceInventory = true;
                if (mapitem.getItemId() == 4031865 || mapitem.getItemId() == 4031866 || mapitem.getMeso() > 0 || ii.isConsumeOnPickup(mapitem.getItemId()) || (hasSpaceInventory = MapleInventoryManipulator.checkSpace(client, mapitem.getItemId(), mItem.getQuantity(), mItem.getOwner()))) {
                    int mapId = this.getMapId();

                    if ((mapId > 209000000 && mapId < 209000016) || (mapId >= 990000500 && mapId <= 990000502)) {//happyville trees and guild PQ
                        if (!mapitem.isPlayerDrop() || mapitem.getDropper().getObjectId() == client.getPlayer().getObjectId()) {
                            if (mapitem.getMeso() > 0) {
                                if (!mpcs.isEmpty()) {
                                    int mesosamm = mapitem.getMeso() / mpcs.size();
                                    for (MapleCharacter partymem : mpcs) {
                                        if (partymem.isLoggedinWorld()) {
                                            partymem.gainMeso(mesosamm, true, true, false);
                                        }
                                    }
                                } else {
                                    this.gainMeso(mapitem.getMeso(), true, true, false);
                                }

                                this.getMap().pickItemDrop(pickupPacket, mapitem);
                            } else if (mapitem.getItemId() == 4031865 || mapitem.getItemId() == 4031866) {
                                // Add NX to account, show effect and make item disappear
                                int nxGain = mapitem.getItemId() == 4031865 ? 100 : 250;
                                this.getCashShop().gainCash(1, nxGain);

                                showHint("You have earned #e#b" + nxGain + " NX#k#n. (" + this.getCashShop().getCash(1) + " NX)", 300);

                                this.getMap().pickItemDrop(pickupPacket, mapitem);
                            } else if (MapleInventoryManipulator.addFromDrop(client, mItem, true)) {
                                this.getMap().pickItemDrop(pickupPacket, mapitem);
                            } else {
                                client.announce(WvsContext.Packet.enableActions());
                                return;
                            }
                        } else {
                            client.announce(MaplePacketCreator.showItemUnavailable());
                            client.announce(WvsContext.Packet.enableActions());
                            return;
                        }
                        client.announce(WvsContext.Packet.enableActions());
                        return;
                    }

                    if (!this.needQuestItem(mapitem.getQuest(), mapitem.getItemId())) {
                        client.announce(MaplePacketCreator.showItemUnavailable());
                        client.announce(WvsContext.Packet.enableActions());
                        return;
                    }

                    if (mapitem.getMeso() > 0) {
                        if (!mpcs.isEmpty()) {
                            int mesosamm = mapitem.getMeso() / mpcs.size();
                            for (MapleCharacter partymem : mpcs) {
                                if (partymem.isLoggedinWorld()) {
                                    partymem.gainMeso(mesosamm, true, true, false);
                                }
                            }
                        } else {
                            this.gainMeso(mapitem.getMeso(), true, true, false);
                        }
                    } else if (mItem.getItemId() / 10000 == 243) {
                        ScriptedItem info = ii.getScriptedItemInfo(mItem.getItemId());
                        if (info != null && info.runOnPickup()) {
                            itemScript = info;
                        } else {
                            if (!MapleInventoryManipulator.addFromDrop(client, mItem, true)) {
                                client.announce(WvsContext.Packet.enableActions());
                                return;
                            }
                        }
                    } else if (mapitem.getItemId() == 4031865 || mapitem.getItemId() == 4031866) {
                        // Add NX to account, show effect and make item disappear
                        int nxGain = mapitem.getItemId() == 4031865 ? 100 : 250;
                        this.getCashShop().gainCash(1, nxGain);

                        showHint("You have earned #e#b" + nxGain + " NX#k#n. (" + this.getCashShop().getCash(1) + " NX)", 300);
                    } else if (applyConsumeOnPickup(mItem.getItemId())) {
                    } else if (MapleInventoryManipulator.addFromDrop(client, mItem, true)) {
                        if (mItem.getItemId() == 4031868) {
                            updateAriantScore();
                        }
                    } else {
                        client.announce(WvsContext.Packet.enableActions());
                        return;
                    }

                    this.getMap().pickItemDrop(pickupPacket, mapitem);
                } else if (!hasSpaceInventory) {
                    client.announce(WvsContext.Packet.onInventoryOperation(true, Collections.emptyList()));
                    client.announce(MaplePacketCreator.getShowInventoryFull());
                }
            } finally {
                mapitem.unlockItem();
            }

            if (itemScript != null) {
                ItemScriptManager ism = ItemScriptManager.getInstance();
                ism.runItemScript(client, itemScript);
            }
        }
        client.announce(WvsContext.Packet.enableActions());
    }

    public int countItem(int itemid) {
        return inventory[ItemConstants.getInventoryType(itemid).ordinal()].countById(itemid);
    }

    public boolean canHold(int itemid) {
        return canHold(itemid, 1);
    }

    public boolean canHold(int itemid, int quantity) {
        return client.getAbstractPlayerInteraction().canHold(itemid, quantity);
    }

    public boolean canHoldUniques(List<Integer> itemids) {
        for (Integer itemid : itemids) {
            if (ii.isPickupRestricted(itemid) && this.haveItem(itemid)) {
                return false;
            }
        }

        return true;
    }

    public boolean isRidingBattleship() {
        Integer bv = getBuffedValue(MapleBuffStat.MONSTER_RIDING);
        return bv != null && bv.equals(Corsair.BATTLE_SHIP);
    }

    public void announceBattleshipHp() {
        announce(UserLocal.Packet.skillCooldown(5221999, battleshipHp));
    }

    public void decreaseBattleshipHp(int decrease) {
        int trueDecrease  = decrease > 6000 ? 6000 : decrease;

        this.battleshipHp -= trueDecrease;
        if (battleshipHp <= 0) {
            PlayerSkill battleship = SkillFactory.getSkill(Corsair.BATTLE_SHIP);
            int cooldown = battleship.getEffect(getSkillLevel(battleship)).getCooldown();
            announce(UserLocal.Packet.skillCooldown(Corsair.BATTLE_SHIP, cooldown));
            addCooldown(
                    Corsair.BATTLE_SHIP,
                    System.currentTimeMillis(),
                    cooldown,
                    TimerManager.getInstance().schedule(
                            new CancelCooldownAction(this, Corsair.BATTLE_SHIP),
                            cooldown * 1000));
            removeCooldown(5221999);
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        } else {
            announceBattleshipHp();
            addCooldown(5221999, 0, battleshipHp, null);
        }
    }

    public void decreaseReports() {
        this.possibleReports--;
    }

    public void deleteGuild(int guildId) {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5 WHERE guildid = ?")) {
                ps.setInt(1, guildId);
                ps.execute();
            }
            Statements.Delete.from("guilds").where("guildid", guildId).execute(con);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public int updateGuildName(int guildId, String newName) {
        if (newName.length() > 12) {
            System.out.println("guild name > 12 char");
            return 0;
        }
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement checkPs = con.prepareStatement("SELECT guildid FROM guilds WHERE name = ?")) {
                checkPs.setString(1, newName);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next()) {
                    System.out.println("guild name already exists");
                    return 1;
                }
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE guilds SET name = ? WHERE guildid = ?")) {
                ps.setString(1, newName);
                ps.setInt(2, guildId);
                ps.execute();
            }

            Statements.Update("guilds").set("name", newName).where("guildid", guildId).execute(con);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 2;
    }

    private void nextPendingRequest(MapleClient c) {
        CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
        if (pendingBuddyRequest != null) {
            c.announce(FriendPacket.Packet.onFriendResult(FriendResultType.AddFriend.getType(), pendingBuddyRequest.getId(), c.getPlayer().getId(), pendingBuddyRequest.getName()));
        }
    }

    private void notifyRemoteChannel(MapleClient c, int remoteChannel, int otherCid, BuddyList.BuddyOperation operation) {
        MapleCharacter player = c.getPlayer();
        if (remoteChannel != -1) {
            c.getWorldServer().buddyChanged(otherCid, player.getId(), player.getName(), c.getChannel(), operation);
        }
    }

    public void deleteBuddy(int otherCid) {
        BuddyList bl = getBuddylist();

        if (bl.containsVisible(otherCid)) {
            notifyRemoteChannel(client, getWorldServer().find(otherCid), otherCid, BuddyList.BuddyOperation.DELETED);
        }
        bl.remove(otherCid);
        client.announce(FriendPacket.Packet.onFriendResult(FriendResultType.UpdateList.getType(), getBuddylist().getBuddies()));
        nextPendingRequest(client);
    }

    public static boolean deleteCharFromDB(MapleCharacter player, int senderAccId) {
        int cid = player.getId();
        if (!Server.getInstance().haveCharacterEntry(senderAccId, cid)) {    // thanks zera (EpiphanyMS) for pointing a critical exploit with non-authored character deletion request
            return false;
        }

        int accId = senderAccId, world = 0;
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT world FROM characters WHERE id = ?")) {
                ps.setInt(1, cid);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        world = rs.getInt("world");
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("SELECT buddyid FROM buddies WHERE characterid = ?")) {
                ps.setInt(1, cid);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int buddyid = rs.getInt("buddyid");
                        MapleCharacter buddy = Server.getInstance().getWorld(world).getPlayerStorage().getCharacterById(buddyid);

                        if (buddy != null) {
                            buddy.deleteBuddy(cid);
                        }
                    }
                }
            }

            Statements.Delete.from("buddies").where("characterid", cid).execute(con);

            try (PreparedStatement ps = con.prepareStatement("SELECT threadid FROM bbs_threads WHERE postercid = ?")) {
                ps.setInt(1, cid);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Statements.Delete.from("bbs_replies").where("threadid", rs.getInt("threadid")).execute(con);
                    }
                }
            }

            Statements.Delete.from("bbs_threads").where("postercid", cid).execute(con);

            try (PreparedStatement ps = con.prepareStatement("SELECT id, guildid, guildrank, name, allianceRank FROM characters WHERE id = ? AND accountid = ?")) {
                ps.setInt(1, cid);
                ps.setInt(2, accId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("guildid") > 0) {
                        Server.getInstance().deleteGuildCharacter(new MapleGuildCharacter(player, cid, 0, rs.getString("name"), (byte) -1, (byte) -1, 0, rs.getInt("guildrank"), rs.getInt("guildid"), false, rs.getInt("allianceRank")));
                    }
                }
            }

            Statements.Delete.from("wish_lists").where("charid", cid).execute(con);
            Statements.Delete.from("cooldowns").where("charid", cid).execute(con);
            Statements.Delete.from("player_diseases").where("charid", cid).execute(con);
            Statements.Delete.from("area_info").where("charid", cid).execute(con);
            Statements.Delete.from("monster_book").where("charid", cid).execute(con);
            Statements.Delete.from("characters").where("id", cid).execute(con);
            Statements.Delete.from("fame_log").where("characterid_to", cid).execute(con);

            try (PreparedStatement ps = con.prepareStatement("SELECT inventoryitemid, petid FROM inventory_items WHERE characterid = ?")) {
                ps.setInt(1, cid);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long inventoryitemid = rs.getLong("inventoryitemid");

                        try (PreparedStatement ps2 = con.prepareStatement("SELECT ringid FROM inventory_equipment WHERE inventoryitemid = ?")) {
                            ps2.setLong(1, inventoryitemid);

                            try (ResultSet rs2 = ps2.executeQuery()) {
                                while (rs2.next()) {
                                    Statements.Delete.from("rings").where("id", rs2.getInt("ringid")).execute(con);
                                }
                            }
                        }

                        Statements.Delete.from("inventory_equipment").where("inventoryitemid", inventoryitemid).execute(con);
                        Statements.Delete.from("pets").where("petid", rs.getInt("petid")).execute(con);
                    }
                }
            }

            FredrickProcessor.removeFredrickLog(cid);   // thanks maple006 for pointing out the player's Fredrick items are not being deleted at character deletion

            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM mts_cart WHERE cid = ?")) {
                ps.setInt(1, cid);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int mtsid = rs.getInt("id");
                        Statements.Delete.from("mts_items").where("id", mtsid).execute(con);
                    }
                }
            }
            Statements.Delete.from("mts_cart").where("cid", cid).execute(con);

            String[] toDel = {"fame_log", "inventory_items", "keymap", "medal_maps", "quest_status", "quest_progress", "saved_locations", "trock_locations", "skill_macros", "skills", "event_stats", "server_queue"};
            for (String s : toDel) {
                Statements.Delete.from(s).where("characterid", cid).execute(con);
            }

            Server.getInstance().deleteCharacterEntry(accId, cid);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void stopChairTask() {
        chrLock.lock();
        try {
            if (chairRecoveryTask != null) {
                chairRecoveryTask.cancel(false);
                chairRecoveryTask = null;
            }
        } finally {
            chrLock.unlock();
        }
    }

    private static Pair<Integer, Pair<Integer, Integer>> getChairTaskIntervalRate(int maxhp, int maxmp) {
        float toHeal = Math.max(maxhp, maxmp);
        float maxDuration = ServerConstants.CHAIR_EXTRA_HEAL_MAX_DELAY * 1000;

        int rate = 0;
        int minRegen = 1, maxRegen = (256 * ServerConstants.CHAIR_EXTRA_HEAL_MULTIPLIER) - 1, midRegen = 1;
        while (minRegen < maxRegen) {
            midRegen = (int) ((minRegen + maxRegen) * 0.94);

            float procs = toHeal / midRegen;
            float newRate = maxDuration / procs;
            rate = (int) newRate;

            if (newRate < 420) {
                minRegen = (int) (1.2 * midRegen);
            } else if (newRate > 5000) {
                maxRegen = (int) (0.8 * midRegen);
            } else {
                break;
            }
        }

        float procs = maxDuration / rate;
        int hpRegen, mpRegen;
        if (maxhp > maxmp) {
            hpRegen = midRegen;
            mpRegen = (int) Math.ceil(maxmp / procs);
        } else {
            hpRegen = (int) Math.ceil(maxhp / procs);
            mpRegen = midRegen;
        }

        return new Pair<>(rate, new Pair<>(hpRegen, mpRegen));
    }

    private void updateChairHealStats() {
        statRlock.lock();
        try {
            if (localchairrate != -1) {
                return;
            }
        } finally {
            statRlock.unlock();
        }

        effLock.lock();
        statWlock.lock();
        try {
            Pair<Integer, Pair<Integer, Integer>> p = getChairTaskIntervalRate(localmaxhp, localmaxmp);

            localchairrate = p.getLeft();
            localchairhp = p.getRight().getLeft();
            localchairmp = p.getRight().getRight();
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    private void startChairTask() {
        if (chair.get() < 0) {
            return;
        }

        int healInterval;
        effLock.lock();
        try {
            updateChairHealStats();
            healInterval = localchairrate;
        } finally {
            effLock.unlock();
        }

        chrLock.lock();
        try {
            if (chairRecoveryTask != null) {
                stopChairTask();
            }

            chairRecoveryTask = TimerManager.getInstance().register(() -> {
                updateChairHealStats();
                final int healHP = localchairhp;
                final int healMP = localchairmp;

                if (MapleCharacter.this.getHp() < localmaxhp) {
                    byte recHP = (byte) (healHP / ServerConstants.CHAIR_EXTRA_HEAL_MULTIPLIER);

                    client.announce(UserLocal.Packet.onEffect(UserEffectType.RECOVERY.getEffect(), "", recHP));
                    //getMap().broadcastMessage(MapleCharacter.this, UserRemote.Packet.onRemoteUserEffect(id, UserEffectType.RECOVERY.getEffect(), recHP), false);
                } else if (MapleCharacter.this.getMp() >= localmaxmp) {
                    stopChairTask();    // optimizing schedule management when player is already with full pool.
                }

                addMPHP(healHP, healMP);
            }, healInterval, healInterval);
        } finally {
            chrLock.unlock();
        }
    }

    private void stopExtraTask() {
        chrLock.lock();
        try {
            if (extraRecoveryTask != null) {
                extraRecoveryTask.cancel(false);
                extraRecoveryTask = null;
            }
        } finally {
            chrLock.unlock();
        }
    }

/*    private void startExtraTask(final byte healHP, final byte healMP, final short healInterval) {
        chrLock.lock();
        try {
            startExtraTaskInternal(healHP, healMP, healInterval);
        } finally {
            chrLock.unlock();
        }
    }*/

 /*   private void startExtraTaskInternal(final byte healHP, final byte healMP, final short healInterval) {
        extraRecInterval = healInterval;

        extraRecoveryTask = TimerManager.getInstance().register(() -> {
            if (getBuffSource(MapleBuffStat.HPREC) == -1 && getBuffSource(MapleBuffStat.MPREC) == -1) {
                stopExtraTask();
                return;
            }

            if (MapleCharacter.this.getHp() < localmaxhp) {
                if (healHP > 0) {
                    client.announce(MaplePacketCreator.showOwnRecovery(healHP));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showRecovery(id, healHP), false);
                }
            }

            addMPHP(healHP, healMP);
        }, healInterval, healInterval);
    }*/

    public void disbandGuild() {
        if (guildid < 1 || guildRank != 1) {
            return;
        }
        try {
            Server.getInstance().disbandGuild(guildid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dispel() {
        for (MapleBuffStatValueHolder mbsvh : new ArrayList<>(effects.values())) {
            if (mbsvh.effect.isSkill()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public final boolean hasDisease(final MapleBuffStat dis) {
        for (final MapleBuffStat disease : effects.keySet()) {
            if (disease == dis) {
                return true;
            }
        }
        return false;
    }

    public Map<MapleDisease, Pair<Long, MobSkill>> getAllDiseases() {
        chrLock.lock();
        try {
            long curtime = Server.getInstance().getCurrentTime();
            Map<MapleDisease, Pair<Long, MobSkill>> ret = new LinkedHashMap<>();

            for (Entry<MapleDisease, Long> de : diseaseExpires.entrySet()) {
                Pair<MapleDiseaseValueHolder, MobSkill> dee = diseases.get(de.getKey());
                MapleDiseaseValueHolder mdvh = dee.getLeft();

                ret.put(de.getKey(), new Pair<>(mdvh.length - (curtime - mdvh.startTime), dee.getRight()));
            }

            return ret;
        } finally {
            chrLock.unlock();
        }
    }

    public void dispelDebuffs() {
        List<MapleBuffStat> toDispel = new ArrayList<>();
        toDispel.add(MapleBuffStat.CURSE);
        toDispel.add(MapleBuffStat.DARKNESS);
        toDispel.add(MapleBuffStat.POISON);
        toDispel.add(MapleBuffStat.SEAL);
        toDispel.add(MapleBuffStat.WEAKEN);
        this.cancelPlayerBuffs(toDispel);
    }

    public void cancelAllDebuffs() {
        List<MapleBuffStat> toDispel = new ArrayList<>();
        toDispel.add(MapleBuffStat.CURSE);
        toDispel.add(MapleBuffStat.DARKNESS);
        toDispel.add(MapleBuffStat.POISON);
        toDispel.add(MapleBuffStat.SEAL);
        toDispel.add(MapleBuffStat.WEAKEN);
        toDispel.add(MapleBuffStat.SEDUCE);
        toDispel.add(MapleBuffStat.CONFUSE);
        this.cancelPlayerBuffs(toDispel);
    }



    public void dispelSkill(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (skillid == 0) {
                if (mbsvh.effect.isSkill()
                        && (mbsvh.effect.getSourceId() % 10000000 == 1004
                        || dispelSkills(mbsvh.effect.getSourceId()))) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            } else if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }


    protected boolean dispelSkills(int skillid) {
        return switch (skillid) {
            case DarkKnight.BEHOLDER, FPArchMage.ELQUINES, ILArchMage.IFRIT, Priest.SUMMON_DRAGON,
                    Bishop.BAHAMUT, Ranger.PUPPET, Ranger.SILVER_HAWK, Sniper.PUPPET,
                    Sniper.GOLDEN_EAGLE, Hermit.SHADOW_PARTNER -> true;
            default -> false;
        };
    }
    public void changeFaceExpression(int emote) {
        long timeNow = Server.getInstance().getCurrentTime();
        if (timeNow - lastExpression > 2000) {
            lastExpression = timeNow;
            client.getChannelServer().registerFaceExpression(map, this, emote);
        }
    }

    private void doHurtHp() {
        if (!(this.getInventory(MapleInventoryType.EQUIPPED).findById(getMap().getHPDecProtect()) != null /*|| buffMapProtection()*/)) {
            addHP(-getMap().getHPDec());
            lastHpDec = Server.getInstance().getCurrentTime();
        }
    }

    private void startHpDecreaseTask(long lastHpTask) {
        hpDecreaseTask = TimerManager.getInstance().register(() -> doHurtHp(), ServerConstants.MAP_DAMAGE_OVERTIME_INTERVAL, ServerConstants.MAP_DAMAGE_OVERTIME_INTERVAL - lastHpTask);
    }

    public void resetHpDecreaseTask() {
        if (hpDecreaseTask != null) {
            hpDecreaseTask.cancel(false);
        }

        long lastHpTask = Server.getInstance().getCurrentTime() - lastHpDec;
        startHpDecreaseTask((lastHpTask > ServerConstants.MAP_DAMAGE_OVERTIME_INTERVAL) ? ServerConstants.MAP_DAMAGE_OVERTIME_INTERVAL : lastHpTask);
    }

    public void dropMessage(String message) {
        dropMessage(0, message);
    }

    public void dropMessage(int type, String message) {
        client.announce(MaplePacketCreator.serverNotice(type, message));
    }

    public void enteredScript(String script, int mapid) {
        if (!entered.containsKey(mapid)) {
            entered.put(mapid, script);
        }
    }

    public void equipChanged() {
        getMap().broadcastUpdateCharLookMessage(this, this);
        equipchanged = true;
        updateLocalStats();
        if (getMessenger() != null) {
            getWorldServer().updateMessenger(getMessenger(), getName(), getWorld(), client.getChannel());
        }
    }

    public void cancelDiseaseExpireTask() {
        if (diseaseExpireTask != null) {
            diseaseExpireTask.cancel(false);
            diseaseExpireTask = null;
        }
    }

    public void diseaseExpireTask() {
        if (diseaseExpireTask == null) {
            diseaseExpireTask = TimerManager.getInstance().register(() -> {
                Set<MapleDisease> toExpire = new LinkedHashSet<>();

                chrLock.lock();
                try {
                    long curTime = Server.getInstance().getCurrentTime();

                    for (Entry<MapleDisease, Long> de : diseaseExpires.entrySet()) {
                        if (de.getValue() < curTime) {
                            toExpire.add(de.getKey());
                        }
                    }
                } finally {
                    chrLock.unlock();
                }

               /* for (MapleDisease d : toExpire) {
                    dispelDebuff(d);
                }*/
            }, 1500);
        }
    }

    public void cancelBuffExpireTask() {
        if (buffExpireTask != null) {
            buffExpireTask.cancel(false);
            buffExpireTask = null;
        }
    }

    public void buffExpireTask() {
        if (buffExpireTask == null) {
            buffExpireTask = TimerManager.getInstance().register(() -> {
                Set<Entry<Integer, Long>> es;
                List<MapleBuffStatValueHolder> toCancel = new ArrayList<>();

                effLock.lock();
                chrLock.lock();
                try {
                    es = new LinkedHashSet<>(buffExpires.entrySet());

                    long curTime = Server.getInstance().getCurrentTime();
                    for (Entry<Integer, Long> bel : es) {
                        if (curTime >= bel.getValue()) {
                            //toCancel.add(buffEffects.get(bel.getKey()).entrySet().iterator().next().getValue());    //rofl
                        }
                    }
                } finally {
                    chrLock.unlock();
                    effLock.unlock();
                }

                for (MapleBuffStatValueHolder mbsvh : toCancel) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            }, 1500);
        }
    }

    public void cancelSkillCooldownTask() {
        if (skillCooldownTask != null) {
            skillCooldownTask.cancel(false);
            skillCooldownTask = null;
        }
    }

    public void skillCooldownTask() {
        if (skillCooldownTask == null) {
            skillCooldownTask = TimerManager.getInstance().register(() -> {
                Set<Entry<Integer, MapleCoolDownValueHolder>> es;

                effLock.lock();
                chrLock.lock();
                try {
                    es = new LinkedHashSet<>(coolDowns.entrySet());
                } finally {
                    chrLock.unlock();
                    effLock.unlock();
                }

                long curTime = Server.getInstance().getCurrentTime();
                for (Entry<Integer, MapleCoolDownValueHolder> bel : es) {
                    MapleCoolDownValueHolder mcdvh = bel.getValue();
                    if (curTime >= mcdvh.startTime + mcdvh.length) {
                        removeCooldown(mcdvh.skillId);
                        client.announce(UserLocal.Packet.skillCooldown(mcdvh.skillId, 0));
                    }
                }
            }, 1500);
        }
    }

    public void cancelExpirationTask() {
        if (itemExpireTask != null) {
            itemExpireTask.cancel(false);
            itemExpireTask = null;
        }
    }

    public void expirationTask() {
        if (itemExpireTask == null) {
            itemExpireTask = TimerManager.getInstance().register(() -> {
                boolean deletedCoupon = false;

                long expiration, currenttime = System.currentTimeMillis();
                Set<PlayerSkill> keys = getSkills().keySet();
                for (PlayerSkill key : keys) {
                    SkillEntry skill = getSkills().get(key);
                    if (skill.expiration != -1 && skill.expiration < currenttime) {
                        changeSkillLevel(key, (byte) -1, 0, -1);
                    }
                }

                List<Item> toberemove = new ArrayList<>();
                for (MapleInventory inv : inventory) {
                    for (Item item : inv.list()) {
                        expiration = item.getExpiration();

                        if (expiration != -1 && (expiration < currenttime) && ((item.getFlag() & ItemConstants.LOCK) == ItemConstants.LOCK)) {
                            int aids = item.getFlag();
                            aids &= ~(ItemConstants.LOCK);
                            item.setFlag(aids); //Probably need a check, else people can make expiring items into permanent items...
                            item.setExpiration(-1);
                            forceUpdateItem(item);   //TEST :3
                        } else if (expiration != -1 && expiration < currenttime) {
                            /*if (!ItemConstants.isPet(item.getItemId())) {*/
                            if (item.getPet() == null) {
                                client.announce(WvsContext.Packet.onMessage(WvsMessageType.Expiration.getType(), item.getItemId()));
                                unequipPet(item.getPet(), false);
                                toberemove.add(item);
                                if (ItemConstants.isRateCoupon(item.getItemId())) {
                                    deletedCoupon = true;
                                }
                            } else {
                                item.setExpiration(-1);
                                unequipPet(item.getPet(), false);
                                forceUpdateItem(item);
                            }
                        }
                    }

                    if (!toberemove.isEmpty()) {
                        for (Item item : toberemove) {
                            MapleInventoryManipulator.removeFromSlot(client, inv.getType(), item.getPosition(), item.getQuantity(), true);
                        }

                        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                        for (Item item : toberemove) {
                            List<Integer> toadd = new ArrayList<>();
                            Pair<Integer, String> replace = ii.getReplaceOnExpire(item.getItemId());
                            if (replace.left > 0) {
                                toadd.add(replace.left);
                                if (!replace.right.isEmpty()) {
                                    dropMessage(replace.right);
                                }
                            }
                            for (Integer itemid : toadd) {
                                MapleInventoryManipulator.addById(client, itemid, (short) 1);
                            }
                        }

                        toberemove.clear();
                    }

                    if (deletedCoupon) {
                        updateCouponRates();
                    }
                }
            }, 60000);
        }
    }

    public void forceUpdateItem(Item item) {
        final List<InventoryOperation> mods = new LinkedList<>();
        mods.add(new InventoryOperation(3, item));
        mods.add(new InventoryOperation(0, item));
        client.announce(WvsContext.Packet.onInventoryOperation(true, mods));
    }
    
    public void updateExpOnItem(Item item) {
        final List<InventoryOperation> mods = new LinkedList<>();
        mods.add(new InventoryOperation(4, item));
        client.announce(WvsContext.Packet.onInventoryOperation(true, mods));
    }

    public void gainGachaExp() {
        int expgain = 0;
        long currentgexp = gachaexp.get();
        if ((currentgexp + exp.get()) >= ExpTable.INSTANCE.getExpNeededForLevel(level)) {
            expgain += ExpTable.INSTANCE.getExpNeededForLevel(level) - exp.get();

            int nextneed = ExpTable.INSTANCE.getExpNeededForLevel(level + 1);
            if (currentgexp - expgain >= nextneed) {
                expgain += nextneed;
            }

            this.gachaexp.set((int) (currentgexp - expgain));
        } else {
            expgain = this.gachaexp.getAndSet(0);
        }
        gainExp(expgain, false, true);
        updateSingleStat(MapleStat.GACHAEXP, this.gachaexp.get());
    }

    public void addGachaExp(int gain) {
        updateSingleStat(MapleStat.GACHAEXP, gachaexp.addAndGet(gain));
    }

    public void gainExp(int gain) {
        gainExp(gain, true, true);
    }

    public void gainExp(int gain, boolean show, boolean inChat) {
        gainExp(gain, show, inChat, true);
    }

    public void gainExp(int gain, boolean show, boolean inChat, boolean white) {
        gainExp(gain, 0, show, inChat, white);
    }

    public void gainExp(int gain, int party, boolean show, boolean inChat, boolean white) {
        if (this.getBuffedValue(MapleBuffStat.CURSE) != null && gain > 0) {
            gain *= 0.5;
            party *= 0.5;
        }
		
        int equip = (gain / 10) * pendantExp;
        int total = gain + equip + party;

        if (level < getMaxLevel()) {
            if ((long) this.exp.get() + (long) total > (long) Integer.MAX_VALUE) {
                int gainFirst = ExpTable.INSTANCE.getExpNeededForLevel(level) - this.exp.get();
                total -= gainFirst + 1;
                this.gainExp(gainFirst + 1, party, false, inChat, white);
            }
            updateSingleStat(MapleStat.EXP, this.exp.addAndGet(total));
            if (show && gain != 0) {
                client.announce(MaplePacketCreator.getShowExpGain(gain, equip, party, inChat, white));
            }
            if (exp.get() >= ExpTable.INSTANCE.getExpNeededForLevel(level)) {
                levelUp(true);
                int need = ExpTable.INSTANCE.getExpNeededForLevel(level);
                if (exp.get() >= need) {
                    setExp(need - 1);
                    updateSingleStat(MapleStat.EXP, need);
                }
            }
        }
    }

    private void announceExpGain(long gain, int equip, int party, boolean inChat, boolean white) {
        gain = Math.min(gain, Integer.MAX_VALUE);
        if (gain == 0) {
            if (party == 0) {
                return;
            }

            gain = party;
            party = 0;
            white = false;
        }

        client.announce(MaplePacketCreator.getShowExpGain((int) gain, equip, party, inChat, white));
    }

    private Pair<Integer, Integer> applyFame(int delta) {
        petLock.lock();
        try {
            int newFame = fame + delta;
            if (newFame < -30000) {
                delta = -(30000 + fame);
            } else if (newFame > 30000) {
                delta = 30000 - fame;
            }

            fame += delta;
            return new Pair<>(fame, delta);
        } finally {
            petLock.unlock();
        }
    }

    public void gainFame(int delta) {
        gainFame(delta, null, 0);
    }

    public boolean gainFame(int delta, MapleCharacter fromPlayer, int mode) {
        Pair<Integer, Integer> fameRes = applyFame(delta);
        delta = fameRes.getRight();
        if (delta != 0) {
            int thisFame = fameRes.getLeft();
            updateSingleStat(MapleStat.FAME, thisFame);

            if (fromPlayer != null) {
                fromPlayer.announce(WvsContext.Packet.onGivePopularityResult(PopularityResponseType.GiveSuccess.getValue(), getName(), mode, thisFame));
                announce(WvsContext.Packet.onGivePopularityResult(PopularityResponseType.ReceiveSuccess.getValue(), fromPlayer.getName(), mode));
            } else {
                announce(WvsContext.Packet.onMessage(WvsMessageType.Popularity.getType(), delta));
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean canHoldMeso(int gain) {  // thanks lucasziron found pointing out a need to check space availability for mesos on player transactions
        long nextMeso = (long) meso.get() + gain;
        return nextMeso <= Integer.MAX_VALUE;
    }

    public void gainMeso(int gain) {
        gainMeso(gain, true, false, true);
    }

    public void gainMeso(int gain, boolean show) {
        gainMeso(gain, show, false, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions, boolean inChat) {
        long nextMeso;
        petLock.lock();
        try {
            nextMeso = (long) meso.get() + gain;  // thanks Thora for pointing integer overflow here
            if (nextMeso > Integer.MAX_VALUE) {
                gain -= (nextMeso - Integer.MAX_VALUE);
            } else if (nextMeso < 0) {
                gain = -meso.get();
            }
            nextMeso = meso.addAndGet(gain);
        } finally {
            petLock.unlock();
        }

        if (gain != 0) {
            updateSingleStat(MapleStat.MESO, (int) nextMeso, enableActions);
            if (show) {
                client.announce(MaplePacketCreator.getShowMesoGain(gain, inChat));
            }
        } else {
            client.announce(WvsContext.Packet.enableActions());
        }
    }

    public void genericGuildMessage(int code) {
        this.client.announce(GuildPacket.Packet.onGuildMessage((byte) code));
    }

    public int getAccountID() {
        return accountid;
    }

    public List<PlayerCoolDownValueHolder> getAllCooldowns() {
        List<PlayerCoolDownValueHolder> ret = new ArrayList<>();
        for (MapleCoolDownValueHolder mcdvh : coolDowns.values()) {
            ret.add(new PlayerCoolDownValueHolder(
                    mcdvh.skillId, mcdvh.startTime, mcdvh.length));
        }
        return ret;
    }

    public int getAllianceRank() {
        return allianceRank;
    }

    public static String getAriantRoomLeaderName(int room) {
        return ariantroomleader[room];
    }

    public static int getAriantSlotsRoom(int room) {
        return ariantroomslot[room];
    }

    public void updateAriantScore() {
        updateAriantScore(0);
    }

    public void updateAriantScore(int dropQty) {
        AriantColiseum arena = this.getAriantColiseum();
        if (arena != null) {
            arena.updateAriantScore(this, countItem(4031868));

            if (dropQty > 0) {
                arena.addLostShards(dropQty);
            }
        }
    }

    public int getBattleshipHp() {
        return battleshipHp;
    }

    public BuddyList getBuddylist() {
        return buddylist;
    }

    public static Map<String, String> getCharacterFromDatabase(String name) {
        Map<String, String> character = new LinkedHashMap<>();
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT id, accountid, name FROM characters WHERE name = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }

                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        character.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));
                    }
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return character;
    }

    public Long getBuffedStarttime(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        return mbsvh == null ? null : mbsvh.startTime;
    }

    public Integer getBuffedValue(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        return mbsvh == null ? null : mbsvh.value.getValue();
    }

    public int getBuffSource(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        return mbsvh == null ? -1 : mbsvh.effect.getSourceId();
    }

    public int getBuffSourceLevel(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        return mbsvh == null ? -1 : mbsvh.effect.getSourceLevel();
    }

    public MapleStatEffect getBuffEffect(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        return mbsvh == null ? null : mbsvh.effect;
    }

    public MapleStatEffect getBuffEffect(int sourceID) {
        for(MapleBuffStatValueHolder mbsvh : effects.values()) {
            if(mbsvh.effect.getSourceId() == sourceID)
                return mbsvh.effect;
        }
        return null;
    }

    protected List<MapleBuffStat> getBuffStats(MapleStatEffect effect, long startTime) {
        List<MapleBuffStat> stats = new ArrayList<>();
        for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : effects.entrySet()) {
            if (stateffect.getValue().effect.sameSource(effect)
                    && (startTime == -1
                    || startTime == stateffect.getValue().startTime)) {
                stats.add(stateffect.getKey());
            }
        }
        return stats;
    }

    public List<Pair<MapleBuffStat, BuffValueHolder>> getAllStatups() {
        List<Pair<MapleBuffStat, BuffValueHolder>> ret = new ArrayList<>();
        for (MapleBuffStat mbs : effects.keySet()) {
            MapleBuffStatValueHolder mbsvh = effects.get(mbs);
            ret.add(new Pair<>(mbs, mbsvh.value));
        }
        return ret;
    }

    public List<PlayerBuffValueHolder> getAllBuffs() {
        List<PlayerBuffValueHolder> ret = new ArrayList<>();
        for (MapleBuffStatValueHolder mbsvh : effects.values()) {
            ret.add(new PlayerBuffValueHolder(mbsvh.startTime, mbsvh.effect));
        }
        return ret;
    }

    public void cancelAllBuffs(boolean disconnect) {
        if (disconnect) {
            effects.clear();
        } else {
            for (MapleBuffStatValueHolder mbsvh : new ArrayList<>(effects.values())) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    protected void deregisterBuffStats(List<MapleBuffStat> stats) {
        synchronized (stats) {
            List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<>(stats.size());
            for (MapleBuffStat stat : stats) {
                MapleBuffStatValueHolder mbsvh = effects.get(stat);
                if (mbsvh != null) {
                    effects.remove(stat);
                    boolean addMbsvh = true;
                    for (MapleBuffStatValueHolder contained : effectsToCancel) {
                        if (mbsvh.startTime == contained.startTime
                                && contained.effect == mbsvh.effect) {
                            addMbsvh = false;
                        }
                    }
                    if (addMbsvh) {
                        effectsToCancel.add(mbsvh);
                    }
                    if (null != stat) {
                        switch (stat) {
                            case RECOVERY:
                                if (recoveryTask != null) {
                                    recoveryTask.cancel(false);
                                    recoveryTask = null;
                                }
                                break;
                            case SUMMON:
                            case PUPPET:
                                int summonId = mbsvh.effect.getSourceId();
                                MapleSummon summon = summons.get(summonId);
                                if (summon != null) {
                                    getMap().broadcastMessage(
                                            SummonedPool.Packet.onSummonRemoved(summon, true),
                                            summon.getPosition());
                                    getMap().removeMapObject(summon);
                                    removeVisibleMapObject(summon);
                                    summons.remove(summonId);
                                }
                                if (summon.getSkill() == DarkKnight.BEHOLDER) {
                                    if (beholderHealingSchedule != null) {
                                        beholderHealingSchedule.cancel(false);
                                        beholderHealingSchedule = null;
                                    }
                                    if (beholderBuffSchedule != null) {
                                        beholderBuffSchedule.cancel(false);
                                        beholderBuffSchedule = null;
                                    }
                                }
                                break;
                            case DRAGONBLOOD:
                                dragonBloodSchedule.cancel(false);
                                dragonBloodSchedule = null;
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            for (MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel) {
                if (getBuffStats(cancelEffectCancelTasks.effect,
                        cancelEffectCancelTasks.startTime).isEmpty()) {
                    if (cancelEffectCancelTasks.schedule != null) {
                        cancelEffectCancelTasks.schedule.cancel(false);
                    }
                }
            }
        }
    }

    public static class CancelCooldownAction implements Runnable {

        protected int skillId;
        protected WeakReference<MapleCharacter> target;

        public CancelCooldownAction(MapleCharacter target, int skillId) {
            this.target = new WeakReference<>(target);
            this.skillId = skillId;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null) {
                realTarget.removeCooldown(skillId);
                realTarget.client.announce(UserLocal.Packet.skillCooldown(skillId, 0));
            }
        }
    }

    public void cancelEffect(int itemId) {
        cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(itemId), false, -1);
    }

    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime) {
        List<MapleBuffStat> buffstats;
        if (!overwrite) {
            buffstats = getBuffStats(effect, startTime);
        } else {
            List<Pair<MapleBuffStat, BuffValueHolder>> statups = effect.getStatups();
            buffstats = new ArrayList<>(statups.size());
            for (Pair<MapleBuffStat, BuffValueHolder> statup : statups) {
                buffstats.add(statup.getLeft());
            }
        }
        deregisterBuffStats(buffstats);
        if (effect.isMagicDoor()) {
            if (door != null) {
                door.getTarget().removeMapObject(door.getTargetOid());
                door.getTown().removeMapObject(door.getTargetOid());

                for (MapleCharacter chr : door.getTarget().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleCharacter chr : door.getTown().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }

                door.destroyDoor(); // should send packet to die to owner

                door = null; // lmao explicit nulling is best nulling

                if (party != null) {
                    getMPC().updateDoor(null);

                    // proper way, but silentPartyUpdate works just as well
//                    getClient().getWorldServer().updateParty(party.getId(), PartyOperation.MYSTIC_DOOR, getMPC());
                    silentPartyUpdate();
                }
            }
        }
        if (!overwrite && (effect.getSourceId() == Spearman.HYPER_BODY
                || effect.getSourceId() == GM.HYPER_BODY
                || effect.getSourceId() == SuperGM.HYPER_BODY
                || effect.isMaxHpIncrease() || effect.isMaxMpIncrease())) {
            List<Pair<MapleStat, Integer>> statup = new ArrayList<>(4);
            statup.add(new Pair<>(MapleStat.HP, Math.min(hp, maxhp)));
            statup.add(new Pair<>(MapleStat.MP, Math.min(mp, maxmp)));
            statup.add(new Pair<>(MapleStat.MAXHP, maxhp));
            statup.add(new Pair<>(MapleStat.MAXMP, maxmp));
            client.announce(WvsContext.Packet.updatePlayerStats(statup, true, this));
        }
        if (effect.isMonsterRiding()) {
            this.getMount().cancelSchedule();
            if (effect.getSourceId() != Corsair.BATTLE_SHIP) {
                this.getMount().setActive(false);
            } else {
                this.mount(0, 0); // TODO mount check if this should be null instead
            }
        }
        if (!overwrite) {
            cancelPlayerBuffs(buffstats);
        }
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat) {
        MapleBuffStatValueHolder effect = effects.get(stat);
        if (effect != null) {
            cancelEffect(effect.effect, false, -1);
        }
    }


    public void cancelBuffStats(MapleBuffStat stat) {
        List<MapleBuffStat> buffStatList = Arrays.asList(stat);
        deregisterBuffStats(buffStatList);
        cancelPlayerBuffs(buffStatList);
    }

    public void cancelBuffEffects() {
        for (MapleBuffStatValueHolder mbsvh : effects.values()) {
            if (mbsvh != null) {
                ScheduledFuture<?> schedule = mbsvh.schedule;
                if (schedule != null) {
                    schedule.cancel(false);
                }
            }
        }
        this.effects.clear();
    }

    public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        return mbsvh == null ? null : mbsvh.effect;
    }

    public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule,  List<Pair<MapleBuffStat, BuffValueHolder>> statups) {
        if (effect.isDragonBlood()) {
            prepareDragonBlood(effect);
        } else if (effect.isBerserk()) {
            checkBerserk(true);
        } else if (effect.isBeholder()) {
            if (beholderHealingSchedule != null) {
                beholderHealingSchedule.cancel(false);
            }
            if (beholderBuffSchedule != null) {
                beholderBuffSchedule.cancel(false);
            }
            PlayerSkill bHealing = SkillFactory.getSkill(DarkKnight.AURA_OF_BEHOLDER);
            int bHealingLvl = getSkillLevel(bHealing);
            if (bHealingLvl > 0) {
                final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
                int healInterval = healEffect.getX() * 1000;
                beholderHealingSchedule = TimerManager.getInstance().register(() -> {
                    if (this.isAlive()) {
                        boolean addHp = true;
                        if (this.getSkillLevel(DarkKnight.BERSERK) > 0) {
                            PlayerSkill beserk = SkillFactory.getSkill(DarkKnight.BERSERK);
                            MapleStatEffect besEffect = beserk.getEffect(this.getSkillLevel(beserk));
                            double percent = ((double) this.getHp() + healEffect.getHp()) / this.getCurrentMaxHp() * 100;
                            if (percent > besEffect.getX()) {
                                addHp = false;
                            }
                        }

                        if (addHp) {
                            addHP(healEffect.getHp());
                            client.announce(MaplePacketCreator.showOwnBuffEffect(DarkKnight.BEHOLDER, 2));
                            getMap().broadcastMessage(MapleCharacter.this, SummonedPool.Packet.onSkill(getId(), DarkKnight.BEHOLDER, 5), true);
                            getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showOwnBuffEffect(DarkKnight.BEHOLDER, 2), false);
                            for (MapleSummon sum : getSummonsValues()) {
                                if (sum != null) {
                                    getMap().broadcastMessage(this, SummonedPool.Packet.onSkill(getId(), sum.getObjectId(), 5), true);
                                }
                            }
                        }
                    }
                }, healInterval, healInterval);
            }
            EffectOperation operation = new EffectOperation(this);
            PlayerSkill bBuff = SkillFactory.getSkill(DarkKnight.HEX_OF_BEHOLDER);
            if (getSkillLevel(bBuff) > 0) {
                final MapleStatEffect buffEffect = bBuff.getEffect(getSkillLevel(bBuff));
                int buffInterval = buffEffect.getX() * 1000;
                beholderBuffSchedule = TimerManager.getInstance().register(() -> {
                    if (getBuffedValue(MapleBuffStat.WATK) == null || buffEffect.getWatk() >= getBuffedValue(MapleBuffStat.WATK)) {
                        buffEffect.applyTo(this);
                        client.announce(MaplePacketCreator.showOwnBuffEffect(DarkKnight.BEHOLDER, 2));
                        for (MapleSummon sum : getSummonsValues()) {
                            if (sum != null) {
                                getMap().broadcastMessage(this, SummonedPool.Packet.onSkill(getId(), sum.getObjectId(), (int) (Math.random() * 3) + 6), true);
                            }
                        }
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showOwnBuffEffect(DarkKnight.BEHOLDER, 2), false);
                    }
                }, buffInterval, buffInterval);
            }
        } else if (effect.isRecovery()) {
            final byte heal = (byte) effect.getX();
            EffectOperation operation = new EffectOperation(this);
            recoveryTask = TimerManager.getInstance().register(() -> {
                if (isAlive()) {
                    addHP(heal);
                    operation.setHp(heal);
                    client.announce(UserLocal.Packet.onEffect(UserEffectType.RECOVERY.getEffect(), "", heal));
                    getMap().broadcastMessage(MapleCharacter.this,
                            UserRemote.Packet.onRemoteUserEffect(id, UserEffectType.RECOVERY.getEffect(), heal), false);

                }
            }, 5000, 5000);
        }
        //effect.morphId = effect.getMorph(this); <- this is basically what we need
        for (Pair<MapleBuffStat, BuffValueHolder> statup : statups) {
            MapleBuffStatValueHolder newBuff = new MapleBuffStatValueHolder(
                    effect, starttime, schedule, new BuffValueHolder(statup.getRight().getSourceID(), statup.getRight().getSourceLevel(), statup.getRight().getValue()));
            if (statup.getLeft().equals(MapleBuffStat.MORPH)) {
                //Cuz it doesn't get this from the applyTo effects, it get's it from the preloaded. rip buffs in odin
                int morphId = newBuff.value.getValue();
                if ( morphId > 999 && morphId != 1002) {
                    morphId = morphId + 100 * this.gender;
                }
                newBuff.value.setValue(morphId);
            }
            effects.put(statup.getLeft(), newBuff);
        }
//        dropMessage("recalcLocalStats() in registerEffect()");
        recalcLocalStats();
    }

    private static int getJobMapChair(MapleJob job) {
        return switch (job.getId() / 1000) {
            case 0 -> Beginner.MAP_CHAIR;
            case 1 -> Noblesse.MAP_CHAIR;
            default -> Legend.MAP_CHAIR;
        };
    }

  /*  public boolean unregisterChairBuff() {
        if (!ServerConstants.USE_CHAIR_EXTRAHEAL) {
            return false;
        }

        int skillId = getJobMapChair(job);
        int skillLv = getSkillLevel(skillId);
        if (skillLv > 0) {
            MapleStatEffect mapChairSkill = SkillFactory.getSkill(skillId).getEffect(skillLv);
            return cancelEffect(mapChairSkill, false, -1);
        }

        return false;
    }

    public boolean registerChairBuff() {
        if (!ServerConstants.USE_CHAIR_EXTRAHEAL) {
            return false;
        }

        int skillId = getJobMapChair(job);
        int skillLv = getSkillLevel(skillId);
        if (skillLv > 0) {
            MapleStatEffect mapChairSkill = SkillFactory.getSkill(skillId).getEffect(skillLv);
            mapChairSkill.applyTo(this);
            return true;
        }

        return false;
    }*/

    public int getChair() {
        return chair.get();
    }

    public String getChalkboard() {
        return this.chalktext;
    }

    public MapleClient getClient() {
        return client;
    }

    public AbstractPlayerInteraction getAbstractPlayerInteraction() {
        return client.getAbstractPlayerInteraction();
    }

    public final List<MapleQuestStatus> getCompletedQuests() {
        synchronized (quests) {
            List<MapleQuestStatus> ret = new LinkedList<>();
            for (MapleQuestStatus q : quests.values()) {
                if (q.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
                    ret.add(q);
                }
            }

            return Collections.unmodifiableList(ret);
        }
    }

    public List<MapleRing> getCrushRings() {
        Collections.sort(crushRings);
        return crushRings;
    }

    public int getCurrentCI() {
        return ci;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public int getCurrentType() {
        return currentType;
    }

    public int getDojoEnergy() {
        return dojoEnergy;
    }

    public boolean getDojoParty() {
        return mapid >= 925030100 && mapid < 925040000;
    }

    public int getDojoPoints() {
        return dojoPoints;
    }

    public int getDojoStage() {
        return dojoStage;
    }

    public int getEnergyBar() {
        return energybar;
    }

    public MapleDoor getDoor() {
        return door;
    }

    public void setBossPoints(int points) {
        bossPoints = points;
    }

    public int getBossPoints() {
        return bossPoints;
    }

    public int getBossQuestRepeats() {
        return bossRepeats;
    }

    public void setBossQuestRepeats(int repeats) {
        bossRepeats = repeats;
    }

    public EventInstanceManager getEventInstance() {
        evtLock.lock();
        try {
            return eventInstance;
        } finally {
            evtLock.unlock();
        }
    }

    public MapleMarriage getMarriageInstance() {
        EventInstanceManager eim = getEventInstance();

        if (eim != null || !(eim instanceof MapleMarriage)) {
            return (MapleMarriage) eim;
        } else {
            return null;
        }
    }

    public void resetExcluded(int petId) {
        chrLock.lock();
        try {
            Set<Integer> petExclude = excluded.get(petId);

            if (petExclude != null) {
                petExclude.clear();
            } else {
                excluded.put(petId, new LinkedHashSet<Integer>());
            }
        } finally {
            chrLock.unlock();
        }
    }

    public void addExcluded(int petId, int x) {
        chrLock.lock();
        try {
            excluded.get(petId).add(x);
        } finally {
            chrLock.unlock();
        }
    }

    public void commitExcludedItems() {
        Map<Integer, Set<Integer>> petExcluded = this.getExcluded();

        chrLock.lock();
        try {
            excludedItems.clear();
        } finally {
            chrLock.unlock();
        }

        for (Map.Entry<Integer, Set<Integer>> pe : petExcluded.entrySet()) {
            byte petIndex = this.getPetIndex(pe.getKey());
            if (petIndex < 0) {
                continue;
            }

            Set<Integer> exclItems = pe.getValue();
            if (!exclItems.isEmpty()) {
                client.announce(PetPacket.Packet.onLoadExceptionList(this.getId(), pe.getKey(), petIndex, new ArrayList<>(exclItems)));

                chrLock.lock();
                try {
                    for (Integer itemid : exclItems) {
                        excludedItems.add(itemid);
                    }
                } finally {
                    chrLock.unlock();
                }
            }
        }
    }

    public void exportExcludedItems(MapleClient c) {
        Map<Integer, Set<Integer>> petExcluded = this.getExcluded();
        for (Map.Entry<Integer, Set<Integer>> pe : petExcluded.entrySet()) {
            byte petIndex = this.getPetIndex(pe.getKey());
            if (petIndex < 0) {
                continue;
            }

            Set<Integer> exclItems = pe.getValue();
            if (!exclItems.isEmpty()) {
                c.announce(PetPacket.Packet.onLoadExceptionList(this.getId(), pe.getKey(), petIndex, new ArrayList<>(exclItems)));
            }
        }
    }

    public Map<Integer, Set<Integer>> getExcluded() {
        chrLock.lock();
        try {
            return Collections.unmodifiableMap(excluded);
        } finally {
            chrLock.unlock();
        }
    }

    public Set<Integer> getExcludedItems() {
        chrLock.lock();
        try {
            return Collections.unmodifiableSet(excludedItems);
        } finally {
            chrLock.unlock();
        }
    }

    public int getExp() {
        return exp.get();
    }

    public int getGachaExp() {
        return gachaexp.get();
    }

    public boolean hasNoviceExpRate() {
        return isBeginnerJob() && level < 11;
    }

    public double getExpRate() {
        if (hasNoviceExpRate()) {   // base exp rate 1x for early levels idea thanks to Vcoc
            return 1.0;
        }

        return expRate;
    }

    public int getDropRate() {
        return dropRate;
    }

    public double getBossDropRate() {
        World w = getWorldServer();
        return (dropRate / w.getDropRate()) * w.getBossDropRate();
    }

    public double getMesoRate() {
        return mesoRate;
    }

    public int getCouponMesoRate() {
        return mesoCoupon;
    }

    public double getRawMesoRate() {
        return mesoRate / (mesoCoupon * getWorldServer().getMesoRate());
    }

    public double getQuestExpRate() {
        World w = getWorldServer();
        if (hasNoviceExpRate()) {   // base exp rate 1x for noblesse and beginners
            return 1;
        }
        
        return w.getQuestRate();
    }

    public double getQuestMesoRate() {
        World w = getWorldServer();
        return w.getQuestRate();
    }

    public int getFace() {
        return face;
    }

    public int getFame() {
        return fame;
    }

    public MapleFamily getFamily() {
        return family;
    }

    public void setFamily(MapleFamily f) {
        this.family = f;
    }

    public int getFamilyId() {
        return familyId;
    }

    public boolean getFinishedDojoTutorial() {
        return finishedDojoTutorial;
    }

    public void setUsedStorage() {
        usedStorage = true;
    }

    public List<MapleRing> getFriendshipRings() {
        Collections.sort(friendshipRings);
        return friendshipRings;
    }

    public int getGender() {
        return gender;
    }

    public boolean isMale() {
        return getGender() == 0;
    }

    public MapleGuild getGuild() {
        try {
            return Server.getInstance().getGuild(getGuildId(), getWorld(), this);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public MapleAlliance getAlliance() {
        if (mgc != null) {
            try {
                return Server.getInstance().getAlliance(getGuild().getAllianceId());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    public int getGuildId() {
        return guildid;
    }

    public int getGuildRank() {
        return guildRank;
    }

    public int getHair() {
        return hair;
    }

    public MapleHiredMerchant getHiredMerchant() {
        return hiredMerchant;
    }

    public int getId() {
        return id;
    }

    public static int getAccountIdByName(String name) {
        int id;
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return -1;
                    }
                    id = rs.getInt("accountid");
                }
            }
            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int getIdByName(String name) {
        int id;
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM characters WHERE name ilike ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return -1;
                    }
                    id = rs.getInt("id");
                }
            }
            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getNameById(int id) {
        String name;
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT name FROM characters WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    name = rs.getString("name");
                }
            }

            return name;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getInitialSpawnpoint() {
        return initialSpawnPoint;
    }

    public MapleInventory getInventory(MapleInventoryType type) {
        return inventory[type.ordinal()];
    }

    public int getItemEffect() {
        return itemEffect;
    }

    public boolean haveItemWithId(int itemid, boolean checkEquipped) {
        return (inventory[ItemConstants.getInventoryType(itemid).ordinal()].findById(itemid) != null)
                || (checkEquipped && inventory[MapleInventoryType.EQUIPPED.ordinal()].findById(itemid) != null);
    }

    public boolean haveItemEquipped(int itemid) {
        return (inventory[MapleInventoryType.EQUIPPED.ordinal()].findById(itemid) != null);
    }

    public boolean haveWeddingRing() {
        int[] rings = {1112806, 1112803, 1112807, 1112809};

        for (int ringid : rings) {
            if (haveItemWithId(ringid, true)) {
                return true;
            }
        }

        return false;
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        int count = inventory[ItemConstants.getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped) {
            count += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        return count;
    }

    public int getCleanItemQuantity(int itemid, boolean checkEquipped) {
        int count = inventory[ItemConstants.getInventoryType(itemid).ordinal()].countNotOwnedById(itemid);
        if (checkEquipped) {
            count += inventory[MapleInventoryType.EQUIPPED.ordinal()].countNotOwnedById(itemid);
        }
        return count;
    }

    public MapleJob getJob() {
        return job;
    }

    public int getJobRank() {
        return jobRank;
    }

    public int getJobRankMove() {
        return jobRankMove;
    }

    public int getJobType() {
        return job.getId() / 1000;
    }

    public Map<Integer, MapleKeyBinding> getKeymap() {
        return keymap;
    }

    public long getLastHealed() {
        return lastHealed;
    }

    public long getLastUsedCashItem() {
        return lastUsedCashItem;
    }

    public int getLevel() {
        return level;
    }

    public int getFh() {
        MapleFoothold fh = getFoothold();
        if (fh != null) {
            return fh.getId();
        }
        return 0;
    }

    public MapleFoothold getFoothold() {
        Point pos = this.getPosition();
        pos.y -= 6;
        return getMap().getFootholds().findBelow(pos);
    }

    public int getMapId() {
        if (map != null) {
            return map.getId();
        }
        return mapid;
    }

    public MapleRing getMarriageRing() {
        return partnerId > 0 ? marriageRing : null;
    }

    public int getMasterLevel(PlayerSkill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).masterlevel;
    }

    public int getTotalStr() {
        return localstr;
    }

    public int getTotalDex() {
        return localdex;
    }

    public int getTotalInt() {
        return localint_;
    }

    public int getTotalLuk() {
        return localluk;
    }

    public int getTotalMagic() {
        return localmagic;
    }

    public int getTotalWatk() {
        return localwatk;
    }

    public int getMaxClassLevel() {
        return isCygnus() ? 120 : 200;
    }

    public int getMaxLevel() {
        return GameConstants.getJobMaxLevel(job);
    }

    public int getMeso() {
        return meso.get();
    }

    public int getMerchantMeso() {
        return merchantmeso;
    }

    public int getMerchantNetMeso() {
        int elapsedDays = 0;
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT timestamp FROM fredstorage WHERE cid = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        elapsedDays = FredrickProcessor.timestampElapsedDays(rs.getTimestamp(1), System.currentTimeMillis());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (elapsedDays > 100) elapsedDays = 100;

        long netMeso = merchantmeso; // negative mesos issues found thanks to Flash, Vcoc
        netMeso = (netMeso * (100 - elapsedDays)) / 100;
        return (int) netMeso;
    }

    public int getMesosTraded() {
        return mesosTraded;
    }

    public int getMessengerPosition() {
        return messengerposition;
    }

    public MapleGuildCharacter getMGC() {
        return mgc;
    }

    public void setMGC(MapleGuildCharacter mgc) {
        this.mgc = mgc;
    }

    public MaplePartyCharacter getMPC() {
        if (mpc == null) {
            mpc = new MaplePartyCharacter(this);
        }
        return mpc;
    }

    public void setMPC(MaplePartyCharacter mpc) {
        this.mpc = mpc;
    }

    public int getTargetHpBarHash() {
        return this.targetHpBarHash;
    }

    public void setTargetHpBarHash(int mobHash) {
        this.targetHpBarHash = mobHash;
    }

    public long getTargetHpBarTime() {
        return this.targetHpBarTime;
    }

    public void setTargetHpBarTime(long timeNow) {
        this.targetHpBarTime = timeNow;
    }

    public MapleMiniGame getMiniGame() {
        return miniGame;
    }

    public int getMiniGamePoints(MiniGameResult type, boolean omok) {
        if (omok) {
            return switch (type) {
                case WIN -> omokwins;
                case LOSS -> omoklosses;
                default -> omokties;
            };
        } else {
            return switch (type) {
                case WIN -> matchcardwins;
                case LOSS -> matchcardlosses;
                default -> matchcardties;
            };
        }
    }

    public MonsterBook getMonsterBook() {
        return monsterbook;
    }

    public int getMonsterBookCover() {
        return bookCover;
    }

    public MapleMount getMount() {
        return maplemount;
    }

    public MapleMessenger getMessenger() {
        return messenger;
    }

    public String getName() {
        return name;
    }

    public int getNextEmptyPetIndex() {
        petLock.lock();
        try {
            for (int i = 0; i < 3; i++) {
                if (pets[i] == null) {
                    return i;
                }
            }
            return 3;
        } finally {
            petLock.unlock();
        }
    }

    public int getNoPets() {
        petLock.lock();
        try {
            int ret = 0;
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    ret++;
                }
            }
            return ret;
        } finally {
            petLock.unlock();
        }
    }

    public MapleParty getParty() {
        prtLock.lock();
        try {
            return party;
        } finally {
            prtLock.unlock();
        }
    }

    public int getPartyId() {
        prtLock.lock();
        try {
            return (party != null ? party.getId() : -1);
        } finally {
            prtLock.unlock();
        }
    }

    public List<MapleCharacter> getPartyMembers() {
        List<MapleCharacter> list = new LinkedList<>();

        prtLock.lock();
        try {
            if (party != null) {
                for (MaplePartyCharacter partyMembers : party.getMembers()) {
                    list.add(partyMembers.getPlayer());
                }
            }
        } finally {
            prtLock.unlock();
        }

        return list;
    }

    public List<MapleCharacter> getPartyMembersOnSameMap() {
        List<MapleCharacter> list = new LinkedList<>();
        int thisMapHash = this.getMap().hashCode();

        prtLock.lock();
        try {
            if (party != null) {
                for (MaplePartyCharacter partyMembers : party.getMembers()) {
                    MapleCharacter chr = partyMembers.getPlayer();
                    MapleMap chrMap = chr.getMap();
                    if (chrMap != null && chrMap.hashCode() == thisMapHash && chr.isLoggedinWorld()) {
                        list.add(chr);
                    }
                }
            }
        } finally {
            prtLock.unlock();
        }

        return list;
    }

    public boolean isPartyMember(MapleCharacter chr) {
        return isPartyMember(chr.getId());
    }

    public boolean isPartyMember(int cid) {
        for (MapleCharacter mpcu : getPartyMembers()) {
            if (mpcu.getId() == cid) {
                return true;
            }
        }

        return false;
    }

    public List<MonsterDropEntry> retrieveRelevantDrops(int monsterId) {
        List<MapleCharacter> pchars = new LinkedList<>();
        for (MapleCharacter chr : getPartyMembers()) {
            if (chr.isLoggedinWorld()) {
                pchars.add(chr);
            }
        }

        if (pchars.isEmpty()) {
            pchars.add(this);
        }
        return MapleLootManager.retrieveRelevantDrops(monsterId, pchars);
    }

    public MaplePlayerShop getPlayerShop() {
        return playerShop;
    }

    public void setGMLevel(int level) {
        this.gmLevel = Math.min(level, 6);
        this.gmLevel = Math.max(level, 0);
    }

    public void closePlayerInteractions() {
        closeNpcShop();
        closeTrade();
        closePlayerShop();
        closeMiniGame(true);
        closeHiredMerchant(false);
        closePlayerMessenger();

        client.closePlayerScriptInteractions();
    }

    public void closeNpcShop() {
        setShop(null);
    }

    public void closeTrade() {
        MapleTrade.cancelTrade(this, MapleTrade.TradeResult.PARTNER_CANCEL);
    }

    public void closePlayerShop() {
        MaplePlayerShop mps = this.getPlayerShop();
        if (mps == null) {
            return;
        }

        if (mps.isOwner(this)) {
            mps.setOpen(false);
            getWorldServer().unregisterPlayerShop(mps);

            for (MaplePlayerShopItem mpsi : mps.getItems()) {
                if (mpsi.getBundles() >= 2) {
                    Item iItem = mpsi.getItem().copy();
                    iItem.setQuantity((short) (mpsi.getBundles() * iItem.getQuantity()));
                    MapleInventoryManipulator.addFromDrop(this.getClient(), iItem, false);
                } else if (mpsi.isExist()) {
                    MapleInventoryManipulator.addFromDrop(this.getClient(), mpsi.getItem(), true);
                }
            }
            mps.closeShop();
        } else {
            mps.removeVisitor(this);
        }
        this.setPlayerShop(null);
    }

    public void closeMiniGame(boolean close) {
        MapleMiniGame game = this.getMiniGame();
        if (game == null) {
            return;
        }

        if (game.isOwner(this)) {
            game.closeRoom(close);
        } else {
            game.removeVisitor(close, this);
        }
    }

    public void closeHiredMerchant(boolean closeMerchant) {
        MapleHiredMerchant merchant = this.getHiredMerchant();
        if (merchant == null) {
            return;
        }

        if (closeMerchant) {
            if (merchant.isOwner(this) && merchant.getItems().isEmpty()) {
                merchant.forceClose();
            } else {
                merchant.removeVisitor(this);
                this.setHiredMerchant(null);
            }
        } else {
            if (merchant.isOwner(this)) {
                merchant.setOpen(true);
            } else {
                merchant.removeVisitor(this);
            }
            try {
                merchant.saveItems(false);
            } catch (SQLException ex) {
                ex.printStackTrace();
                FilePrinter.printError(FilePrinter.EXCEPTION_CAUGHT, "Error while saving " + name + "'s Hired Merchant items.");
            }
        }
    }

    public void closePlayerMessenger() {
        MapleMessenger m = this.getMessenger();
        if (m == null) {
            return;
        }

        World w = getWorldServer();
        MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(this, this.getMessengerPosition());

        w.leaveMessenger(m.getId(), messengerplayer);
        this.setMessenger(null);
        this.setMessengerPosition(4);
    }

    public MaplePet[] getPets() {
        petLock.lock();
        try {
            return Arrays.copyOf(pets, pets.length);
        } finally {
            petLock.unlock();
        }
    }

    public MaplePet getPet(int index) {
        if (index < 0) {
            return null;
        }

        petLock.lock();
        try {
            return pets[index];
        } finally {
            petLock.unlock();
        }
    }

    public byte getPetIndex(int petId) {
        petLock.lock();
        try {
            for (byte i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    if (pets[i].getUniqueId() == petId) {
                        return i;
                    }
                }
            }
            return -1;
        } finally {
            petLock.unlock();
        }
    }

    public byte getPetIndex(MaplePet pet) {
        petLock.lock();
        try {
            for (byte i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    if (pets[i].getUniqueId() == pet.getUniqueId()) {
                        return i;
                    }
                }
            }
            return -1;
        } finally {
            petLock.unlock();
        }
    }

    public int getPossibleReports() {
        return possibleReports;
    }

    public final byte getQuestStatus(final int quest) {
        synchronized (quests) {
            MapleQuestStatus mqs = quests.get((short) quest);
            if (mqs != null) {
                return (byte) mqs.getStatus().getId();
            } else {
                return 0;
            }
        }
    }

    public final MapleQuestStatus getMapleQuestStatus(final int quest) {
        synchronized (quests) {
            MapleQuestStatus mqs = quests.get((short) quest);
            return mqs;
        }
    }

    public MapleQuestStatus getQuest(MapleQuest quest) {
        synchronized (quests) {
            if (!quests.containsKey(quest.getId())) {
                return new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
            }
            return quests.get(quest.getId());
        }
    }

    //---- \/ \/ \/ \/ \/ \/ \/  NOT TESTED  \/ \/ \/ \/ \/ \/ \/ \/ \/ ----

    public final void setQuestAdd(final MapleQuest quest, final byte status, final String customData) {
        synchronized (quests) {
            if (!quests.containsKey(quest.getId())) {
                final MapleQuestStatus stat = new MapleQuestStatus(quest, MapleQuestStatus.Status.getById(status));
                stat.setCustomData(customData);
                quests.put(quest.getId(), stat);
            }
        }
    }

    public final MapleQuestStatus getQuestNAdd(final MapleQuest quest) {
        synchronized (quests) {
            if (!quests.containsKey(quest.getId())) {
                final MapleQuestStatus status = new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
                quests.put(quest.getId(), status);
                return status;
            }
            return quests.get(quest.getId());
        }
    }

    public final MapleQuestStatus getQuestNoAdd(final MapleQuest quest) {
        synchronized (quests) {
            return quests.get(quest.getId());
        }
    }

    public final MapleQuestStatus getQuestRemove(final MapleQuest quest) {
        synchronized (quests) {
            return quests.remove(quest.getId());
        }
    }

    //---- /\ /\ /\ /\ /\ /\ /\  NOT TESTED  /\ /\ /\ /\ /\ /\ /\ /\ /\ ----

    public boolean needQuestItem(int questid, int itemid) {
        if (questid <= 0) { //For non quest items :3
            return true;
        }

        int amountNeeded, questStatus = this.getQuestStatus(questid);
        if (questStatus == 0) {
            amountNeeded = MapleQuest.getInstance(questid).getStartItemAmountNeeded(itemid);
        } else if (questStatus != 1) {
            return false;
        } else {
            amountNeeded = MapleQuest.getInstance(questid).getCompleteItemAmountNeeded(itemid);
        }

        return amountNeeded > 0 && getInventory(ItemConstants.getInventoryType(itemid)).countById(itemid) < amountNeeded;
    }

    public int getPlayerRank() {
        return playerRank;
    }

    public int getRankMove() {
        return rankMove;
    }

    public void clearSavedLocation(SavedLocationType type) {
        savedLocations[type.ordinal()] = null;
    }

    public int peekSavedLocation(String type) {
        SavedLocation sl = savedLocations[SavedLocationType.fromString(type).ordinal()];
        if (sl == null) {
            return -1;
        }
        return sl.getMapId();
    }

    public int getSavedLocation(String type) {
        int m = peekSavedLocation(type);
        clearSavedLocation(SavedLocationType.fromString(type));

        return m;
    }

    public String getSearch() {
        return search;
    }

    public MapleShop getShop() {
        return shop;
    }

    public Map<PlayerSkill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    public int getSkillLevel(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return 0;
        }
        return ret.skillevel;
    }

    public byte getSkillLevel(PlayerSkill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).skillevel;
    }

    public long getSkillExpiration(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return -1;
        }
        return ret.expiration;
    }

    public long getSkillExpiration(PlayerSkill skill) {
        if (skills.get(skill) == null) {
            return -1;
        }
        return skills.get(skill).expiration;
    }

    public MapleSkinColor getSkinColor() {
        return skinColor;
    }

    public int getSlot() {
        return slots;
    }

    public final List<MapleQuestStatus> getStartedQuests() {
        synchronized (quests) {
            List<MapleQuestStatus> ret = new LinkedList<>();
            for (MapleQuestStatus q : quests.values()) {
                if (q.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                    ret.add(q);
                }
            }
            return Collections.unmodifiableList(ret);
        }
    }

    public final int getStartedQuestsSize() {
        synchronized (quests) {
            int i = 0;
            for (MapleQuestStatus q : quests.values()) {
                if (q.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                    if (q.getQuest().getInfoNumber() > 0) {
                        i++;
                    }
                    i++;
                }
            }
            return i;
        }
    }

    public MapleStorage getStorage() {
        return storage;
    }

    public Collection<MapleSummon> getSummonsValues() {
        return summons.values();
    }

    public void clearSummons() {
        summons.clear();
    }

    public MapleSummon getSummonByKey(int id) {
        return summons.get(id);
    }

    public boolean isSummonsEmpty() {
        return summons.isEmpty();
    }

    public boolean containsSummon(MapleSummon summon) {
        return summons.containsValue(summon);
    }

    public MapleTrade getTrade() {
        return trade;
    }

    public int getVanquisherKills() {
        return vanquisherKills;
    }

    public int getVanquisherStage() {
        return vanquisherStage;
    }

    public MapleMapObject[] getVisibleMapObjects() {
        return visibleMapObjects.toArray(new MapleMapObject[visibleMapObjects.size()]);
    }

    public int getWorld() {
        return world;
    }

    public World getWorldServer() {
        return Server.getInstance().getWorld(world);
    }

    public void giveCoolDowns(final int skillid, long starttime, long length) {
        if (skillid == 5221999) {
            this.battleshipHp = (int) length;
            addCooldown(skillid, 0, length, null);
        } else {
            long timeNow = Server.getInstance().getCurrentTime();
            int time = (int) ((length + starttime) - timeNow);
            addCooldown(
                    skillid,
                    System.currentTimeMillis(),
                    time,
                    TimerManager.getInstance().schedule(
                            new CancelCooldownAction(this, skillid), time));
        }
    }

    public int gmLevel() {
        return gmLevel;
    }

    public final int getClearance() {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT clearance FROM characters WHERE id = ?")) {
                ps.setInt(1, id);
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

    public final int getTrophy() {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT trophy FROM characters WHERE id = ?")) {
                ps.setInt(1, id);
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

    private void guildUpdate() {
        mgc.setLevel(level);
        mgc.setJobId(job.getId());

        if (this.guildid < 1) {
            return;
        }

        try {
            Server.getInstance().memberLevelJobUpdate(this.mgc);
            int allianceId = getGuild().getAllianceId();
            if (allianceId > 0) {
                Server.getInstance().allianceMessage(allianceId,
                        AlliancePacket.Packet.onAllianceResult(this, AllianceResultType.JobLevelUpdate.getResult()), getId(), -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleEnergyChargeGain() { // to get here energychargelevel has to be > 0
        PlayerSkill energycharge = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.ENERGY_CHARGE) : SkillFactory.getSkill(Marauder.ENERGY_CHARGE);
        MapleStatEffect ceffect;
        ceffect = energycharge.getEffect(getSkillLevel(energycharge));
        TimerManager tMan = TimerManager.getInstance();
        if (energybar < 10000) {
            energybar += 102;
            if (energybar > 10000) {
                energybar = 10000;
            }
            List<Pair<MapleBuffStat, BuffValueHolder>> stat = Collections.singletonList(new Pair<>(
                    MapleBuffStat.ENERGY_CHARGE,  new BuffValueHolder(0, 0, energybar)));
            setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energybar);
            client.announce(WvsContext.Packet.giveBuff(energybar, 0, stat));
            client.announce(MaplePacketCreator.showOwnBuffEffect(energycharge.getId(), 2));
            getMap().broadcastMessage(this, UserRemote.Packet.showBuffEffect(id, energycharge.getId(), 2));
            getMap().broadcastMessage(this, UserRemote.Packet.giveForeignBuff(energybar, stat));
        }
        if (energybar >= 10000 && energybar < 11000) {
            energybar = 15000;
            final MapleCharacter chr = this;
            tMan.schedule(() -> {
                energybar = 0;
                List<Pair<MapleBuffStat, BuffValueHolder>> stat = Collections.singletonList(new Pair<>(
                        MapleBuffStat.ENERGY_CHARGE,  new BuffValueHolder(0, 0, energybar)));;
                setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energybar);
                client.announce(WvsContext.Packet.giveBuff(energybar, 0, stat));
                getMap().broadcastMessage(chr, UserRemote.Packet.giveForeignBuff(energybar, stat));
            }, ceffect.getDuration());
        }
    }

    public void handleOrbconsume() {
        int skillid = isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
        PlayerSkill combo = SkillFactory.getSkill(skillid);
        List<Pair<MapleBuffStat, BuffValueHolder>> stat = Collections.singletonList(new Pair<>(
                MapleBuffStat.COMBO, new BuffValueHolder(0, 0, 1)));
        setBuffedValue(MapleBuffStat.COMBO, 1);
        client.announce(WvsContext.Packet.giveBuff(skillid, combo.getEffect(
                getSkillLevel(combo)).getDuration() + (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis())), stat));
        getMap().broadcastMessage(this, UserRemote.Packet.giveForeignBuff(getId(), stat), false);
    }

    public boolean hasEntered(String script) {
        for (int mapId : entered.keySet()) {
            if (entered.get(mapId).equals(script)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEntered(String script, int mapId) {
        if (entered.containsKey(mapId)) {
            return entered.get(mapId).equals(script);
        }
        return false;
    }

    public void hasGivenFame(MapleCharacter to) {
        lastfametime = System.currentTimeMillis();
        lastmonthfameids.add(to.getId());
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Insert.into("fame_log")
                    .add("characterid", getId())
                    .add("characterid_to", to.getId())
                    .execute(con);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasMerchant() {
        return hasMerchant;
    }

    public boolean haveItem(int itemid) {
        return getItemQuantity(itemid, ItemConstants.isEquipment(itemid)) > 0;
    }

    public boolean haveCleanItem(int itemid) {
        return getCleanItemQuantity(itemid, ItemConstants.isEquipment(itemid)) > 0;
    }

    public boolean hasEmptySlot(int itemId) {
        return getInventory(ItemConstants.getInventoryType(itemId)).getNextFreeSlot() > -1;
    }

    public boolean hasEmptySlot(byte invType) {
        return getInventory(MapleInventoryType.getByType(invType)).getNextFreeSlot() > -1;
    }

    public void increaseGuildCapacity() {
        int cost = MapleGuild.getIncreaseGuildCost(getGuild().getCapacity());

        if (getMeso() < cost) {
            dropMessage(1, "You don't have enough mesos.");
            return;
        }

        if (Server.getInstance().increaseGuildCapacity(guildid)) {
            gainMeso(-cost, true, false, true);
        } else {
            dropMessage(1, "Your guild already reached the maximum capacity of players.");
        }
    }

    public boolean isActiveBuffedValue(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isSkill()
                    && mbsvh.effect.getSourceId() == skillid) {
                return true;
            }
        }
        return false;
    }

    private boolean canBuyback(int fee, boolean usingMesos) {
        return (usingMesos ? this.getMeso() : cashshop.getCash(1)) >= fee;
    }

    private void applyBuybackFee(int fee, boolean usingMesos) {
        if (usingMesos) {
            this.gainMeso(-fee);
        } else {
            cashshop.gainCash(1, -fee);
        }
    }

    private long getNextBuybackTime() {
        return lastBuyback + ServerConstants.BUYBACK_COOLDOWN_MINUTES * 60 * 1000;
    }

    private boolean isBuybackInvincible() {
        return Server.getInstance().getCurrentTime() - lastBuyback < 4200;
    }

    private int getBuybackFee() {
        float fee = ServerConstants.BUYBACK_FEE;
        int grade = Math.min(Math.max(level, 30), 120) - 30;

        fee += (grade * ServerConstants.BUYBACK_LEVEL_STACK_FEE);
        if (ServerConstants.USE_BUYBACK_WITH_MESOS) {
            fee *= ServerConstants.BUYBACK_MESO_MULTIPLIER;
        }

        return (int) Math.floor(fee);
    }

    public void showBuybackInfo() {
        String s = "#eBUYBACK STATUS#n\r\n\r\nCurrent buyback fee: #b" + getBuybackFee() + " " + (ServerConstants.USE_BUYBACK_WITH_MESOS ? "mesos" : "NX") + "#k\r\n\r\n";

        long timeNow = Server.getInstance().getCurrentTime();
        boolean avail = true;
        if (!isAlive()) {
            long timeLapsed = timeNow - lastDeathtime;
            long timeRemaining = ServerConstants.BUYBACK_RETURN_MINUTES * 60 * 1000 - (timeLapsed + Math.max(0, getNextBuybackTime() - timeNow));
            if (timeRemaining < 1) {
                s += "Buyback #e#rUNAVAILABLE#k#n";
                avail = false;
            } else {
                s += "Buyback countdown: #e#b" + getTimeRemaining(ServerConstants.BUYBACK_RETURN_MINUTES * 60 * 1000 - timeLapsed) + "#k#n";
            }
            s += "\r\n";
        }

        if (timeNow < getNextBuybackTime() && avail) {
            s += "Buyback available in #r" + getTimeRemaining(getNextBuybackTime() - timeNow) + "#k";
            s += "\r\n";
        }

        this.showHint(s);
    }

    private static String getTimeRemaining(long timeLeft) {
        int seconds = (int) Math.floor(timeLeft / 1000.0) % 60;
        int minutes = (int) Math.floor(timeLeft / (1000.0 * 60)) % 60;

        return (minutes > 0 ? (String.format("%02d", minutes) + " minutes, ") : "") + String.format("%02d", seconds) + " seconds";
    }

    public boolean couldBuyback() {  // Ronan's buyback system
        long timeNow = Server.getInstance().getCurrentTime();

        if (timeNow - lastDeathtime > ServerConstants.BUYBACK_RETURN_MINUTES * 60 * 1000) {
            this.dropMessage(5, "The period of time to decide has expired, therefore you are unable to buyback.");
            return false;
        }

        long nextBuybacktime = getNextBuybackTime();
        if (timeNow < nextBuybacktime) {
            long timeLeft = nextBuybacktime - timeNow;
            this.dropMessage(5, "Next buyback available in " + getTimeRemaining(timeLeft) + ".");
            return false;
        }

        boolean usingMesos = ServerConstants.USE_BUYBACK_WITH_MESOS;
        int fee = getBuybackFee();

        if (!canBuyback(fee, usingMesos)) {
            this.dropMessage(5, "You don't have " + fee + " " + (usingMesos ? "mesos" : "NX") + " to buyback.");
            return false;
        }

        lastBuyback = timeNow;
        applyBuybackFee(fee, usingMesos);
        return true;
    }

    public boolean isBuffFrom(MapleBuffStat stat, PlayerSkill skill) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        return mbsvh != null && mbsvh.effect.isSkill()
                && mbsvh.effect.getSourceId() == skill.getId();
    }

    public boolean isGmJob() {
        int jn = job.getJobNiche();
        return jn >= 8 && jn <= 9;
    }

    public boolean isCygnus() {
        return getJobType() == 1;
    }

    public boolean isAran() {
        return job.getId() >= 2000 && job.getId() <= 2112;
    }

    public boolean isBeginnerJob() {
        return (job.getId() == 0 || job.getId() == 1000 || job.getId() == 2000);
    }

    public boolean isGM() {
        return gmLevel >= 2;
    }
    
    public boolean isDonor() {
        return gmLevel == 1;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        return visibleMapObjects.contains(mo);
    }

    public boolean isPartyLeader() {
        prtLock.lock();
        try {
            return party.getLeaderId() == getId();
        } finally {
            prtLock.unlock();
        }
    }

    public boolean isGuildLeader() {    // true on guild master or jr. master
        return guildid > 0 && guildRank < 3;
    }

    public void leaveMap() {
        //releaseControlledMonsters();
        controlled.clear();
        visibleMapObjects.clear();
        setChair(-1);
        if (hpDecreaseTask != null) {
            hpDecreaseTask.cancel(false);
        }

        AriantColiseum arena = this.getAriantColiseum();
        if (arena != null) {
            arena.leaveArena(this);
        }
    }

    private int getChangedJobSp(MapleJob newJob) {
        int curSp = getUsedSp(newJob) + getJobRemainingSp(newJob);
        int spGain = 0;
        int expectedSp = getJobLevelSp(level - 10, newJob, GameConstants.getJobBranch(newJob));
        if (curSp < expectedSp) {
            spGain += (expectedSp - curSp);
        }

        return getSpGain(spGain, curSp, job);
    }

    private int getUsedSp(MapleJob job) {
        int jobId = job.getId();
        int spUsed = 0;

        for (Entry<PlayerSkill, SkillEntry> s : this.getSkills().entrySet()) {
            PlayerSkill skill = s.getKey();
            if (GameConstants.isInJobTree(skill.getId(), jobId) && !skill.isBeginnerSkill()) {
                spUsed += s.getValue().skillevel;
            }
        }

        return spUsed;
    }

    private int getJobLevelSp(int level, MapleJob job, int jobBranch) {
        if (getJobStyleInternal(job.getId(), (byte) 0x40) == MapleJob.MAGICIAN) {
            level += 2;  // starts earlier, level 8
        }

        return 3 * level + GameConstants.getChangeJobSpUpgrade(jobBranch);
    }

    private int getJobMaxSp(MapleJob job) {
        int jobBranch = GameConstants.getJobBranch(job);
        int jobRange = GameConstants.getJobUpgradeLevelRange(jobBranch);
        return getJobLevelSp(jobRange, job, jobBranch);
    }

    private int getJobRemainingSp(MapleJob job) {
        int skillBook = GameConstants.getSkillBook(job.getId());

        int ret = 0;
        for (int i = 0; i <= skillBook; i++) {
            ret += this.getRemainingSp(i);
        }

        return ret;
    }

    private int getSpGain(int spGain, MapleJob job) {
        int curSp = getUsedSp(job) + getJobRemainingSp(job);
        return getSpGain(spGain, curSp, job);
    }

    private int getSpGain(int spGain, int curSp, MapleJob job) {
        int maxSp = getJobMaxSp(job);

        spGain = Math.min(spGain, maxSp - curSp);
        int jobBranch = GameConstants.getJobBranch(job);
        return spGain;
    }

    private void levelUpGainSp() {
        if (GameConstants.getJobBranch(job) == 0) {
            return;
        }

        int spGain = 3;
        if (!GameConstants.hasSPTable(job)) {
            spGain = getSpGain(spGain, job);
        }

        if (spGain > 0) {
            gainSp(spGain, GameConstants.getSkillBook(job.getId()), true);
        }
    }

    public synchronized void levelUp(boolean takeexp) {
        PlayerSkill improvingMaxHP = null;
        PlayerSkill improvingMaxMP = null;
        int improvingMaxHPLevel = 0;
        int improvingMaxMPLevel = 0;

        boolean isBeginner = isBeginnerJob();
        if (isBeginner && level < 11) {
            effLock.lock();
            statWlock.lock();
            try {
                gainAp(5, true);

                int str = 0, dex = 0;
                if (level < 6) {
                    str += 5;
                } else {
                    str += 4;
                    dex += 1;
                }

                assignStrDexIntLuk(str, dex, 0, 0);
            } finally {
                statWlock.unlock();
                effLock.unlock();
            }
        } else {
            int remainingAp = 5;

            // Cygnus get +6 AP every level, and +5 AP every level after 70.
            if (isCygnus() && level <= 70) {
                remainingAp += 1;
            }

            gainAp(remainingAp, true);
        }

        int addhp = 0, addmp = 0;
        if (isBeginner) {
            addhp += Randomizer.rand(12, 16);
            addmp += Randomizer.rand(10, 12);
        } else if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1)) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(DawnWarrior.MAX_HP_INCREASE) : SkillFactory.getSkill(Swordsman.IMPROVED_MAX_HP_INCREASE);
//            if (job.isA(MapleJob.CRUSADER)) {
//                improvingMaxMP = SkillFactory.getSkill(1210000);
//            } else if (job.isA(MapleJob.DAWNWARRIOR2)) {
//                improvingMaxMP = SkillFactory.getSkill(11110000);
//            }
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            addhp += Randomizer.rand(24, 28);
            addmp += Randomizer.rand(4, 6);
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            improvingMaxMP = isCygnus() ? SkillFactory.getSkill(BlazeWizard.INCREASING_MAX_MP) : SkillFactory.getSkill(Magician.IMPROVED_MAX_MP_INCREASE);
            improvingMaxMPLevel = getSkillLevel(improvingMaxMP);
            addhp += Randomizer.rand(10, 14);
            addmp += Randomizer.rand(22, 24);
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.THIEF) || (job.getId() > 1299 && job.getId() < 1500)) {
            addhp += Randomizer.rand(20, 24);
            addmp += Randomizer.rand(14, 16);
        } else if (job.isA(MapleJob.GM)) {
            addhp += 30000;
            addmp += 30000;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.IMPROVE_MAX_HP) : SkillFactory.getSkill(Brawler.IMPROVE_MAX_HP);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            addhp += Randomizer.rand(22, 28);
            addmp += Randomizer.rand(18, 23);
        } else if (job.isA(MapleJob.ARAN1)) {
            addhp += Randomizer.rand(44, 48);
            int aids = Randomizer.rand(4, 8);
            addmp += aids + Math.floor(aids * 0.1);
        }
        if (improvingMaxHPLevel > 0) {
            addhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
        }
        if (improvingMaxMPLevel > 0) {
            addmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
        }

        addmp += localint_ / 10;

        addMaxMPMaxHP(addhp, addmp, true);

        if (takeexp) {
            exp.addAndGet(-ExpTable.INSTANCE.getExpNeededForLevel(level));
            if (exp.get() < 0) {
                exp.set(0);
            }
        }

        level++;
        if (level >= getMaxClassLevel()) {
            exp.set(0);

            int maxClassLevel = getMaxClassLevel();
            if (level == maxClassLevel) {
                if (!this.isGM()) {
                    if (ServerConstants.PLAYERNPC_AUTODEPLOY) {
                        ThreadManager.getInstance().newTask(() -> MaplePlayerNPC.spawnPlayerNPC(GameConstants.getHallOfFameMapid(job), MapleCharacter.this));
                    }

                    final String names = (getMedalText() + name);
                    getWorldServer().broadcastPacket(MaplePacketCreator.serverNotice(6, String.format(LEVEL_200, names, maxClassLevel, names)));
                }
            }

            level = maxClassLevel; //To prevent levels past the maximum
        }

        levelUpGainSp();

        effLock.lock();
        statWlock.lock();
        try {
            recalcLocalStats();
            changeHpMp(localmaxhp, localmaxmp, true);

            List<Pair<MapleStat, Integer>> statup = new ArrayList<>(10);
            statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
            statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp[GameConstants.getSkillBook(job.getId())]));
            statup.add(new Pair<>(MapleStat.HP, hp));
            statup.add(new Pair<>(MapleStat.MP, mp));
            statup.add(new Pair<>(MapleStat.EXP, exp.get()));
            statup.add(new Pair<>(MapleStat.LEVEL, level));
            statup.add(new Pair<>(MapleStat.MAXHP, clientmaxhp));
            statup.add(new Pair<>(MapleStat.MAXMP, clientmaxmp));
            statup.add(new Pair<>(MapleStat.STR, str));
            statup.add(new Pair<>(MapleStat.DEX, dex));

            client.announce(WvsContext.Packet.updatePlayerStats(statup, true, this));
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }

        getMap().broadcastMessage(this, UserRemote.Packet.showForeignEffect(getId(), 0), false);
        setMPC(new MaplePartyCharacter(this));
        silentPartyUpdate();

        if (this.guildid > 0) {
            getGuild().broadcast(WvsContext.Packet.onNotifyLevelUp(2, level, name), this.getId());
        }

        // achievements
        this.finishWorldTour(WorldTour.AchievementType.LEVELUP, level);

        guildUpdate();
        this.saveCharToDB(true);

        if (ServerConstants.HTTP_SERVER) Server.httpWorker.add("http://localhost:17003/api/character_levelup/" + id);
    }

    public boolean leaveParty() {
        MapleParty party;
        boolean partyLeader;

        prtLock.lock();
        try {
            party = getParty();
            partyLeader = party != null && isPartyLeader();
        } finally {
            prtLock.unlock();
        }

        if (party != null) {
            if (partyLeader) party.assignNewLeader(client);
            MapleParty.leaveParty(party, client);

            return true;
        } else {
            return false;
        }
    }

    public void revertLastPlayerRates() {
        this.expRate /= GameConstants.getPlayerBonusExpRate((this.level - 1) / 20);
        this.mesoRate /= GameConstants.getPlayerBonusMesoRate((this.level - 1) / 20);
        this.dropRate /= GameConstants.getPlayerBonusDropRate((this.level - 1) / 20);
    }

    public void revertPlayerRates() {
        this.expRate /= GameConstants.getPlayerBonusExpRate(this.level / 20);
        this.mesoRate /= GameConstants.getPlayerBonusMesoRate(this.level / 20);
        this.dropRate /= GameConstants.getPlayerBonusDropRate(this.level / 20);
    }

    public void setWorldRates() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC-0"));
        World worldz = getWorldServer();
        int hr = cal.get(Calendar.HOUR_OF_DAY);

        // ugly but works :shrug:
        if ((haveItem(5360001) && hr > 6 && hr < 12) || (haveItem(5360002) && hr > 9 && hr < 15)
                || (haveItem(536000) && hr > 12 && hr < 18) || (haveItem(5360004) && hr > 15 && hr < 21)
                || (haveItem(536000) && hr > 18) || (haveItem(5360006) && hr < 5)
                || (haveItem(5360007) && hr > 2 && hr < 6) || (haveItem(5360008) && hr >= 6 && hr < 11)) {
            this.mesoRate = 2 * worldz.getMesoRate();
        }
        if ((haveItem(5211000) && hr > 17 && hr < 21) || (haveItem(5211014) && hr > 6 && hr < 12)
                || (haveItem(5211015) && hr > 9 && hr < 15) || (haveItem(5211016) && hr > 12 && hr < 18)
                || (haveItem(5211017) && hr > 15 && hr < 21) || (haveItem(5211018) && hr > 14)
                || (haveItem(5211039) && hr < 5) || (haveItem(5211042) && hr > 2 && hr < 8)
                || (haveItem(5211045) && hr > 5 && hr < 11) || haveItem(5211048)) {
            if (hasNoviceExpRate()) {
                this.expRate = 2;
            } else {
                this.expRate = 2 * worldz.getExpRate();
            }
        }
        this.expRate = worldz.getExpRate();
        this.mesoRate = worldz.getMesoRate();
        this.dropRate *= worldz.getDropRate();
    }

    public void revertWorldRates() {
        World worldz = getWorldServer();
        this.expRate /= worldz.getExpRate();
        this.mesoRate /= worldz.getMesoRate();
        this.dropRate /= worldz.getDropRate();
    }

    private void setCouponRates() {
        List<Integer> couponEffects;

        Collection<Item> cashItems = this.getInventory(MapleInventoryType.CASH).list();
        chrLock.lock();
        try {
            setActiveCoupons(cashItems);
            couponEffects = activateCouponsEffects();
        } finally {
            chrLock.unlock();
        }

        for (Integer couponId : couponEffects) {
            commitBuffCoupon(couponId);
        }
    }

    private void revertCouponRates() {
        revertCouponsEffects();
    }

    public void updateCouponRates() {
        if (cpnLock.tryLock()) {
            MapleInventory cashInv = this.getInventory(MapleInventoryType.CASH);

            effLock.lock();
            chrLock.lock();
            cashInv.lockInventory();
            try {
                revertCouponRates();
                setCouponRates();
            } finally {
                cpnLock.unlock();

                cashInv.unlockInventory();
                chrLock.unlock();
                effLock.unlock();
            }
        }
    }

    public void resetPlayerRates() {
        expRate = 1;
        mesoRate = 1;
        dropRate = 1;

        expCoupon = 1;
        mesoCoupon = 1;
        dropCoupon = 1;
    }

    private int getCouponMultiplier(int couponId) {
        return activeCouponRates.get(couponId);
    }

    private void setExpCouponRate(int couponId, int couponQty) {
        this.expCoupon *= (getCouponMultiplier(couponId) * couponQty);
    }

    private void setDropCouponRate(int couponId, int couponQty) {
        this.dropCoupon *= (getCouponMultiplier(couponId) * couponQty);
        this.mesoCoupon *= (getCouponMultiplier(couponId) * couponQty);
    }

    private void revertCouponsEffects() {
        //dispelBuffCoupons();

        this.expRate /= this.expCoupon;
        this.dropRate /= this.dropCoupon;
        this.mesoRate /= this.mesoCoupon;

        this.expCoupon = 1;
        this.dropCoupon = 1;
        this.mesoCoupon = 1;
    }

    private List<Integer> activateCouponsEffects() {
        List<Integer> toCommitEffect = new LinkedList<>();

        int maxExpRate = 1, maxDropRate = 1, maxExpCouponId = -1, maxDropCouponId = -1;

        for (Entry<Integer, Integer> coupon : activeCoupons.entrySet()) {
            int couponId = coupon.getKey();

            if (ItemConstants.isExpCoupon(couponId)) {
                if (maxExpRate < getCouponMultiplier(couponId)) {
                    maxExpCouponId = couponId;
                    maxExpRate = getCouponMultiplier(couponId);
                }
            } else {
                if (maxDropRate < getCouponMultiplier(couponId)) {
                    maxDropCouponId = couponId;
                    maxDropRate = getCouponMultiplier(couponId);
                }
            }
        }

        if (maxExpCouponId > -1) {
            toCommitEffect.add(maxExpCouponId);
        }
        if (maxDropCouponId > -1) {
            toCommitEffect.add(maxDropCouponId);
        }

        this.expCoupon = maxExpRate;
        this.dropCoupon = maxDropRate;
        this.mesoCoupon = maxDropRate;

        this.expRate *= this.expCoupon;
        this.dropRate *= this.dropCoupon;
        this.mesoRate *= this.mesoCoupon;

        return toCommitEffect;
    }

    private void setActiveCoupons(Collection<Item> cashItems) {
        activeCoupons.clear();
        activeCouponRates.clear();

        Map<Integer, Integer> coupons = Server.getInstance().getCouponRates();
        List<Integer> active = Server.getInstance().getActiveCoupons();

        for (Item it : cashItems) {
            if (ItemConstants.isRateCoupon(it.getItemId()) && active.contains(it.getItemId())) {
                Integer count = activeCoupons.get(it.getItemId());

                if (count != null) {
                    activeCoupons.put(it.getItemId(), count + 1);
                } else {
                    activeCoupons.put(it.getItemId(), 1);
                    activeCouponRates.put(it.getItemId(), coupons.get(it.getItemId()));
                }
            }
        }
    }

    private void commitBuffCoupon(int couponid) {
        if (!isLoggedin() || getCashShop().isOpened()) {
            return;
        }

        MapleStatEffect mse = ii.getItemEffect(couponid);
        mse.applyTo(this);
    }

/*
    public void dispelBuffCoupons() {
        List<MapleBuffStatValueHolder> allBuffs = getAllStatups();

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (ItemConstants.isRateCoupon(mbsvh.effect.getSourceId())) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }
*/

    public Set<Integer> getActiveCoupons() {
        chrLock.lock();
        try {
            return Collections.unmodifiableSet(activeCoupons.keySet());
        } finally {
            chrLock.unlock();
        }
    }

    public void addPlayerRing(MapleRing ring) {
        int ringItemId = ring.getItemId();
        if (ItemConstants.isWeddingRing(ringItemId)) {
            this.addMarriageRing(ring);
        } else if (ring.getItemId() > 1112012) {
            this.addFriendshipRing(ring);
        } else {
            this.addCrushRing(ring);
        }
    }

    public static MapleCharacter loadCharacterEntryFromDB(ResultSet rs, List<Item> equipped) {
        MapleCharacter ret = new MapleCharacter();

        try {
            ret.accountid = rs.getInt("accountid");
            ret.id = rs.getInt("id");
            ret.name = rs.getString("name");
            ret.gender = rs.getInt("gender");
            ret.skinColor = MapleSkinColor.getById(rs.getInt("skincolor"));
            ret.face = rs.getInt("face");
            ret.hair = rs.getInt("hair");

            // skipping pets, probably unneeded here

            ret.level = rs.getInt("level");
            ret.job = MapleJob.getById(rs.getInt("job"));
            ret.str = rs.getInt("str");
            ret.dex = rs.getInt("dex");
            ret.int_ = rs.getInt("int");
            ret.luk = rs.getInt("luk");
            ret.hp = rs.getInt("hp");
            ret.setMaxHp(rs.getInt("maxhp"));
            ret.mp = rs.getInt("mp");
            ret.setMaxMp(rs.getInt("maxmp"));
            ret.remainingAp = rs.getInt("ap");
            ret.loadCharSkillPoints(rs.getString("sp").split(","));
            ret.exp.set(rs.getInt("exp"));
            ret.fame = rs.getInt("fame");
            ret.gachaexp.set(rs.getInt("gachaexp"));
            ret.mapid = rs.getInt("map");
            ret.initialSpawnPoint = rs.getInt("spawnpoint");

            ret.gmLevel = rs.getInt("gm");
            ret.world = rs.getByte("world");
            ret.playerRank = rs.getInt("playerRank");
            ret.rankMove = rs.getInt("rankMove");
            ret.jobRank = rs.getInt("jobRank");
            ret.jobRankMove = rs.getInt("jobRankMove");

            if (equipped != null) {  // players can have no equipped items at all, ofc
                MapleInventory inv = ret.inventory[MapleInventoryType.EQUIPPED.ordinal()];
                for (Item item : equipped) {
                    inv.addItemFromDB(item);
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return ret;
    }

    public MapleCharacter generateCharacterEntry() {
        MapleCharacter ret = new MapleCharacter();

        ret.accountid = this.getAccountID();
        ret.id = this.getId();
        ret.name = this.getName();
        ret.gender = this.getGender();
        ret.skinColor = this.getSkinColor();
        ret.face = this.getFace();
        ret.hair = this.getHair();

        // skipping pets, probably unneeded here

        ret.level = this.getLevel();
        ret.job = this.getJob();
        ret.str = this.getStr();
        ret.dex = this.getDex();
        ret.int_ = this.getInt();
        ret.luk = this.getLuk();
        ret.hp = this.getHp();
        ret.setMaxHp(this.getMaxHp());
        ret.mp = this.getMp();
        ret.setMaxMp(this.getMaxMp());
        ret.remainingAp = this.getRemainingAp();
        ret.setRemainingSp(this.getRemainingSps());
        ret.exp.set(this.getExp());
        ret.fame = this.getFame();
        ret.gachaexp.set(this.getGachaExp());
        ret.mapid = this.getMapId();
        ret.initialSpawnPoint = this.getInitialSpawnpoint();

        ret.inventory[MapleInventoryType.EQUIPPED.ordinal()] = this.getInventory(MapleInventoryType.EQUIPPED);

        ret.gmLevel = this.gmLevel();
        ret.world = this.getWorld();
        ret.playerRank = this.getPlayerRank();
        ret.rankMove = this.getRankMove();
        ret.jobRank = this.getJobRank();
        ret.jobRankMove = this.getJobRankMove();

        return ret;
    }

    private void loadCharSkillPoints(String[] skillPoints) {
        int[] sps = new int[skillPoints.length];
        for (int i = 0; i < skillPoints.length; i++) {
            sps[i] = Integer.parseInt(skillPoints[i]);
        }

        setRemainingSp(sps);
    }

    public int getRemainingSp() {
        return getRemainingSp(job.getId()); //default
    }

    public void updateRemainingSp(int remainingSp) {
        updateRemainingSp(remainingSp, GameConstants.getSkillBook(job.getId()));
    }

    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) throws SQLException {
        MapleCharacter ret = new MapleCharacter();
        ret.client = client;
        ret.id = charid;
        try (Connection con = DatabaseConnection.getConnection()) {
            final int mountexp;
            final int mountlevel;
            final int mounttiredness;
            final World wserv;

            try (PreparedStatement ps = con.prepareStatement("select characters.*, accounts.cheater from characters " +
                    "left join accounts on (accounts.id = characters.accountid) where characters.id = ?")) {
                ps.setInt(1, charid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Loading char failed (not found)");
                    }
                    ret.name = rs.getString("name");
                    ret.level = rs.getInt("level");
                    ret.fame = rs.getInt("fame");
                    ret.quest_fame = rs.getInt("fquest");
                    ret.str = rs.getInt("str");
                    ret.dex = rs.getInt("dex");
                    ret.int_ = rs.getInt("int");
                    ret.luk = rs.getInt("luk");
                    ret.exp.set(rs.getInt("exp"));
                    ret.gachaexp.set(rs.getInt("gachaexp"));
                    ret.hp = rs.getInt("hp");
                    ret.setMaxHp(rs.getInt("maxhp"));
                    ret.mp = rs.getInt("mp");
                    ret.setMaxMp(rs.getInt("maxmp"));
                    ret.hpMpApUsed = rs.getInt("hpMpUsed");
                    ret.hasMerchant = rs.getBoolean("HasMerchant");
                    ret.remainingAp = rs.getInt("ap");
                    ret.loadCharSkillPoints(rs.getString("sp").split(","));
                    ret.meso.set(rs.getInt("meso"));
                    ret.merchantmeso = rs.getInt("MerchantMesos");
                    ret.gmLevel = rs.getInt("gm");
                    ret.skinColor = MapleSkinColor.getById(rs.getInt("skincolor"));
                    ret.gender = rs.getInt("gender");
                    ret.job = MapleJob.getById(rs.getInt("job"));
                    ret.finishedDojoTutorial = rs.getBoolean("finishedDojoTutorial");
                    ret.vanquisherKills = rs.getInt("vanquisherKills");
                    ret.omokwins = rs.getInt("omokwins");
                    ret.omoklosses = rs.getInt("omoklosses");
                    ret.omokties = rs.getInt("omokties");
                    ret.matchcardwins = rs.getInt("matchcardwins");
                    ret.matchcardlosses = rs.getInt("matchcardlosses");
                    ret.matchcardties = rs.getInt("matchcardties");
                    ret.hair = rs.getInt("hair");
                    ret.face = rs.getInt("face");
                    ret.accountid = rs.getInt("accountid");
                    ret.mapid = rs.getInt("map");
                    ret.jailExpiration = rs.getLong("jailexpire");
                    ret.initialSpawnPoint = rs.getInt("spawnpoint");
                    ret.world = rs.getByte("world");
                    ret.playerRank = rs.getInt("playerRank");
                    ret.rankMove = rs.getInt("rankMove");
                    ret.jobRank = rs.getInt("jobRank");
                    ret.jobRankMove = rs.getInt("jobRankMove");
                    mountexp = rs.getInt("mountexp");
                    mountlevel = rs.getInt("mountlevel");
                    mounttiredness = rs.getInt("mounttiredness");
                    ret.guildid = rs.getInt("guildid");
                    ret.guildRank = rs.getInt("guildrank");
                    ret.allianceRank = rs.getInt("allianceRank");
                    ret.familyId = rs.getInt("familyId");
                    ret.bookCover = rs.getInt("monsterbookcover");
                    ret.monsterbook = new MonsterBook(ret);
                    ret.monsterbook.load(con);
                    ret.vanquisherStage = rs.getInt("vanquisherStage");
                    ret.ariantPoints = rs.getInt("ariantPoints");
                    ret.dojoPoints = rs.getInt("dojoPoints");
                    ret.dojoStage = rs.getInt("lastDojoStage");
                    ret.dataString = rs.getString("dataString");
                    ret.mgc = new MapleGuildCharacter(ret);
                    int buddyCapacity = rs.getInt("buddyCapacity");
                    ret.buddylist = new BuddyList(buddyCapacity);
                    ret.canRecvPartySearchInvite = rs.getBoolean("partySearch");
                    ret.ischeater = rs.getBoolean("cheater");
                    System.out.println(Calendar.getInstance().getTime().toString() + ":log in, " +
                            client.getSession().getRemoteAddress().toString() + "," + ret.accountid + "," + ret.id + "," + ret.name);

                    ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getByte("equipslots"));
                    ret.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getByte("useslots"));
                    ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getByte("setupslots"));
                    ret.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getByte("etcslots"));

                    byte sandboxCheck = 0x0;
                    for (Pair<Item, MapleInventoryType> item : ItemFactory.INVENTORY.loadItems(ret.id, !channelserver)) {
                        sandboxCheck |= item.getLeft().getFlag();

                        ret.getInventory(item.getRight()).addItemFromDB(item.getLeft());
                        Item itemz = item.getLeft();
                        if (itemz.getPetId() > -1) {
                            MaplePet pet = itemz.getPet();
                            if (pet != null && pet.isSummoned()) {
                                ret.addPet(pet);
                            }
                            continue;
                        }

                        MapleInventoryType mit = item.getRight();
                        if (mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)) {
                            Equip equip = (Equip) item.getLeft();
                            if (equip.getRingId() > -1) {
                                MapleRing ring = MapleRing.loadFromDb(equip.getRingId());
                                if (item.getRight().equals(MapleInventoryType.EQUIPPED)) {
                                    ring.equip();
                                }

                                ret.addPlayerRing(ring);
                            }
                        }
                    }
                    if ((sandboxCheck & ItemConstants.SANDBOX) == ItemConstants.SANDBOX) {
                        ret.setHasSandboxItem();
                    }

                    wserv = Server.getInstance().getWorld(ret.world);

                    ret.partnerId = rs.getInt("partnerId");
                    ret.marriageItemid = rs.getInt("marriageItemId");
                    if (ret.marriageItemid > 0 && ret.partnerId <= 0) {
                        ret.marriageItemid = -1;
                    } else if (ret.partnerId > 0 && wserv.getRelationshipId(ret.id) <= 0) {
                        ret.marriageItemid = -1;
                        ret.partnerId = -1;
                    }


                    NewYearCardRecord.loadPlayerNewYearCards(ret);

                    try (PreparedStatement ps3 = con.prepareStatement("SELECT petid FROM inventory_items WHERE characterid = ? AND petid > -1")) {
                        ps3.setInt(1, charid);
                        ResultSet rs3 = ps3.executeQuery();
                        while (rs3.next()) {
                            int petId = rs3.getInt("petid");

                            try (PreparedStatement ps2 = con.prepareStatement("SELECT itemid FROM pet_ignores WHERE petid = ?")) {
                                ps2.setInt(1, petId);

                                ret.resetExcluded(petId);

                                ResultSet rs2 = ps2.executeQuery();
                                while (rs2.next()) {
                                    ret.addExcluded(petId, rs2.getInt("itemid"));
                                }
                            }
                        }
                    }

                    ret.commitExcludedItems();

                    if (channelserver) {
                        MapleMapFactory mapFactory = client.getChannelServer().getMapFactory();
                        ret.map = mapFactory.getMap(ret.mapid);

                        if (ret.map == null) {
                            ret.map = mapFactory.getMap(100000000);
                        }
                        MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
                        if (portal == null) {
                            portal = ret.map.getPortal(0);
                            ret.initialSpawnPoint = 0;
                        }
                        ret.setPosition(portal.getPosition());
                        int partyid = rs.getInt("party");
                        MapleParty party = wserv.getParty(partyid);
                        if (party != null) {
                            ret.mpc = party.getMemberById(ret.id);
                            if (ret.mpc != null) {
                                ret.mpc = new MaplePartyCharacter(ret);
                                ret.party = party;
                            }
                        }
                        int messengerid = rs.getInt("messengerid");
                        int position = rs.getInt("messengerposition");
                        if (messengerid > 0 && position < 4 && position > -1) {
                            MapleMessenger messenger = wserv.getMessenger(messengerid);
                            if (messenger != null) {
                                ret.messenger = messenger;
                                ret.messengerposition = position;
                            }
                        }
                        ret.loggedIn = true;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("SELECT mapid, vip FROM trock_locations WHERE characterid = ? LIMIT 15")){
                ps.setInt(1, charid);
                try (ResultSet rs = ps.executeQuery()) {
                    byte v = 0;
                    byte r = 0;
                    while (rs.next()) {
                        if (rs.getInt("vip") == 1) {
                            ret.viptrockmaps.add(rs.getInt("mapid"));
                            v++;
                        } else {
                            ret.trockmaps.add(rs.getInt("mapid"));
                            r++;
                        }
                    }
                    while (v < 10) {
                        ret.viptrockmaps.add(999999999);
                        v++;
                    }
                    while (r < 5) {
                        ret.trockmaps.add(999999999);
                        r++;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("SELECT name, characterslots FROM accounts WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, ret.accountid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        MapleClient retClient = ret.getClient();

                        retClient.setAccountName(rs.getString("name"));
                        retClient.setCharacterSlots(rs.getByte("characterslots"));
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("SELECT area, info FROM area_info WHERE charid = ?")) {
                ps.setInt(1, ret.id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ret.area_info.put(rs.getShort("area"), rs.getString("info"));
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("SELECT name, info FROM event_stats WHERE characterid = ?")) {
                ps.setInt(1, ret.id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("name");
                        if (rs.getString("name").contentEquals("rescueGaga")) {
                            ret.events.put(name, new RescueGaga(rs.getInt("info")));
                        }
                    }
                }
            }

            ret.cashshop = new CashShop(ret.accountid, ret.id, ret.getJobType());
            ret.autoban = new AutobanManager(ret);
            try (PreparedStatement ps = con.prepareStatement("SELECT name, level FROM characters WHERE accountid = ? AND id != ? ORDER BY level DESC limit 1")) {
                ps.setInt(1, ret.accountid);
                ps.setInt(2, charid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ret.linkedName = rs.getString("name");
                        ret.linkedLevel = rs.getInt("level");
                    }
                }
            }

            if (channelserver) {
                Map<Integer, MapleQuestStatus> loadedQuestStatus = new LinkedHashMap<>();

                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM quest_status WHERE characterid = ?")) {
                    ps.setInt(1, charid);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            MapleQuest q = MapleQuest.getInstance(rs.getShort("quest"));
                            MapleQuestStatus status = new MapleQuestStatus(q, MapleQuestStatus.Status.getById(rs.getInt("status")));
                            long cTime = rs.getLong("time");
                            if (cTime > -1) {
                                status.setCompletionTime(cTime * 1000);
                            }

                            long eTime = rs.getLong("expires");
                            if (eTime > 0) {
                                status.setExpirationTime(eTime);
                            }

                            status.setForfeited(rs.getInt("forfeited"));
                            status.setCompleted(rs.getInt("completed"));
                            ret.quests.put(q.getId(), status);
                            loadedQuestStatus.put(rs.getInt("queststatusid"), status);
                        }
                    }
                }

                // opportunity for improvement on questprogress/medalmaps calls to DB
                try (PreparedStatement pse = con.prepareStatement("SELECT * FROM quest_progress WHERE characterid = ?")) {
                    pse.setInt(1, charid);
                    try (ResultSet rsProgress = pse.executeQuery()) {
                        while (rsProgress.next()) {
                            MapleQuestStatus status = loadedQuestStatus.get(rsProgress.getInt("queststatusid"));
                            if (status != null) {
                                status.setProgress(rsProgress.getInt("progressid"), rsProgress.getString("progress"));
                            }
                        }
                    }
                }

                try (PreparedStatement pse = con.prepareStatement("SELECT * FROM medal_maps WHERE characterid = ?")) {
                    pse.setInt(1, charid);
                    try (ResultSet rsMedalMaps = pse.executeQuery()) {
                        while (rsMedalMaps.next()) {
                            MapleQuestStatus status = loadedQuestStatus.get(rsMedalMaps.getInt("queststatusid"));
                            if (status != null) {
                                status.addMedalMap(rsMedalMaps.getInt("mapid"));
                            }
                        }
                    }
                }

                loadedQuestStatus.clear();

                try (PreparedStatement ps = con.prepareStatement("SELECT skillid, skilllevel, masterlevel, expiration FROM skills WHERE characterid = ?")) {
                    ps.setInt(1, charid);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ret.skills.put(SkillFactory.getSkill(rs.getInt("skillid")), new SkillEntry(rs.getByte("skilllevel"), rs.getInt("masterlevel"), rs.getLong("expiration")));
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement("SELECT SkillID, StartTime, length FROM cooldowns WHERE charid = ?")) {
                    ps.setInt(1, ret.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        long curTime = Server.getInstance().getCurrentTime();
                        while (rs.next()) {
                            final int skillid = rs.getInt("SkillID");
                            final long length = rs.getLong("length"), startTime = rs.getLong("StartTime");
                            if (skillid != 5221999 && (length + startTime < curTime)) {
                                continue;
                            }
                            ret.giveCoolDowns(skillid, startTime, length);
                        }
                    }
                }

                Statements.Delete.from("cooldowns").where("charid", ret.getId()).execute(con);

                Map<MapleDisease, Pair<Long, MobSkill>> loadedDiseases = new LinkedHashMap<>();
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM player_diseases WHERE charid = ?")) {
                    ps.setInt(1, ret.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            final MapleDisease disease = MapleDisease.ordinal(rs.getInt("disease"));
                            if (disease == MapleDisease.NULL) {
                                continue;
                            }

                            final int skillid = rs.getInt("mobskillid"), skilllv = rs.getInt("mobskilllv");
                            final long length = rs.getInt("length");

                            MobSkill ms = MobSkillFactory.getMobSkill(skillid, skilllv);
                            if (ms != null) {
                                loadedDiseases.put(disease, new Pair<>(length, ms));
                            }
                        }
                    }
                }

                Statements.Delete.from("player_diseases").where("charid", ret.getId()).execute(con);

/*                if (!loadedDiseases.isEmpty()) {
                    Server.getInstance().getPlayerBuffStorage().addDiseasesToStorage(ret.id, loadedDiseases);
                }*/
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM skill_macros WHERE characterid = ?")) {
                    ps.setInt(1, charid);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int position = rs.getInt("position");
                            SkillMacro macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), position);
                            ret.skillMacros[position] = macro;
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement("SELECT key, type, action FROM keymap WHERE characterid = ?")) {
                    ps.setInt(1, charid);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int key = rs.getInt("key");
                            int type = rs.getInt("type");
                            int action = rs.getInt("action");
                            ret.keymap.put(Integer.valueOf(key), new MapleKeyBinding(type, action));
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement("SELECT locationtype, map, portal FROM saved_locations WHERE characterid = ?")) {
                    ps.setInt(1, charid);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ret.savedLocations[SavedLocationType.valueOf(rs.getString("locationtype")).ordinal()] = new SavedLocation(rs.getInt("map"), rs.getInt("portal"));
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement("SELECT characterid_to, \"when\" FROM fame_log WHERE characterid = ? AND date_part('day', \"when\" - now()) < 30")) {
                    ps.setInt(1, charid);
                    try (ResultSet rs = ps.executeQuery()) {
                        ret.lastfametime = 0;
                        ret.lastmonthfameids = new ArrayList<>(31);
                        while (rs.next()) {
                            ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                            ret.lastmonthfameids.add(Integer.valueOf(rs.getInt("characterid_to")));
                        }
                    }
                }

                String achsql = "SELECT * FROM world_tour WHERE charid = ?";
                try (PreparedStatement ps = con.prepareStatement(achsql)) {
                    ps.setInt(1, charid);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ret.finishedWorldTour.add(rs.getString("worldtourid"));
                        }
                    }
                }

                String bpqsql = "SELECT * FROM boss_quest WHERE id = ?";
                try (PreparedStatement ps = con.prepareStatement(bpqsql)){
                    ps.setInt(1, charid);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ret.bossQuest.addPoints(charid, rs.getInt("points"));
                            ret.bossQuest.addAttempts(charid, rs.getInt("attempts"));
                        }
                    }
                }

                ret.buddylist.loadFromDb(charid);
                ret.storage = MapleStorage.loadOrCreateFromDB(ret.accountid, ret.world);

                int startHp = ret.hp, startMp = ret.mp;
                ret.reapplyLocalStats();
                ret.changeHpMp(startHp, startMp, true);
                //ret.resetBattleshipHp();
            }
            int mountid = ret.getJobType() * 10000000 + 1004;
            if (ret.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18) != null) {
                ret.maplemount = new MapleMount(ret, ret.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18).getItemId(), mountid);
            } else {
                ret.maplemount = new MapleMount(ret, 0, mountid);
            }
            ret.maplemount.setExp(mountexp);
            ret.maplemount.setLevel(mountlevel);
            ret.maplemount.setTiredness(mounttiredness);
            ret.maplemount.setActive(false);

            return ret;

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void reloadQuestExpirations() {
        for (MapleQuestStatus mqs : quests.values()) {
            if (mqs.getExpirationTime() > 0) {
                questTimeLimit2(mqs.getQuest(), mqs.getExpirationTime());
            }
        }
    }

    public static String makeMapleReadable(String in) {
        String i = in.replace('I', 'i');
        i = i.replace('l', 'L');
        i = i.replace("rn", "Rn");
        i = i.replace("vv", "Vv");
        i = i.replace("VV", "Vv");

        return i;
    }

   /* private static class MapleBuffStatValueHolder {

        public MapleStatEffect effect;
        public long startTime;
        public int value;
        public boolean bestApplied;

        public MapleBuffStatValueHolder(MapleStatEffect effect, long startTime, int value) {
            super();
            this.effect = effect;
            this.startTime = startTime;
            this.value = value;
            this.bestApplied = false;
        }
    }*/

    public void message(String m) {
        dropMessage(5, m);
    }

    public void yellowMessage(String m) {
        announce(MaplePacketCreator.sendYellowTip(m));
    }

    public void updateQuestMobCount(int id) {
        // It seems nexon uses monsters that don't exist in the WZ (except string) to merge multiple mobs together for these 3 monsters.
        // We also want to run mobKilled for both since there are some quest that don't use the updated ID...
        if (id == 1110100 || id == 1110130) {
            updateQuestMobCount(9101000);
        } else if (id == 2230101 || id == 2230131) {
            updateQuestMobCount(9101001);
        } else if (id == 1140100 || id == 1140130) {
            updateQuestMobCount(9101002);
        }

        int lastQuestProcessed = 0;
        try {
            synchronized (quests) {
                for (MapleQuestStatus q : quests.values()) {
                    lastQuestProcessed = q.getQuest().getId();
                    if (q.getStatus() == MapleQuestStatus.Status.COMPLETED || q.getQuest().canComplete(this, null)) {
                        continue;
                    }
                    String progress = q.getProgress(id);
                    if (!progress.isEmpty() && Integer.parseInt(progress) >= q.getQuest().getMobAmountNeeded(id)) {
                        continue;
                    }
                    if (q.progress(id)) {
                        client.announce(MaplePacketCreator.updateQuest(q, false));
                    }
                }
            }
        } catch (Exception e) {
            FilePrinter.printError(FilePrinter.EXCEPTION_CAUGHT, e, "MapleCharacter.mobKilled. CID: " + this.id + " last Quest Processed: " + lastQuestProcessed);
        }
    }

    public void mount(int id, int skillid) {
        maplemount = new MapleMount(this, id, skillid);
    }

    private void playerDead() {
        if (this.getMap().isCPQMap()) {
            int losing = getMap().getDeathCP();
            if (getCP() < losing) {
                losing = getCP();
            }
            getMap().broadcastMessage(MonsterCarnivalPacket.Packet.onProcessForDeath(getName(), losing, getTeam()));
            gainCP(-losing);
            return;
        }

        cancelAllBuffs(false);
        //dispelDebuffs();
        lastDeathtime = Server.getInstance().getCurrentTime();

        EventInstanceManager eim = getEventInstance();
        if (eim != null) {
            eim.playerKilled(this);
        }
        int[] charmID = {5130000, 4031283, 4140903};
        int possesed = 0;
        int i;
        for (i = 0; i < charmID.length; i++) {
            int quantity = getItemQuantity(charmID[i], false);
            if (possesed == 0 && quantity > 0) {
                possesed = quantity;
                break;
            }
        }
        if (possesed > 0 && !map.isDojoMap()) {
            message("You have used one safety charm, so your EXP points have not been decreased.");
            MapleInventoryManipulator.removeById(client, ItemConstants.getInventoryType(charmID[i]), charmID[i], 1, true, false);
            usedSafetyCharm = true;
        } else if (map.isDojoMap()) {
            this.dojoStage = 0;
        } else if (getJob() != MapleJob.BEGINNER) { //Hmm...
            int wholeEXP = ExpTable.INSTANCE.getExpNeededForLevel(getLevel());
            int expLoss = wholeEXP / 10;
            if (expLoss > this.getExp()) {
                this.gainExp(-this.getExp(), false, false);
            } else {
                this.gainExp(-expLoss, false, false);
            }
        }
        if (getBuffedValue(MapleBuffStat.MORPH) != null) {
            cancelEffectFromBuffStat(MapleBuffStat.MORPH);
        }

        if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        }

        unsitChairInternal();
        client.announce(WvsContext.Packet.enableActions());
    }

    private void unsitChairInternal() {
        int chairid = chair.get();
        if (chairid >= 0) {
            setChair(-1);
/*            if (unregisterChairBuff()) {
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignChairSkillEffect(this.getId()), false);
            }*/

            getMap().broadcastMessage(this, UserRemote.Packet.showChair(this.getId(), 0), false);
        }

        announce(UserLocal.Packet.onSitResult(-1));
        cancelFishingTask();
    }

    public void sitChair(int itemId) {
        if (client.tryacquireClient()) {
            try {
                if (this.isLoggedinWorld()) {
                    if (itemId >= 1000000) {    // sit on item chair
                        if (chair.get() < 0) {
                            if (itemId == 3011000 && getMapId() == 741000200) {
                                startFishingTask();
                            }
                            setChair(itemId);
                            getMap().broadcastMessage(this, UserRemote.Packet.showChair(this.getId(), itemId), false);
                        }
                        announce(WvsContext.Packet.enableActions());
                    } else if (itemId >= 0) {    // sit on map chair
                        if (chair.get() < 0) {
                            setChair(itemId);
                            /*if (registerChairBuff()) {
                                getMap().broadcastMessage(this, MaplePacketCreator.giveForeignChairSkillEffect(this.getId()), false);
                            }*/
                            announce(UserLocal.Packet.onSitResult(itemId));
                        }
                    } else {    // stand up
                        unsitChairInternal();
                    }
                }
            } finally {
                client.releaseClient();
            }
        }
    }

    private void setChair(int chair) {
        this.chair.set(chair);
    }

    public void respawn(int returnMap) {
        respawn(null, returnMap);    // unspecified EIM, don't force EIM unregister in this case
    }

    public void respawn(EventInstanceManager eim, int returnMap) {
        if (eim != null) {
            eim.unregisterPlayer(this);    // some event scripts uses this...
        }
        changeMap(returnMap);

        cancelAllBuffs(false);  // thanks Oblivium91 for finding out players still could revive in area and take damage before returning to town
        if (usedSafetyCharm) {  // thanks kvmba for noticing safety charm not providing 30% HP/MP
            addMPHP((int) Math.ceil(this.getClientMaxHp() * 0.3), (int) Math.ceil(this.getClientMaxMp() * 0.3));
        } else {
            updateHp(50);
        }
        setStance(0);
    }

    private void prepareDragonBlood(final MapleStatEffect bloodEffect) {
        if (dragonBloodSchedule != null) {
            dragonBloodSchedule.cancel(false);
        }
        dragonBloodSchedule = TimerManager.getInstance().register(() -> {
            if (awayFromWorld.get()) {
                return;
            }

            addHP(-bloodEffect.getX());
            announce(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
            getMap().broadcastMessage(MapleCharacter.this, UserRemote.Packet.showBuffEffect(getId(), bloodEffect.getSourceId(), 5), false);
        }, 4000, 4000);
    }

    private void recalcEquipStats() {
        if (equipchanged) {
            equipmaxhp = 0;
            equipmaxmp = 0;
            equipdex = 0;
            equipint_ = 0;
            equipstr = 0;
            equipluk = 0;
            equipmagic = 0;
            equipwatk = 0;
            //equipspeed = 0;
            //equipjump = 0;

            for (Item item : getInventory(MapleInventoryType.EQUIPPED)) {
                Equip equip = (Equip) item;
                equipmaxhp += equip.getHp();
                equipmaxmp += equip.getMp();
                equipdex += equip.getDex();
                equipint_ += equip.getInt();
                equipstr += equip.getStr();
                equipluk += equip.getLuk();
                equipmagic += equip.getMatk() + equip.getInt();
                equipwatk += equip.getWatk();
                //equipspeed += equip.getSpeed();
                //equipjump += equip.getJump();
            }

            equipchanged = false;
        }

        localmaxhp += equipmaxhp;
        localmaxmp += equipmaxmp;
        localdex += equipdex;
        localint_ += equipint_;
        localstr += equipstr;
        localluk += equipluk;
        localmagic += equipmagic;
        localwatk += equipwatk;
    }

    private void reapplyLocalStats() {
        effLock.lock();
        chrLock.lock();
        statWlock.lock();
        try {
            localmaxhp = getMaxHp();
            localmaxmp = getMaxMp();
            localdex = getDex();
            localint_ = getInt();
            localstr = getStr();
            localluk = getLuk();
            localmagic = localint_;
            localwatk = 0;
            localchairrate = -1;

            recalcEquipStats();
        
            localmagic = Math.min(localmagic, 2000);

            Integer hbhp = getBuffedValue(MapleBuffStat.HYPERBODYHP);
            if (hbhp != null) {
                localmaxhp += (hbhp.doubleValue() / 100) * localmaxhp;
            }
            Integer hbmp = getBuffedValue(MapleBuffStat.HYPERBODYMP);
            if (hbmp != null) {
                localmaxmp += (hbmp.doubleValue() / 100) * localmaxmp;
            }

            localmaxhp = Math.min(30000, localmaxhp);
            localmaxmp = Math.min(30000, localmaxmp);

            MapleStatEffect combo = getBuffEffect(MapleBuffStat.ARAN_COMBO);
            if (combo != null) {
                localwatk += combo.getX();
            }

            if (energybar == 15000) {
                PlayerSkill energycharge = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.ENERGY_CHARGE) : SkillFactory.getSkill(Marauder.ENERGY_CHARGE);
                MapleStatEffect ceffect = energycharge.getEffect(getSkillLevel(energycharge));
                localwatk += ceffect.getWatk();
            }

            Integer mwarr = getBuffedValue(MapleBuffStat.MAPLE_WARRIOR);
            if (mwarr != null) {
                localstr += getStr() * mwarr / 100;
                localdex += getDex() * mwarr / 100;
                localint_ += getInt() * mwarr / 100;
                localluk += getLuk() * mwarr / 100;
            }
            if (job.isA(MapleJob.BOWMAN)) {
                PlayerSkill expert = null;
                if (job.isA(MapleJob.MARKSMAN)) {
                    expert = SkillFactory.getSkill(3220004);
                } else if (job.isA(MapleJob.BOWMASTER)) {
                    expert = SkillFactory.getSkill(3120005);
                }
                if (expert != null) {
                    int boostLevel = getSkillLevel(expert);
                    if (boostLevel > 0) {
                        localwatk += expert.getEffect(boostLevel).getX();
                    }
                }
            }

            Integer watkbuff = getBuffedValue(MapleBuffStat.WATK);
            if (watkbuff != null) {
                localwatk += watkbuff.intValue();
            }
            Integer matkbuff = getBuffedValue(MapleBuffStat.MATK);
            if (matkbuff != null) {
                localmagic += matkbuff.intValue();
            }

            /*
            Integer speedbuff = getBuffedValue(MapleBuffStat.SPEED);
            if (speedbuff != null) {
                localspeed += speedbuff.intValue();
            }
            Integer jumpbuff = getBuffedValue(MapleBuffStat.JUMP);
            if (jumpbuff != null) {
                localjump += jumpbuff.intValue();
            }
            */

            Integer blessing = getSkillLevel(10000000 * getJobType() + 12); // blessing of fairy
            if (blessing > 0) {
                localwatk += blessing;
                localmagic += blessing * 2;
            }

            if (job.isA(MapleJob.THIEF) || job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.PIRATE) || job.isA(MapleJob.NIGHTWALKER1) || job.isA(MapleJob.WINDARCHER1)) {
                Item weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
                if (weapon_item != null) {
                    MapleWeaponType weapon = ii.getWeaponType(weapon_item.getItemId());
                    boolean bow = weapon == MapleWeaponType.BOW;
                    boolean crossbow = weapon == MapleWeaponType.CROSSBOW;
                    boolean claw = weapon == MapleWeaponType.CLAW;
                    boolean gun = weapon == MapleWeaponType.GUN;
                    if (bow || crossbow || claw || gun) {
                        // Also calc stars into this.
                        MapleInventory inv = getInventory(MapleInventoryType.USE);
                        for (short i = 1; i <= inv.getSlotLimit(); i++) {
                            Item item = inv.getItem(i);
                            if (item != null) {
                                if ((claw && ItemConstants.isThrowingStar(item.getItemId())) || (gun && ItemConstants.isBullet(item.getItemId())) || (bow && ItemConstants.isArrowForBow(item.getItemId())) || (crossbow && ItemConstants.isArrowForCrossBow(item.getItemId()))) {
                                    if (item.getQuantity() > 0) {
                                        // Finally there!
                                        localwatk += ii.getWatkForProjectile(item.getItemId());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                // Add throwing stars to dmg.
            }
        } finally {
            statWlock.unlock();
            chrLock.unlock();
            effLock.unlock();
        }
    }

    private List<Pair<MapleStat, Integer>> recalcLocalStats() {
        effLock.lock();
        chrLock.lock();
        statWlock.lock();
        try {
            List<Pair<MapleStat, Integer>> hpmpupdate = new ArrayList<>(2);

            reapplyLocalStats();
            return hpmpupdate;
            
        } finally {
            statWlock.unlock();
            chrLock.unlock();
            effLock.unlock();
        }
    }

    private void updateLocalStats() {
        effLock.lock();
        statWlock.lock();
        try {
            int oldmaxhp = localmaxhp;
            List<Pair<MapleStat, Integer>> hpmpupdate = recalcLocalStats();
            enforceMaxHpMp();

            if (!hpmpupdate.isEmpty()) {
                client.announce(WvsContext.Packet.updatePlayerStats(hpmpupdate, true, this));
            }

            if (oldmaxhp != localmaxhp) {
                updatePartyMemberHP();
            }
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public void receivePartyMemberHP() {
        prtLock.lock();
        try {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapId() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = Server.getInstance().getWorld(world).getChannel(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        client.announce(UserRemote.Packet.onReceiveHp(other.getId(), other.getHp(), other.getCurrentMaxHp()));
                    }
                }
            }
        }
        } finally {
            prtLock.unlock();
        }
    }

    public void removeAllCooldownsExcept(int id, boolean packet) {
        for (MapleCoolDownValueHolder mcvh : coolDowns.values()) {

            if (mcvh.skillId == id) {
                continue; //just cuz lesser nested shiets.
            }

            MapleCoolDownValueHolder cd = coolDowns.remove(mcvh.skillId);
            cd.timer.cancel(false);

            if (packet) {
                client.announce(UserLocal.Packet.skillCooldown(mcvh.skillId, 0));
            }
        }
    }

    public static void removeAriantRoom(int room) {
        ariantroomleader[room] = "";
        ariantroomslot[room] = 0;
    }

    public void removeCooldown(int skillId) {
        if (this.coolDowns.containsKey(skillId)) {
            MapleCoolDownValueHolder cd = this.coolDowns.remove(skillId);
            cd.timer.cancel(false);
        }
    }

    public void removePet(MaplePet pet, boolean shift_left) {
        petLock.lock();
        try {
            int slot = -1;
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    if (pets[i].getUniqueId() == pet.getUniqueId()) {
                        pets[i] = null;
                        slot = i;
                        break;
                    }
                }
            }
            if (shift_left) {
                if (slot > -1) {
                    for (int i = slot; i < 3; i++) {
                        if (i != 2) {
                            pets[i] = pets[i + 1];
                        } else {
                            pets[i] = null;
                        }
                    }
                }
            }
        } finally {
            petLock.unlock();
        }
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.remove(mo);
    }

    public synchronized void resetStats() {
        effLock.lock();
        statWlock.lock();
        try {
            int tap = remainingAp + str + dex + int_ + luk, tsp = 1;
            int tstr = 4, tdex = 4, tint = 4, tluk = 4;

            switch (job.getId()) {
                case 100, 1100, 2100 -> {
                    tstr = 35;
                    tsp += ((getLevel() - 10) * 3);
                }
                case 200, 1200 -> {
                    tint = 20;
                    tsp += ((getLevel() - 8) * 3);
                }
                case 300, 1300, 400, 1400 -> {
                    tdex = 25;
                    tsp += ((getLevel() - 10) * 3);
                }
                case 500, 1500 -> {
                    tdex = 20;
                    tsp += ((getLevel() - 10) * 3);
                }
            }

            tap -= tstr;
            tap -= tdex;
            tap -= tint;
            tap -= tluk;

            if (tap >= 0) {
                updateStrDexIntLukSp(tstr, tdex, tint, tluk, tap, tsp, GameConstants.getSkillBook(job.getId()));
            } else {
                FilePrinter.print(FilePrinter.EXCEPTION_CAUGHT, name + " tried to get their stats reseted, without having enough AP available.");
            }
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public void resetBattleshipHp() {
        int bshipLevel = Math.max(getLevel() - 120, 0);  // thanks alex12 for noticing battleship HP issues for low-level players
        this.battleshipHp = 4000 * getSkillLevel(SkillFactory.getSkill(Corsair.BATTLE_SHIP)) + (bshipLevel * 2000);
    }

    public void resetEnteredScript() {
        entered.remove(map.getId());
    }

    public void resetEnteredScript(int mapId) {
        entered.remove(mapId);
    }

    public void resetEnteredScript(String script) {
        for (int mapId : entered.keySet()) {
            if (entered.get(mapId).equals(script)) {
                entered.remove(mapId);
            }
        }
    }

    public synchronized void saveCooldowns() {
        List<PlayerCoolDownValueHolder> listcd = getAllCooldowns();

        if (!listcd.isEmpty()) {
            try (Connection con = DatabaseConnection.getConnection()) {
                Statements.Delete.from("cooldowns").where("charid", id).execute(con);
                Statements.BatchInsert statement = new Statements.BatchInsert("cooldowns");

                for (PlayerCoolDownValueHolder cooling : listcd) {
                    statement.add("charid", getId());
                    statement.add("skillid", cooling.skillId);
                    statement.add("starttime", cooling.startTime);
                    statement.add("length", cooling.length);
                }

                statement.execute(con);
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        Map<MapleDisease, Pair<Long, MobSkill>> listds = getAllDiseases();
        if (!listds.isEmpty()) {
            try (Connection con = DatabaseConnection.getConnection()) {
                Statements.Delete.from("player_diseases").where("charid", id).execute(con);

                Statements.BatchInsert statement = new Statements.BatchInsert("player_diseases");

                for (Entry<MapleDisease, Pair<Long, MobSkill>> e : listds.entrySet()) {
                    MobSkill ms = e.getValue().getRight();
                    statement.add("charid", getId());
                    statement.add("disease", e.getKey().ordinal());
                    statement.add("mobskillid", ms.getSkillId());
                    statement.add("mobskilllv", ms.getSkillLevel());
                    statement.add("length", e.getValue().getLeft().intValue());
                }

                statement.execute(con);
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    //=====================================================================================================================================
    // Listeners
    //=====================================================================================================================================
    /*
     * Adds damage listener to the character
     * @param	listener	methods for total damage done and amount of damage from this single evnet
     */
    public void addDamageListener(DamageListener listener) {
        damage_listeners.add(listener);
    }

    /*
     * Returns all damage listeners on the character
     */
    public List<DamageListener> getDamageListeners() {
        return damage_listeners;
    }

    /*
     * Adds a drop listener to the character
     * @param	listener	contains methods dealing with handling a DropEvent
     */
    //public void addDropListener(DropListener listener) {
        //this.drop_listeners.add(listener);
    //}

    /**
     * Returns list of DropListeners
     */
    //public List<DropListener> getDropListeners() {
        //return this.drop_listeners;
    //}

    /**
     * DamageEvents are sent here to be broadcasted to listeners
     *
     * @param	event	that contains information about damage done during an event
     * or to a monster
     */
    public void updateDamageListeners(DamageEvent event) {
        //updatePetListeners(new PetEvent(event));
        if (damage_listeners != null) {
            for (DamageListener listener : getDamageListeners()) {
                listener.update(event);
            }
        }
    }

    public void updateMobKilledListeners(MobKilledEvent event) {
        //updatePetListeners(new PetEvent(event));
        if (mob_killed_listeners != null)
            for (MobKilledListener listener : getMobKilledListeners())
                listener.update(event);
    }

    public ArrayList<MobKilledListener> getMobKilledListeners() {
        return this.mob_killed_listeners;
    }

    /*public void updateDropListeners(DropEvent event) {
        //updatePetListeners(new PetEvent(event));
        if (drop_listeners != null) {
            for (DropListener listener : getDropListeners()) {
                listener.update(event);
            }
        }
    }

    /**
     * PetListeners are updated here
     *
     * @param PetEvent contains information each PetDonorFeature can react to
     */
   /* public void updatePetListeners(PetEvent event) {
        pets.stream().filter(pet -> pet != null && pet.getDonorFeature() != null).forEach(
                pet -> pet.getDonorFeature().update(event));
    }*/

    //==================================================================================================================================
    // End of listeners
    //==================================================================================================================================

    public void saveGuildStatus() {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, allianceRank = ? WHERE id = ?")) {
                ps.setInt(1, guildid);
                ps.setInt(2, guildRank);
                ps.setInt(3, allianceRank);
                ps.setInt(4, id);
                ps.executeUpdate();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void saveLocationOnWarp() {  // suggestion to remember the map before warp command thanks to Lei
        MaplePortal closest = map.findClosestPortal(getPosition());
        int curMapid = getMapId();

        for (int i = 0; i < savedLocations.length; i++) {
            if (savedLocations[i] == null) {
                savedLocations[i] = new SavedLocation(curMapid, closest != null ? closest.getId() : 0);
            }
        }
    }

    public void saveLocation(String type) {
        MaplePortal closest = map.findClosestPortal(getPosition());
        savedLocations[SavedLocationType.fromString(type).ordinal()] = new SavedLocation(getMapId(), closest != null ? closest.getId() : 0);
    }

    public final boolean insertNewChar(CharacterFactoryRecipe recipe) {
        str = recipe.getStr();
        dex = recipe.getDex();
        int_ = recipe.getInt();
        luk = recipe.getLuk();
        setMaxHp(recipe.getMaxHp());
        setMaxMp(recipe.getMaxMp());
        hp = maxhp;
        mp = maxmp;
        level = recipe.getLevel();
        remainingAp = recipe.getRemainingAp();
        remainingSp[GameConstants.getSkillBook(job.getId())] = recipe.getRemainingSp();
        mapid = recipe.getMap();
        meso.set(recipe.getMeso());

        List<Pair<PlayerSkill, Integer>> startingSkills = recipe.getStartingSkillLevel();
        for (Pair<PlayerSkill, Integer> skEntry : startingSkills) {
            PlayerSkill skill = skEntry.getLeft();
            this.changeSkillLevel(skill, skEntry.getRight().byteValue(), skill.getMaxLevel(), -1);
        }

        List<Pair<Item, MapleInventoryType>> itemsWithType = recipe.getStartingItems();
        for (Pair<Item, MapleInventoryType> itEntry : itemsWithType) {
            this.getInventory(itEntry.getRight()).addItem(itEntry.getLeft());
        }

        this.events.put("rescueGaga", new RescueGaga(0));

        try (Connection con = DatabaseConnection.getConnection()) {
            try {
                con.setAutoCommit(false);
                Statements.Insert statement = new Statements.Insert("characters");
                statement.add("skincolor", skinColor.getId());
                statement.add("gender", gender);
                statement.add("job", getJob().getId());
                statement.add("hair", hair);
                statement.add("face", face);
                statement.add("map", mapid);
                statement.add("spawnpoint", 0);
                statement.add("accountid", accountid);
                statement.add("name", name);
                statement.add("world", world);

                this.id = statement.execute(con);

                if (this.id == -1) {
                    FilePrinter.printError(FilePrinter.INSERT_CHAR, "Error trying to insert " + name);
                    return false;
                }

                // Select a keybinding method
                int[] selectedKey;
                int[] selectedType;
                int[] selectedAction;

                selectedKey = GameConstants.getCustomKey(false);
                selectedType = GameConstants.getCustomType(false);
                selectedAction = GameConstants.getCustomAction(false);

                try (PreparedStatement ps = con.prepareStatement("INSERT INTO keymap (characterid, key, type, action) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, id);
                    for (int i = 0; i < selectedKey.length; i++) {
                        ps.setInt(2, selectedKey[i]);
                        ps.setInt(3, selectedType[i]);
                        ps.setInt(4, selectedAction[i]);
                        ps.execute();
                    }
                }

                itemsWithType = new ArrayList<>();
                for (MapleInventory iv : inventory) {
                    for (Item item : iv.list()) {
                        itemsWithType.add(new Pair<>(item, iv.getType()));
                    }
                }

                ItemFactory.INVENTORY.saveItems(itemsWithType, id, con);

                if (!skills.isEmpty()) {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration) VALUES (?, ?, ?, ?, ?)")) {
                        ps.setInt(1, id);
                        for (Entry<PlayerSkill, SkillEntry> skill : skills.entrySet()) {
                            ps.setInt(2, skill.getKey().getId());
                            ps.setInt(3, skill.getValue().skillevel);
                            ps.setInt(4, skill.getValue().masterlevel);
                            ps.setLong(5, skill.getValue().expiration);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Throwable t) {
            FilePrinter.printError(FilePrinter.INSERT_CHAR, t, "Error creating " + name + " Level: " + level + " Job: " + job.getId());
        }
        return false;
    }

    public void saveCharToDB() {
        if (ServerConstants.USE_AUTOSAVE) {
            ThreadManager.getInstance().newTask(() -> saveCharToDB(true));  //spawns a new thread to deal with this
        } else {
            saveCharToDB(true);
        }
    }

    private int getAbsoluteMap() {
        if (map == null) {
            return mapid;
        } else if (map.getForcedReturnId() != 999999999) {
            return map.getForcedReturnId();
        } else if (getHp() < 1) {
            return map.getReturnMapId();
        } else {
            return map.getId();
        }
    }

    private void saveCharacter(Connection con) throws SQLException {
        Statements.Update statement = Statements.Update("characters");
        statement.cond("id", id);

        effLock.lock();
        statWlock.lock();

        try {
            statement.set("level", level);
            statement.set("fame", fame);
            statement.set("str", str);
            statement.set("dex", dex);
            statement.set("luk", luk);
            statement.set("int", int_);
            statement.set("exp", Math.abs(exp.get()));
            statement.set("hp", hp);
            statement.set("mp", mp);
            statement.set("maxhp", maxhp);
            statement.set("maxmp", maxmp);
            statement.set("ap", remainingAp);
            statement.set("sp",
                    Arrays.stream(remainingSp)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.joining(",")));
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }

        statement.set("skincolor", skinColor.getId());
        statement.set("gender", gender);
        statement.set("job", job.getId());
        statement.set("hair", hair);
        statement.set("face", face);

        //statement.add("gm", gmLevel);

        statement.set("map", getAbsoluteMap());
        statement.set("meso", meso.get());
        statement.set("hpMpUsed", hpMpApUsed);

        if (map != null && map.getId() != 610020000 && map.getId() != 610020001 && map.findClosestPlayerSpawnpoint(getPosition()) != null) {  // reset to first spawnpoint on those maps
            statement.set("spawnpoint", map.findClosestPlayerSpawnpoint(getPosition()).getId());
        } else {
            statement.set("spawnpoint", 0);
        }

        prtLock.lock();

        try {
            if (party != null) {
                statement.set("party", party.getId());
            } else {
                statement.set("party", -1);
            }
        } finally {
            prtLock.unlock();
        }

        statement.set("buddycapacity", buddylist.getCapacity());

        if (messenger != null) {
            statement.set("messengerid", messenger.getId());
            statement.set("messengerposition", messengerposition);
        } else {
            statement.set("messengerid", 0);
            statement.set("messengerposition", 0);
        }

        if (maplemount != null) {
            statement.set("mountlevel", maplemount.getLevel());
            statement.set("mountexp", maplemount.getExp());
            statement.set("mounttiredness", maplemount.getTiredness());
        } else {
            statement.set("mountlevel", 1);
            statement.set("mountexp", 0);
            statement.set("mounttiredness", 0);
        }

        statement.set("equipslots", getSlots(1));
        statement.set("useslots", getSlots(2));
        statement.set("setupslots", getSlots(3));
        statement.set("etcslots", getSlots(4));
        statement.set("monsterbookcover", bookCover);
        statement.set("vanquisherstage", vanquisherStage);
        statement.set("dojopoints", dojoPoints);
        statement.set("lastdojostage", dojoStage);
        statement.set("finisheddojotutorial", finishedDojoTutorial);
        statement.set("vanquisherkills", vanquisherKills);
        statement.set("matchcardwins", matchcardwins);
        statement.set("matchcardlosses", matchcardlosses);
        statement.set("matchcardties", matchcardties);
        statement.set("omokwins", omokwins);
        statement.set("omoklosses", omoklosses);
        statement.set("omokties", omokties);
        statement.set("datastring", dataString);
        statement.set("fquest", quest_fame);
        statement.set("jailexpire", jailExpiration);
        statement.set("partnerid", partnerId);
        statement.set("marriageitemid", marriageItemid);
        statement.set("ariantpoints", ariantPoints);
        statement.set("partysearch", canRecvPartySearchInvite);

        if (statement.execute_keys(con) < 1) {
            throw new RuntimeException("Character not in database (" + id + ")");
        }
    }

    public void savePetIgnores(Connection con) throws SQLException {
        for (Entry<Integer, Set<Integer>> es : getExcluded().entrySet()) {    // this set is already protected

            Statements.Delete.from("pet_ignores").where("petid", es.getKey()).execute(con);

            Statements.BatchInsert statement = new Statements.BatchInsert("pet_ignores");
            for (Integer x : es.getValue()) {
                statement.add("petid", es.getKey());
                statement.add("itemid", x);
            }
            statement.execute(con);
        }
    }

    public void saveKeymap(Connection con) throws SQLException {
        Statements.Delete.from("keymap").where("characterid", id).execute(con);

        Statements.BatchInsert statement = new Statements.BatchInsert("keymap");

        for (Entry<Integer, MapleKeyBinding> keybinding : Collections.unmodifiableSet(keymap.entrySet())) {
            statement.add("characterid", id);
            statement.add("key", keybinding.getKey());
            statement.add("type", keybinding.getValue().getType());
            statement.add("action", keybinding.getValue().getAction());
        }
        statement.execute(con);
    }

    public void savePets() {
        petLock.lock();
        try {
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    pets[i].saveToDb();
                }
            }
        } finally {
            petLock.unlock();
        }
    }

    public void saveSkillMacros(Connection con) throws SQLException {
        Statements.Delete.from("skill_macros").where("characterid", id).execute(con);
        Statements.BatchInsert statement = new Statements.BatchInsert("skill_macros");

        for (int i = 0; i < 5; i++) {
            SkillMacro macro = skillMacros[i];
            if (macro != null) {
                statement.add("characterid", getId());
                statement.add("skill1", macro.getSkill1());
                statement.add("skill2", macro.getSkill2());
                statement.add("skill3", macro.getSkill3());
                statement.add("name", macro.getName());
                statement.add("shout", macro.getShout());
                statement.add("position", i);
            }
        }
        statement.execute(con);
    }

    public void saveSkills(Connection con) throws SQLException {
        Statements.Delete.from("skills").where("characterid", id).execute(con);
        Statements.BatchInsert statement = new Statements.BatchInsert("skills");
        for (Entry<PlayerSkill, SkillEntry> skill : skills.entrySet()) {
            statement.add("characterid", id);
            statement.add("skillid", skill.getKey().getId());
            statement.add("skilllevel", skill.getValue().skillevel);
            statement.add("masterlevel", skill.getValue().masterlevel);
            statement.add("expiration", skill.getValue().expiration);
        }
        statement.execute(con);
    }

    public void saveSavedLocations(Connection con) throws SQLException {
        Statements.Delete.from("saved_locations").where("characterid", id).execute(con);
        Statements.BatchInsert statement = new Statements.BatchInsert("saved_locations");
        for (SavedLocationType savedLocationType : SavedLocationType.values()) {
            if (savedLocations[savedLocationType.ordinal()] != null) {
                statement.add("characterid", id);
                statement.add("locationtype", savedLocationType);
                statement.add("map", savedLocations[savedLocationType.ordinal()].getMapId());
                statement.add("portal", savedLocations[savedLocationType.ordinal()].getPortal());
            }
        }
        statement.execute(con);
    }

    public void saveTrockLocations(Connection con) throws SQLException {
        Statements.Delete.from("trock_locations").where("characterid", id).execute(con);
        Statements.BatchInsert statement = new Statements.BatchInsert("trock_locations");
        for (int i = 0; i < getTrockSize(); i++) {
            if (trockmaps.get(i) != 999999999) {
                statement.add("characterid", getId());
                statement.add("mapid", trockmaps.get(i));
                statement.add("vip", 0);
            }
        }
        for (int i = 0; i < getVipTrockSize(); i++) {
            if (viptrockmaps.get(i) != 999999999) {
                statement.add("characterid", getId());
                statement.add("mapid", viptrockmaps.get(i));
                statement.add("vip", 1);
            }
        }
        statement.execute(con);
    }

    public void saveBuddies(Connection con) throws SQLException {
        Statements.Delete.from("buddies").where("characterid", id).where("pending", 0).execute(con);
        Statements.BatchInsert statement = new Statements.BatchInsert("buddies");
        for (BuddylistEntry entry : buddylist.getBuddies()) {
            if (entry.isVisible()) {
                statement.add("characterid", id);
                statement.add("buddyid", entry.getCharacterId());
                statement.add("pending", 0);
                statement.add("\"group\"", entry.getGroup());
            }
        }
        statement.execute(con);
    }

    public void saveWorldTour(Connection con) throws SQLException {
        Statements.Delete.from("world_tour").where("charid", id).execute(con);
        Statements.BatchInsert statement = new Statements.BatchInsert("world_tour");
        for (String achid : finishedWorldTour) {
            statement.add("charid", id);
            statement.add("worldtourid", achid);
            statement.add("accountid", accountid);
        }
        statement.execute(con);
    }

    public void saveAreaInfo(Connection con) throws SQLException {
        Statements.Delete.from("area_info").where("charid", id).execute(con);
        Statements.BatchInsert statement = new Statements.BatchInsert("area_info");
        for (Entry<Short, String> area : area_info.entrySet()) {
            statement.add("charid", id);
            statement.add("area", area.getKey());
            statement.add("info", area.getValue());
        }
        statement.execute(con);
    }

    public void saveEventStats(Connection con) throws SQLException {
        Statements.Delete.from("event_stats").where("characterid", id).execute(con);
        Statements.BatchInsert statement = new Statements.BatchInsert("event_stats");
        for (Map.Entry<String, MapleEvents> entry : events.entrySet()) {
            statement.add("characterid", id);
            statement.add("name", entry.getKey());
            statement.add("info", entry.getValue().getInfo());
        }
        statement.execute(con);
    }

    public void saveQuestProgress(Connection con) throws SQLException {
        Statements.Delete.from("medal_maps").where("characterid", id).execute(con);
        Statements.Delete.from("quest_progress").where("characterid", id).execute(con);
        Statements.Delete.from("quest_status").where("characterid", id).execute(con);

        Statements.BatchInsert questprogress = new Statements.BatchInsert("quest_progress");
        Statements.BatchInsert medalmaps = new Statements.BatchInsert("medal_maps");

        synchronized (quests) {
            for (MapleQuestStatus q : quests.values()) {
                Statements.Insert queststatus = new Statements.Insert("quest_status");
                queststatus.add("characterid", id);
                queststatus.add("quest", q.getQuest().getId());
                queststatus.add("status", q.getStatus().getId());
                queststatus.add("time", (int) (q.getCompletionTime() / 1000));
                queststatus.add("expires", q.getExpirationTime());
                queststatus.add("forfeited", q.getForfeited());
                queststatus.add("completed", q.getCompleted());
                int questid = queststatus.execute(con);

                for (int mob : q.getProgress().keySet()) {
                    questprogress.add("characterid", id);
                    questprogress.add("queststatusid", questid);
                    questprogress.add("progressid", mob);
                    questprogress.add("progress", q.getProgress(mob));
                }
                for (int i = 0; i < q.getMedalMaps().size(); i++) {
                    medalmaps.add("characterid", id);
                    medalmaps.add("queststatusid", questid);
                    medalmaps.add("mapid", q.getMedalMaps().get(i));
                }
                questprogress.execute(con);
                medalmaps.execute(con);
            }
        }
    }

    //ItemFactory saveItems and monsterbook.saveCards are the most time consuming here.
    public synchronized void saveCharToDB(boolean notAutosave) {
        if (!loggedIn) return;

        Calendar c = Calendar.getInstance();

        if (notAutosave) {
            FilePrinter.print(FilePrinter.SAVING_CHARACTER, "[Save] " + name + " at " + c.getTime().toString());
        } else {
            FilePrinter.print(FilePrinter.AUTOSAVING_CHARACTER, "[Autosave] " + name + " at " + c.getTime().toString());
        }

        Server.getInstance().updateCharacterEntry(this);

        savePets();

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                saveCharacter(con);
                con.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Failed saving character stats");
            }

            try {
                saveSkills(con);
                con.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Failed saving character skills");
            }

            try {
                List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();
                for (MapleInventory iv : inventory) {
                    for (Item item : iv.list()) {
                        itemsWithType.add(new Pair<>(item, iv.getType()));
                    }
                }
                ItemFactory.INVENTORY.saveItems(itemsWithType, id, con);
                con.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Failed saving character inventory");
            }

            try {
                saveQuestProgress(con);
                con.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Failed saving character quest progress");
            }

            monsterbook.saveCards(con);
            savePetIgnores(con);
            saveKeymap(con);
            saveSkillMacros(con);

            saveSavedLocations(con);
            saveTrockLocations(con);

            saveBuddies(con);
            saveAreaInfo(con);
            saveEventStats(con);
            saveWorldTour(con);
            BossQuest.saveBossQuest(this, con);

            con.commit();
            con.setAutoCommit(true);

            if (cashshop != null) {
                cashshop.save(con);
            }

            if (storage != null && usedStorage) {
                storage.saveToDB(con);
                usedStorage = false;
            }

        } catch (Exception t) {
            FilePrinter.printError(FilePrinter.SAVE_CHAR, t, "Error saving " + name + " Level: " + level + " Job: " + job.getId());
        } finally {
            if (notAutosave) {
                FilePrinter.print(FilePrinter.SAVING_CHARACTER, "[Save] Finished " + name + " at " + c.getTime().toString());
            } else {
                FilePrinter.print(FilePrinter.AUTOSAVING_CHARACTER, "[Autosave] Finished " + name + " at " + c.getTime().toString());
            }
        }
    }

    public void sendPolice(int greason, String reason, int duration) {
        announce(WvsContext.Packet.onDataCRCCheckFailed(String.format("You have been blocked by the#b %s Police for %s.#k", "Boswell", reason)));
        this.isbanned = true;
        TimerManager.getInstance().schedule(() -> client.disconnect(false, false), duration);
    }

    public void sendPolice(String text) {
        String message = getName() + " received this - " + text;
        if (Server.getInstance().isGmOnline(this.getWorld())) { //Alert and log if a GM is online
            Server.getInstance().broadcastGMMessage(this.getWorld(), MaplePacketCreator.sendYellowTip(message));
            FilePrinter.print(FilePrinter.AUTOBAN_WARNING, message);
        } else { //Auto DC and log if no GM is online
            client.disconnect(false, false);
            FilePrinter.print(FilePrinter.AUTOBAN_DC, message);
        }
        //Server.getInstance().broadcastGMMessage(0, MaplePacketCreator.serverNotice(1, getName() + " received this - " + text));
        //announce(MaplePacketCreator.sendPolice(text));
        //this.isbanned = true;
        //TimerManager.getInstance().schedule(new Runnable() {
        //    @Override
        //    public void run() {
        //        client.disconnect(false, false);
        //    }
        //}, 6000);
    }

    public void sendKeymap() {
        client.announce(FuncKeyMappedMan.Packet.onFuncKeyMappedItemInit(keymap));
    }

    public void sendMacros() {
        // Always send the macro packet to fix a client side bug when switching characters.
        client.announce(WvsContext.Packet.onMacroSysDataInit(skillMacros));
    }

    public void sendNote(String to, String msg, byte fame) throws SQLException {
        sendNote(to, this.getName(), msg, fame);
    }

    public static void sendNote(String to, String from, String msg, byte fame) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Insert.into("notes")
                    .add("\"to\"", to)
                    .add("\"from\"", from)
                    .add("message", msg)
                    .add("timestamp", Server.getInstance().getCurrentTime())
                    .add("fame", fame)
                    .execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setAriantRoomLeader(int room, String charname) {
        ariantroomleader[room] = charname;
    }

    public static void setAriantSlotRoom(int room, int slot) {
        ariantroomslot[room] = slot;
    }

    public void setBattleshipHp(int battleshipHp) {
        this.battleshipHp = battleshipHp;
    }

    public void setBuddyCapacity(int capacity) {
        buddylist.setCapacity(capacity);
        client.announce(FriendPacket.Packet.onFriendResult(FriendResultType.CapacityChange.getType(), capacity));
    }

    public void setBuffedValue(MapleBuffStat effect, int value) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh != null) {
            mbsvh.value.setValue(value);
        }
    }

    public void setChalkboard(String text) {
        this.chalktext = text;
    }

    public void setDojoEnergy(int x) {
        this.dojoEnergy = Math.min(x, 10000);
    }

    public void setDojoPoints(int x) {
        this.dojoPoints = x;
    }

    public void setDojoStage(int x) {
        this.dojoStage = x;
    }

    public void setEnergyBar(int set) {
        energybar = set;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        evtLock.lock();
        try {
            this.eventInstance = eventInstance;
        } finally {
            evtLock.unlock();
        }
    }

    public void setExp(int amount) {
        this.exp.set(amount);
    }

    public void setGachaExp(int amount) {
        this.gachaexp.set(amount);
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }

    public void setFinishedDojoTutorial() {
        this.finishedDojoTutorial = true;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setGM(int level) {
        this.gmLevel = level;
    }
    
    public void setClearance(int c) {
        this.charClearance = c;
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Update("characters").set("clearance", charClearance).where("id", id).execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setTrophy(int t) {
        this.charTrophy = t;
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Update("trophy").set("trophy", charTrophy).where("id", id).execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setGuildId(int _id) {
        guildid = _id;
    }

    public void setGuildRank(int _rank) {
        guildRank = _rank;
    }

    public void setAllianceRank(int _rank) {
        allianceRank = _rank;
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public void setHasMerchant(boolean set) {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET HasMerchant = ? WHERE id = ?")) {
                ps.setBoolean(1, set);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        hasMerchant = set;
    }

    public void addMerchantMesos(int add) {
        int newAmount;
        newAmount = (int) Math.min((long) merchantmeso + add, Integer.MAX_VALUE);

        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET MerchantMesos = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, newAmount);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        merchantmeso = newAmount;
    }

    public void setMerchantMeso(int set) {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET MerchantMesos = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, set);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        merchantmeso = set;
    }

    public synchronized void withdrawMerchantMesos() {
        int merchantMeso = this.getMerchantNetMeso();
        int playerMeso = this.getMeso();

        if (merchantMeso > 0) {
            int possible = Integer.MAX_VALUE - playerMeso;

            if (possible > 0) {
                if (possible < merchantMeso) {
                    this.gainMeso(possible, false);
                    this.setMerchantMeso(merchantMeso - possible);
                } else {
                    this.gainMeso(merchantMeso, false);
                    this.setMerchantMeso(0);
                }
            }
        } else {
            int nextMeso = playerMeso + merchantMeso;

            if (nextMeso < 0) {
                this.gainMeso(-playerMeso, false);
                this.setMerchantMeso(merchantMeso + playerMeso);
            } else {
                this.gainMeso(merchantMeso, false);
                this.setMerchantMeso(0);
            }
        }
    }

    public void setHiredMerchant(MapleHiredMerchant merchant) {
        this.hiredMerchant = merchant;
    }

    private void hpChangeAction(int oldHp) {
        boolean playerDied = false;
        if (hp <= 0) {
            if (oldHp > hp) {
                if (!isBuybackInvincible()) {
                    playerDied = true;
                } else {
                    hp = 1;
                }
            }
        }

        final boolean chrDied = playerDied;
        if (map != null) {
            map.registerCharacterStatUpdate(() -> {
                updatePartyMemberHP();    // thanks BHB (BHB88) for detecting a deadlock case within player stats.

                if (chrDied) {
                    playerDead();
                } else {
                    checkBerserk(isHidden());
                }
            });
        }
    }

    private Pair<MapleStat, Integer> calcHpRatioUpdate(int newHp, int oldHp) {
        int delta = newHp - oldHp;
        this.hp = calcHpRatioUpdate(hp, oldHp, delta);

        hpChangeAction(Short.MIN_VALUE);
        return new Pair<>(MapleStat.HP, hp);
    }

    private Pair<MapleStat, Integer> calcMpRatioUpdate(int newMp, int oldMp) {
        int delta = newMp - oldMp;
        this.mp = calcMpRatioUpdate(mp, oldMp, delta);
        return new Pair<>(MapleStat.MP, mp);
    }

    private static int calcTransientRatio(float transientpoint) {
        int ret = (int) transientpoint;
        return !(ret <= 0 && transientpoint > 0.0f) ? ret : 1;
    }

    private Pair<MapleStat, Integer> calcHpRatioTransient() {
        this.hp = calcTransientRatio(transienthp * localmaxhp);

        hpChangeAction(Short.MIN_VALUE);
        return new Pair<>(MapleStat.HP, hp);
    }

    private Pair<MapleStat, Integer> calcMpRatioTransient() {
        this.mp = calcTransientRatio(transientmp * localmaxmp);
        return new Pair<>(MapleStat.MP, mp);
    }

    private int calcHpRatioUpdate(int curpoint, int maxpoint, int diffpoint) {
        int curMax = maxpoint;
        int nextMax = Math.min(30000, maxpoint + diffpoint);

        float temp = curpoint * nextMax;
        int ret = (int) Math.ceil(temp / curMax);

        transienthp = (maxpoint > nextMax) ? ((float) curpoint) / maxpoint : ((float) ret) / nextMax;
        return ret;
    }

    private int calcMpRatioUpdate(int curpoint, int maxpoint, int diffpoint) {
        int curMax = maxpoint;
        int nextMax = Math.min(30000, maxpoint + diffpoint);

        float temp = curpoint * nextMax;
        int ret = (int) Math.ceil(temp / curMax);

        transientmp = (maxpoint > nextMax) ? ((float) curpoint) / maxpoint : ((float) ret) / nextMax;
        return ret;
    }

    public boolean applyHpMpChange(int hpCon, int hpchange, int mpchange) {
        effLock.lock();
        statWlock.lock();
        try {
            int nextHp = hp + hpchange, nextMp = mp + mpchange;
            boolean cannotApplyHp = hpchange != 0 && nextHp <= 0 && (hpCon > 0);
            boolean cannotApplyMp = mpchange != 0 && nextMp < 0;

            if (cannotApplyHp || cannotApplyMp) {
                if (!isGM()) {
                    return false;
                }

                if (cannotApplyHp) {
                    nextHp = 1;
                }
            }

            updateHpMp(nextHp, nextMp);
            return true;
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public void setInventory(MapleInventoryType type, MapleInventory inv) {
        inventory[type.ordinal()] = inv;
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    public void setJob(MapleJob job) {
        this.job = job;
    }

    public void setLastHealed(long time) {
        this.lastHealed = time;
    }

    public void setLastUsedCashItem(long time) {
        this.lastUsedCashItem = time;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    public void setMessengerPosition(int position) {
        this.messengerposition = position;
    }

    public void setMiniGame(MapleMiniGame miniGame) {
        this.miniGame = miniGame;
    }

    public EnumMap<MapleBuffStat, MapleBuffStatValueHolder> copyBuffs() {
        return this.effects;
    }

    public void setTemporaryBuffs(EnumMap<MapleBuffStat, MapleBuffStatValueHolder> buffs) {
        this.effects = buffs;
    }

    public void setMiniGamePoints(MapleCharacter visitor, int winnerslot, boolean omok) {
        if (omok) {
            if (winnerslot == 1) {
                this.omokwins++;
                visitor.omoklosses++;
            } else if (winnerslot == 2) {
                visitor.omokwins++;
                this.omoklosses++;
            } else {
                this.omokties++;
                visitor.omokties++;
            }
        } else {
            if (winnerslot == 1) {
                this.matchcardwins++;
                visitor.matchcardlosses++;
            } else if (winnerslot == 2) {
                visitor.matchcardwins++;
                this.matchcardlosses++;
            } else {
                this.matchcardties++;
                visitor.matchcardties++;
            }
        }
    }

    public void setMonsterBookCover(int bookCover) {
        this.bookCover = bookCover;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void changeName(String name) {
        FredrickProcessor.removeFredrickReminders(this.getId());

        this.name = name;
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Update("characters").set("name", name).where("id", id).execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setParty(MapleParty p) {
        prtLock.lock();
        try {
            if (p == null) {
                this.mpc = null;
                party = null;
            } else {
                party = p;
            }
        } finally {
            prtLock.unlock();
        }
    }

    public void setPlayerShop(MaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public void setSearch(String find) {
        search = find;
    }

    public void setSkinColor(MapleSkinColor skinColor) {
        this.skinColor = skinColor;
    }

    public byte getSlots(int type) {
        return type == MapleInventoryType.CASH.getType() ? 96 : inventory[type].getSlotLimit();
    }

    public boolean gainSlots(int type, int slots) {
        return gainSlots(type, slots, true);
    }

    public boolean gainSlots(int type, int slots, boolean update) {
        slots += inventory[type].getSlotLimit();
        if (slots <= 96) {
            inventory[type].setSlotLimit(slots);

            this.saveCharToDB();
            if (update) {
                client.announce(WvsContext.Packet.onInventoryGrow(type, slots));
            }

            return true;
        }

        return false;
    }

    public int sellAllItemsFromName(byte invTypeId, String name) {
        //player decides from which inventory items should be sold.
        MapleInventoryType type = MapleInventoryType.getByType(invTypeId);

        MapleInventory inv = getInventory(type);
        inv.lockInventory();
        try {
            Item it = inv.findByName(name);
            if (it == null) {
                return (-1);
            }

            return (sellAllItemsFromPosition(ii, type, it.getPosition()));
        } finally {
            inv.unlockInventory();
        }
    }

    public int sellAllItemsFromPosition(MapleItemInformationProvider ii, MapleInventoryType type, short pos) {
        int mesoGain = 0;

        MapleInventory inv = getInventory(type);
        inv.lockInventory();
        try {
            for (short i = pos; i <= inv.getSlotLimit(); i++) {
                if (inv.getItem(i) == null) {
                    continue;
                }
                mesoGain += standaloneSell(getClient(), ii, type, i, inv.getItem(i).getQuantity());
            }
        } finally {
            inv.unlockInventory();
        }

        return (mesoGain);
    }

    private int standaloneSell(MapleClient c, MapleItemInformationProvider ii, MapleInventoryType type, short slot, short quantity) {
        if (quantity == 0xFFFF || quantity == 0) {
            quantity = 1;
        }

        MapleInventory inv = getInventory(type);
        inv.lockInventory();
        try {
            Item item = inv.getItem(slot);
            if (item == null) { //Basic check
                return (0);
            }

            int itemid = item.getItemId();
            if (ItemConstants.isRechargeable(itemid)) {
                quantity = item.getQuantity();
            } else if (ItemConstants.isWeddingToken(itemid) || ItemConstants.isWeddingRing(itemid)) {
                return (0);
            }

            if (quantity < 0) {
                return (0);
            }
            short iQuant = item.getQuantity();
            if (iQuant == 0xFFFF) {
                iQuant = 1;
            }

            if (quantity <= iQuant && iQuant > 0) {
                MapleInventoryManipulator.removeFromSlot(c, type, (byte) slot, quantity, false);
                int recvMesos = ii.getPrice(itemid, quantity);
                if (recvMesos > 0) {
                    gainMeso(recvMesos, false);
                    return (recvMesos);
                }
            }

            return (0);
        } finally {
            inv.unlockInventory();
        }
    }

    private static boolean hasMergeFlag(Item item) {
        return (item.getFlag() & ItemConstants.MERGE_UNTRADEABLE) == ItemConstants.MERGE_UNTRADEABLE;
    }

//    private static void setMergeFlag(Item item) {
//        int flag = item.getFlag();
//        flag |= ItemConstants.MERGE_UNTRADEABLE;
//        flag |= ItemConstants.UNTRADEABLE;
//        item.setFlag(flag);
//    }
//
//    private List<Equip> getUpgradeableEquipped() {
//        List<Equip> list = new LinkedList<>();
//
//        for (Item item : getInventory(MapleInventoryType.EQUIPPED)) {
//            if (ii.isUpgradeable(item.getItemId())) {
//                list.add((Equip) item);
//            }
//        }
//
//        return list;
//    }
//
//    private static List<Equip> getEquipsWithStat(List<Pair<Equip, Map<StatUpgrade, Short>>> equipped, StatUpgrade stat) {
//        List<Equip> equippedWithStat = new LinkedList<>();
//
//        for (Pair<Equip, Map<StatUpgrade, Short>> eq : equipped) {
//            if (eq.getRight().containsKey(stat)) {
//                equippedWithStat.add(eq.getLeft());
//            }
//        }
//
//        return equippedWithStat;
//    }

//    public boolean mergeAllItemsFromName(String name) {
//        MapleInventoryType type = MapleInventoryType.EQUIP;
//
//        MapleInventory inv = getInventory(type);
//        inv.lockInventory();
//        try {
//            Item it = inv.findByName(name);
//            if (it == null) {
//                return false;
//            }
//
//            Map<StatUpgrade, Float> statups = new LinkedHashMap<>();
//            mergeAllItemsFromPosition(statups, it.getPosition());
//
//            List<Pair<Equip, Map<StatUpgrade, Short>>> upgradeableEquipped = new LinkedList<>();
//            Map<Equip, List<Pair<StatUpgrade, Integer>>> equipUpgrades = new LinkedHashMap<>();
//            for (Equip eq : getUpgradeableEquipped()) {
//                upgradeableEquipped.add(new Pair<>(eq, eq.getStats()));
//                equipUpgrades.put(eq, new LinkedList<Pair<StatUpgrade, Integer>>());
//            }
//
//            /*
//            for (Entry<StatUpgrade, Float> es : statups.entrySet()) {
//                System.out.println(es);
//            }
//            */
//
//            for (Entry<StatUpgrade, Float> e : statups.entrySet()) {
//                Double ev = Math.sqrt(e.getValue());
//
//                Set<Equip> extraEquipped = new LinkedHashSet<>(equipUpgrades.keySet());
//                List<Equip> statEquipped = getEquipsWithStat(upgradeableEquipped, e.getKey());
//                float extraRate = (float) (0.2 * Math.random());
//
//                if (!statEquipped.isEmpty()) {
//                    float statRate = 1.0f - extraRate;
//
//                    int statup = (int) Math.ceil((ev * statRate) / statEquipped.size());
//                    for (Equip statEq : statEquipped) {
//                        equipUpgrades.get(statEq).add(new Pair<>(e.getKey(), statup));
//                        extraEquipped.remove(statEq);
//                    }
//                }
//
//                if (!extraEquipped.isEmpty()) {
//                    int statup = (int) Math.round((ev * extraRate) / extraEquipped.size());
//                    if (statup > 0) {
//                        for (Equip extraEq : extraEquipped) {
//                            equipUpgrades.get(extraEq).add(new Pair<>(e.getKey(), statup));
//                        }
//                    }
//                }
//            }
//
//            dropMessage(6, "EQUIPMENT MERGE operation results:");
//            for (Entry<Equip, List<Pair<StatUpgrade, Integer>>> eqpUpg : equipUpgrades.entrySet()) {
//                List<Pair<StatUpgrade, Integer>> eqpStatups = eqpUpg.getValue();
//                if (!eqpStatups.isEmpty()) {
//                    Equip eqp = eqpUpg.getKey();
//                    setMergeFlag(eqp);
//
//                    String showStr = " '" + MapleItemInformationProvider.getInstance().getName(eqp.getItemId()) + "': ";
//                    String upgdStr = eqp.gainStats(eqpStatups).getLeft();
//
//                    this.forceUpdateItem(eqp);
//
//                    showStr += upgdStr;
//                    dropMessage(6, showStr);
//                }
//            }
//
//            return true;
//        } finally {
//            inv.unlockInventory();
//        }
//    }

    public void mergeAllItemsFromPosition(Map<StatUpgrade, Float> statups, short pos) {
        MapleInventory inv = getInventory(MapleInventoryType.EQUIP);
        inv.lockInventory();
        try {
            for (short i = pos; i <= inv.getSlotLimit(); i++) {
                standaloneMerge(statups, getClient(), MapleInventoryType.EQUIP, i, inv.getItem(i));
            }
        } finally {
            inv.unlockInventory();
        }
    }

    private void standaloneMerge(Map<StatUpgrade, Float> statups, MapleClient c, MapleInventoryType type, short slot, Item item) {
        short quantity;
        if (item == null || (quantity = item.getQuantity()) < 1 || ii.isCash(item.getItemId()) || !ii.isUpgradeable(item.getItemId()) || hasMergeFlag(item)) {
            return;
        }

        Equip e = (Equip) item;
        for (Entry<StatUpgrade, Short> s : e.getStats().entrySet()) {
            Float newVal = statups.get(s.getKey());

            float incVal = s.getValue().floatValue();
            switch (s.getKey()) {
                case incPAD:
                case incMAD:
                case incPDD:
                case incMDD:
                    incVal = (float) Math.log(incVal);
                    break;
            }

            if (newVal != null) {
                newVal += incVal;
            } else {
                newVal = incVal;
            }

            statups.put(s.getKey(), newVal);
        }

        MapleInventoryManipulator.removeFromSlot(c, type, (byte) slot, quantity, false);
    }

    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public void setSlot(int slotid) {
        slots = slotid;
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public void setVanquisherKills(int x) {
        this.vanquisherKills = x;
    }

    public void setVanquisherStage(int x) {
        this.vanquisherStage = x;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public void shiftPetsRight() {
        petLock.lock();
        try {
            if (pets[2] == null) {
                pets[2] = pets[1];
                pets[1] = pets[0];
                pets[0] = null;
            }
        } finally {
            petLock.unlock();
        }
    }

    private long getDojoTimeLeft() {
        return client.getChannelServer().getDojoFinishTime(map.getId()) - Server.getInstance().getCurrentTime();
    }

    public void showDojoClock() {
        if (map.isDojoFightMap()) {
            client.announce(CField.Packet.onClock(true, (int) (getDojoTimeLeft() / 1000)));
        }
    }

    public void timeoutFromDojo() {
        if (map.isDojoMap()) {
            client.getPlayer().changeMap(client.getChannelServer().getMapFactory().getMap(925020002));
        }
    }

    public void showMapOwnershipInfo(MapleCharacter mapOwner) {
        long curTime = Server.getInstance().getCurrentTime();
        if (nextWarningTime < curTime) {
            nextWarningTime = curTime + (60 * 1000);   // show underlevel info again after 1 minute

            String medal = "";
            Item medalItem = mapOwner.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -49);
            if (medalItem != null) {
                medal = "<" + ii.getName(medalItem.getItemId()) + "> ";
            }

            List<String> strLines = new LinkedList<>();
            strLines.add("");
            strLines.add("");
            strLines.add("");
            strLines.add(this.getClient().getChannelServer().getServerMessage().isEmpty() ? 0 : 1, "Get off my lawn!!");

            this.announce(WvsContext.Packet.onSetAvatarMegaphone(mapOwner, medal, this.getClient().getChannel(), 5390006, strLines, true));
        }
    }

    public void showHint(String msg) {
        showHint(msg, 500);
    }

    public void showHint(String msg, int length) {
        client.announceHint(msg, length);
    }

    public void showNote() {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM notes WHERE \"to\" = ? AND deleted = 0", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                ps.setString(1, this.getName());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.last();
                    int count = rs.getRow();
                    rs.first();
                    client.announce(WvsContext.Packet.showNotes(rs, count));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
        long currentTime = System.currentTimeMillis();
        for (PlayerBuffValueHolder mbsvh : buffs) {

            long timeInStorage = currentTime - mbsvh.storageTime; // Time in storage
            mbsvh.startTime = mbsvh.startTime + timeInStorage; // New start time so that it's timed correctly upon restoring
            long endTime = mbsvh.startTime + mbsvh.effect.getDuration(); // Old end time
            long newEnd = timeInStorage + endTime; // New end time since the old one is modified

            if(System.currentTimeMillis() < newEnd) // If time hasn't surpassed the new end
                mbsvh.effect.silentApplyBuff(this, mbsvh.startTime);
        }
    }

    public void silentPartyUpdate() {
        //so that things like job and lvl change reflects in party, no need for external setMPC() calling.
        mpc = new MaplePartyCharacter(this);
        if (party != null) {
            Server.getInstance().getWorld(world).updateParty(party.getId(), PartyResultType.SilentUpdate.getResult(), getMPC());
        }
    }

    public static class SkillEntry {

        public int masterlevel;
        public byte skillevel;
        public long expiration;

        public SkillEntry(byte skillevel, int masterlevel, long expiration) {
            this.skillevel = skillevel;
            this.masterlevel = masterlevel;
            this.expiration = expiration;
        }

        @Override
        public String toString() {
            return skillevel + ":" + masterlevel;
        }
    }

    public boolean skillIsCooling(int skillId) {
        effLock.lock();
        chrLock.lock();
        try {
            return coolDowns.containsKey(Integer.valueOf(skillId));
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public void runFullnessSchedule(int petSlot) {
        MaplePet pet = getPet(petSlot);
        if (pet == null) {
            return;
        }

        int newFullness = pet.getFullness() - PetDataFactory.getHunger(pet.getItemId());
        if (newFullness <= 5) {
            pet.setFullness(15);
            pet.saveToDb();
            unequipPet(pet, true);
            dropMessage(6, "Your pet grew hungry! Treat it some pet food to keep it healthy!");
        } else {
            pet.setFullness(newFullness);
            pet.saveToDb();
            Item petz = getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
            if (petz != null) {
                forceUpdateItem(petz);
            }
        }
    }

    public boolean runTirednessSchedule() {
        if (maplemount != null) {
            int tiredness = maplemount.incrementAndGetTiredness();

            this.getMap().broadcastMessage(WvsContext.Packet.onSetTamingMobInfo(this.getId(), maplemount, false));
            if (tiredness > 99) {
                maplemount.setTiredness(99);
                this.dispelSkill(this.getJobType() * 10000000 + 1004);
                this.dropMessage(6, "Your mount grew tired! Treat it some revitalizer before riding it again!");
                return false;
            }
        }

        return true;
    }

    public void startMapEffect(String msg, int itemId) {
        startMapEffect(msg, itemId, 30000);
    }

    public void startMapEffect(String msg, int itemId, int duration) {
        final BlowWeather mapEffect = new BlowWeather(msg, itemId);
        getClient().announce(mapEffect.makeStartData());
        TimerManager.getInstance().schedule(() -> getClient().announce(mapEffect.makeDestroyData()), duration);
    }

    public void unequipAllPets() {
        for (int i = 0; i < 3; i++) {
            MaplePet pet = getPet(i);
            if (pet != null) {
                unequipPet(pet, true);
            }
        }
    }

    public void unequipPet(MaplePet pet, boolean shift_left) {
        unequipPet(pet, shift_left, false);
    }

    public void unequipPet(MaplePet pet, boolean shift_left, boolean hunger) {
        byte petIdx = this.getPetIndex(pet);
        MaplePet chrPet = this.getPet(petIdx);

        if (chrPet != null) {
            chrPet.setSummoned(false);
            chrPet.saveToDb();
        }

        this.getClient().getWorldServer().unregisterPetHunger(this, petIdx);
        getMap().broadcastMessage(this, PetPacket.Packet.onPetActivated(this, pet, true, hunger), true);

        removePet(pet, shift_left);
        commitExcludedItems();

        client.announce(WvsContext.Packet.petStatUpdate(this));
        client.announce(WvsContext.Packet.enableActions());
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        skillMacros[position] = updateMacro;
    }

    public void updatePartyMemberHP() {
        prtLock.lock();
        try {
            updatePartyMemberHPInternal();
        } finally {
            prtLock.unlock();
        }
    }

    private void updatePartyMemberHPInternal() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapId() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = Server.getInstance().getWorld(world).getChannel(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        other.client.announce(UserRemote.Packet.onReceiveHp(getId(), this.hp, maxhp));
                    }
                }
            }
        }
    }

    public String getQuestInfo(int quest) {
        MapleQuestStatus qs = getQuest(MapleQuest.getInstance(quest));
        return qs.getInfo();
    }

    public void updateQuestInfo(int quest, String info) {
        MapleQuest q = MapleQuest.getInstance(quest);
        MapleQuestStatus qs = getQuest(q);
        qs.setInfo(info);

        synchronized (quests) {
            quests.put(q.getId(), qs);
        }

        announce(MaplePacketCreator.updateQuest(qs, false));
        if (qs.getQuest().getInfoNumber() > 0) {
            announce(MaplePacketCreator.updateQuest(qs, true));
        }
        announce(UserLocal.Packet.onQuestResult(qs.getQuest().getId(), QuestResultType.UpdateInfo.getResult(), qs.getNpc()));
    }

    public void updateQuest(MapleQuestStatus quest) {
        synchronized (quests) {
            quests.put(quest.getQuestID(), quest);
        }
        if (quest.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
            announce(MaplePacketCreator.updateQuest(quest, false));
            if (quest.getQuest().getInfoNumber() > 0) {
                announce(MaplePacketCreator.updateQuest(quest, true));
            }
            announce(UserLocal.Packet.onQuestResult(quest.getQuest().getId(), QuestResultType.UpdateInfo.getResult(), quest.getNpc()));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
            MapleQuest mquest = quest.getQuest();
            short questid = mquest.getId();

            quest.setCompleted(quest.getCompleted() + 1);   // count quest completed Jayd's idea

            announce(MaplePacketCreator.completeQuest(questid, quest.getCompletionTime()));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.NOT_STARTED)) {
            announce(MaplePacketCreator.updateQuest(quest, false));
            if (quest.getQuest().getInfoNumber() > 0) {
                announce(MaplePacketCreator.updateQuest(quest, true));
            }
        }
    }

    private void expireQuest(MapleQuest quest) {
        if (getQuestStatus(quest.getId()) == MapleQuestStatus.Status.COMPLETED.getId()) {
            return;
        }
        if (System.currentTimeMillis() < getMapleQuestStatus(quest.getId()).getExpirationTime()) {
            return;
        }

        announce(UserLocal.Packet.onQuestResult(quest.getId(), QuestResultType.Expire.getResult()));
        MapleQuestStatus newStatus = new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
        newStatus.setForfeited(getQuest(quest).getForfeited() + 1);
        updateQuest(newStatus);
    }

    public void cancelQuestExpirationTask() {
        evtLock.lock();
        try {
            if (questExpireTask != null) {
                questExpireTask.cancel(false);
                questExpireTask = null;
            }
        } finally {
            evtLock.unlock();
        }
    }

    public void forfeitExpirableQuests() {
        evtLock.lock();
        try {
            for (MapleQuest quest : questExpirations.keySet()) {
                quest.forfeit(this);
            }

            questExpirations.clear();
        } finally {
            evtLock.unlock();
        }
    }

    public void questExpirationTask() {
        evtLock.lock();
        try {
            if (!questExpirations.isEmpty()) {
                if (questExpireTask == null) {
                    questExpireTask = TimerManager.getInstance().register(() -> runQuestExpireTask(), 10 * 1000);
                }
            }
        } finally {
            evtLock.unlock();
        }
    }

    private void runQuestExpireTask() {
        evtLock.lock();
        try {
            long timeNow = Server.getInstance().getCurrentTime();
            List<MapleQuest> expireList = new LinkedList<>();

            for (Entry<MapleQuest, Long> qe : questExpirations.entrySet()) {
                if (qe.getValue() <= timeNow) {
                    expireList.add(qe.getKey());
                }
            }

            if (!expireList.isEmpty()) {
                for (MapleQuest quest : expireList) {
                    expireQuest(quest);
                    questExpirations.remove(quest);
                }

                if (questExpirations.isEmpty()) {
                    questExpireTask.cancel(false);
                    questExpireTask = null;
                }
            }
        } finally {
            evtLock.unlock();
        }
    }

    private void registerQuestExpire(MapleQuest quest, long time) {
        evtLock.lock();
        try {
            if (questExpireTask == null) {
                questExpireTask = TimerManager.getInstance().register(() -> runQuestExpireTask(), 10 * 1000);
            }

            questExpirations.put(quest, Server.getInstance().getCurrentTime() + time);
        } finally {
            evtLock.unlock();
        }
    }

    public void questTimeLimit(final MapleQuest quest, int seconds) {
        registerQuestExpire(quest, seconds * 1000);
        announce(UserLocal.Packet.onQuestResult(quest.getId(), QuestResultType.AddTime.getResult(), seconds * 1000));
    }

    public void questTimeLimit2(final MapleQuest quest, long expires) {
        long timeLeft = expires - System.currentTimeMillis();

        if (timeLeft <= 0) {
            expireQuest(quest);
        } else {
            registerQuestExpire(quest, timeLeft);
        }
    }

    public void updateSingleStat(MapleStat stat, int newval) {
        updateSingleStat(stat, newval, false);
    }

    private void updateSingleStat(MapleStat stat, int newval, boolean itemReaction) {
        announce(WvsContext.Packet.updatePlayerStats(Collections.singletonList(new Pair<>(stat, Integer.valueOf(newval))), itemReaction, this));
    }

    public void announce(final byte[] packet) {
        client.announce(packet);
    }

    @Override
    public int getObjectId() {
        return getId();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(UserPool.Packet.onUserLeaveField(this.getObjectId()));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (!this.isHidden() || client.getPlayer().gmLevel() > 1) {
            client.announce(UserPool.Packet.onUserEnterField(client, this));

/*            if (buffEffects.containsKey(getJobMapChair(job))) { // mustn't effLock, chrLock this function
                client.announce(MaplePacketCreator.giveForeignChairSkillEffect(id));
            }*/
        }

        if (this.isHidden()) {
            List<Pair<MapleBuffStat, BuffValueHolder>> dsstat = Collections.singletonList(new Pair<>(
                    MapleBuffStat.DARKSIGHT, new BuffValueHolder(0, 0, 0)));
            getMap().broadcastGMMessage(this, UserRemote.Packet.giveForeignBuff(getId(), dsstat), false);
        }
    }

    @Override
    public void setObjectId(int id) {
    }

    @Override
    public String toString() {
        return name;
    }

    public int getLinkedLevel() {
        return linkedLevel;
    }

    public String getLinkedName() {
        return linkedName;
    }

    public CashShop getCashShop() {
        return cashshop;
    }

    public Set<NewYearCardRecord> getNewYearRecords() {
        return newyears;
    }

    public Set<NewYearCardRecord> getReceivedNewYearRecords() {
        Set<NewYearCardRecord> received = new LinkedHashSet<>();

        for (NewYearCardRecord nyc : newyears) {
            if (nyc.isReceiverCardReceived()) {
                received.add(nyc);
            }
        }

        return received;
    }

    public NewYearCardRecord getNewYearRecord(int cardid) {
        for (NewYearCardRecord nyc : newyears) {
            if (nyc.getId() == cardid) {
                return nyc;
            }
        }

        return null;
    }

    public void addNewYearRecord(NewYearCardRecord newyear) {
        newyears.add(newyear);
    }

    public void removeNewYearRecord(NewYearCardRecord newyear) {
        newyears.remove(newyear);
    }

    public void portalDelay(long delay) {
        this.portaldelay = System.currentTimeMillis() + delay;
    }

    public long portalDelay() {
        return portaldelay;
    }

    public void blockPortal(String scriptName) {
        if (!blockedPortals.contains(scriptName) && scriptName != null) {
            blockedPortals.add(scriptName);
            client.announce(WvsContext.Packet.enableActions());
        }
    }

    public void unblockPortal(String scriptName) {
        if (blockedPortals.contains(scriptName) && scriptName != null) {
            blockedPortals.remove(scriptName);
        }
    }

    public List<String> getBlockedPortals() {
        return blockedPortals;
    }

    public boolean containsAreaInfo(int area, String info) {
        Short area_ = Short.valueOf((short) area);
        if (area_info.containsKey(area_)) {
            return area_info.get(area_).contains(info);
        }
        return false;
    }

    public void updateAreaInfo(int area, String info) {
        area_info.put(Short.valueOf((short) area), info);
        announce(MaplePacketCreator.updateAreaInfo(area, info));
    }

    public String getAreaInfo(int area) {
        return area_info.get(Short.valueOf((short) area));
    }

    public Map<Short, String> getAreaInfos() {
        return area_info;
    }

    public void autoban(String reason, AutobanFactory fac) {
        this.cheating(reason);
        //announce(MaplePacketCreator.sendPolice(String.format("You have been blocked by the#b %s Police for HACK reason.#k", "MapleAvenue")));
        //TimerManager.getInstance().schedule(() -> client.disconnect(false, false), 8000);
        Server.getInstance().broadcastGMMessage(this.getWorld(), MaplePacketCreator.serverNotice(6, MapleCharacter.makeMapleReadable(this.name) + " is a cheater for " + reason));
    }

    public void block(int reason, int days, String desc) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, days);
        Timestamp TS = new Timestamp(cal.getTimeInMillis());
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banreason = ?, tempban = ?, greason = ? WHERE id = ?")) {
                ps.setString(1, desc);
                ps.setTimestamp(2, TS);
                ps.setInt(3, reason);
                ps.setInt(4, accountid);
                ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isBanned() {
        return isbanned;
    }

    public boolean isCheater() {
        return ischeater;
    }

    public List<Integer> getTrockMaps() {
        return trockmaps;
    }

    public List<Integer> getVipTrockMaps() {
        return viptrockmaps;
    }

    public int getTrockSize() {
        int ret = trockmaps.indexOf(999999999);
        if (ret == -1) {
            ret = 5;
        }

        return ret;
    }

    public void deleteFromTrocks(int map) {
        trockmaps.remove(Integer.valueOf(map));
        while (trockmaps.size() < 10) {
            trockmaps.add(999999999);
        }
    }

    public void addTrockMap() {
        int index = trockmaps.indexOf(999999999);
        if (index != -1) {
            trockmaps.set(index, getMapId());
        }
    }

    public boolean isTrockMap(int id) {
        int index = trockmaps.indexOf(id);
        return index != -1;
    }

    public int getVipTrockSize() {
        int ret = viptrockmaps.indexOf(999999999);

        if (ret == -1) {
            ret = 10;
        }

        return ret;
    }

    public void deleteFromVipTrocks(int map) {
        viptrockmaps.remove(Integer.valueOf(map));
        while (viptrockmaps.size() < 10) {
            viptrockmaps.add(999999999);
        }
    }

    public void addVipTrockMap() {
        int index = viptrockmaps.indexOf(999999999);
        if (index != -1) {
            viptrockmaps.set(index, getMapId());
        }
    }

    public boolean isVipTrockMap(int id) {
        int index = viptrockmaps.indexOf(id);
        return index != -1;
    }

    public AutobanManager getAutobanManager() {
        return autoban;
    }

    public void equippedItem(Equip equip) {
        int itemid = equip.getItemId();

        if (itemid == 1122017) {
            this.equipPendantOfSpirit();
        } else if (itemid == 1812000) { // meso magnet
            equippedMesoMagnet = true;
        } else if (itemid == 1812001) { // item pouch
            equippedItemPouch = true;
        } else if (itemid == 1812007) { // item ignore pendant
            equippedPetItemIgnore = true;
        }
    }

    public void unequippedItem(Equip equip) {
        int itemid = equip.getItemId();

        if (itemid == 1122017) {
            this.unequipPendantOfSpirit();
        } else if (itemid == 1812000) { // meso magnet
            equippedMesoMagnet = false;
        } else if (itemid == 1812001) { // item pouch
            equippedItemPouch = false;
        } else if (itemid == 1812007) { // item ignore pendant
            equippedPetItemIgnore = false;
        }
    }

    public boolean isEquippedMesoMagnet() {
        return equippedMesoMagnet;
    }

    public boolean isEquippedItemPouch() {
        return equippedItemPouch;
    }

    public boolean isEquippedPetItemIgnore() {
        return equippedPetItemIgnore;
    }

    private void equipPendantOfSpirit() {
        if (pendantOfSpirit == null) {
            pendantOfSpirit = TimerManager.getInstance().register(() -> {
                if (pendantExp < 3) {
                    pendantExp++;
                    message("Pendant of the Spirit has been equipped for " + pendantExp + " hour(s), you will now receive " + pendantExp + "0% bonus exp.");
                } else {
                    pendantOfSpirit.cancel(false);
                }
            }, 3600000); //1 hour
        }
    }

    private void unequipPendantOfSpirit() {
        if (pendantOfSpirit != null) {
            pendantOfSpirit.cancel(false);
            pendantOfSpirit = null;
        }
        pendantExp = 0;
    }

    private Collection<Item> getUpgradeableEquipList() {
        Collection<Item> fullList = getInventory(MapleInventoryType.EQUIPPED).list();
        Collection<Item> eqpList = new LinkedHashSet<>();

        for (Item it : fullList) {
            if (!ii.isCash(it.getItemId())) {
                eqpList.add(it);
            }
        }

        return eqpList;
    }

    public void broadcastMarriageMessage() {
        MapleGuild guild = this.getGuild();
        if (guild != null) {
            guild.broadcast(WvsContext.Packet.onNotifyWedding(0, name));
        }

        MapleFamily family = this.getFamily();
        if (family != null) {
            family.broadcast(WvsContext.Packet.onNotifyWedding(1, name));
        }
    }
    
    // item leveling only used for reverse, von leon, timeless weapon sets
    public void increaseEquipExp(int mobexp) {
        MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
        for (Item item : getInventory(MapleInventoryType.EQUIPPED).list()) {
            Equip nEquip = (Equip) item;
            if (mii.canLevelUp(item.getItemId())) {
                nEquip.gainItemExp(client, mobexp);
            }
        }
    }

    public Map<String, MapleEvents> getEvents() {
        return events;
    }

    public PartyQuest getPartyQuest() {
        return partyQuest;
    }

    public void setPartyQuest(PartyQuest pq) {
        this.partyQuest = pq;
    }

    public void setCpqTimer(ScheduledFuture timer) {
        this.cpqSchedule = timer;
    }

    public void clearCpqTimer() {
        if (cpqSchedule != null) {
            cpqSchedule.cancel(true);
        }
        cpqSchedule = null;
    }

    public final void empty(final boolean remove) {
        if (dragonBloodSchedule != null) {
            dragonBloodSchedule.cancel(true);
        }
        dragonBloodSchedule = null;

        if (hpDecreaseTask != null) {
            hpDecreaseTask.cancel(true);
        }
        hpDecreaseTask = null;

        if (beholderHealingSchedule != null) {
            beholderHealingSchedule.cancel(true);
        }
        beholderHealingSchedule = null;

        if (beholderBuffSchedule != null) {
            beholderBuffSchedule.cancel(true);
        }
        beholderBuffSchedule = null;

        if (berserkSchedule != null) {
            berserkSchedule.cancel(true);
        }
        berserkSchedule = null;

        //unregisterChairBuff();
        cancelBuffExpireTask();
        cancelDiseaseExpireTask();
        cancelSkillCooldownTask();
        cancelExpirationTask();

        if (questExpireTask != null) {
            questExpireTask.cancel(true);
        }
        questExpireTask = null;

        if (recoveryTask != null) {
            recoveryTask.cancel(true);
        }
        recoveryTask = null;

        if (extraRecoveryTask != null) {
            extraRecoveryTask.cancel(true);
        }
        extraRecoveryTask = null;

        // already done on unregisterChairBuff
        /* if (chairRecoveryTask != null) { chairRecoveryTask.cancel(true); }
        chairRecoveryTask = null; */

        if (pendantOfSpirit != null) {
            pendantOfSpirit.cancel(true);
        }
        pendantOfSpirit = null;

        clearCpqTimer();

        evtLock.lock();
        try {
            if (questExpireTask != null) {
                questExpireTask.cancel(false);
                questExpireTask = null;

                questExpirations.clear();
                questExpirations = null;
            }
        } finally {
            evtLock.unlock();
        }

        if (maplemount != null) {
            maplemount.empty();
            maplemount = null;
        }
        if (remove) {
            partyQuest = null;
            events = null;
            mpc = null;
            mgc = null;
            party = null;
            family = null;

            getWorldServer().registerTimedMapObject(() -> {
                client = null;  // clients still triggers handlers a few times after disconnecting
                map = null;
                setListener(null);

                for (MapleInventory items : inventory) {
                    items.dispose();
                }
                
                inventory = null;
                
            }, 5 * 60 * 1000);
        }
    }

    public void logOff() {
        this.loggedIn = false;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE characters SET lastLogoutTime=? WHERE id=?")) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isLoggedin() {
        return loggedIn;
    }

    public void setMapId(int mapid) {
        this.mapid = mapid;
    }

    public boolean getWhiteChat() {
        return isGM() && whiteChat;
    }

    public void toggleWhiteChat() {
        whiteChat = !whiteChat;
    }

    // These need to be renamed, but I am too lazy right now to go through the scripts and rename them...
    public String getPartyQuestItems() {
        return dataString;
    }

    public boolean gotPartyQuestItem(String partyquestchar) {
        return dataString.contains(partyquestchar);
    }

    public void removePartyQuestItem(String letter) {
        if (gotPartyQuestItem(letter)) {
            dataString = dataString.substring(0, dataString.indexOf(letter)) + dataString.substring(dataString.indexOf(letter) + letter.length());
        }
    }

    public void setPartyQuestItemObtained(String partyquestchar) {
        if (!dataString.contains(partyquestchar)) {
            this.dataString += partyquestchar;
        }
    }

    public void createDragon() {
        dragon = new MapleDragon(this);
    }

    public MapleDragon getDragon() {
        return dragon;
    }

    public void setDragon(MapleDragon dragon) {
        this.dragon = dragon;
    }

    public long getJailExpirationTimeLeft() {
        return jailExpiration - System.currentTimeMillis();
    }

    private void setFutureJailExpiration(long time) {
        jailExpiration = System.currentTimeMillis() + time;
    }

    public void addJailExpirationTime(long time) {
        long timeLeft = getJailExpirationTimeLeft();

        if (timeLeft <= 0) {
            setFutureJailExpiration(time);
        } else {
            setFutureJailExpiration(timeLeft + time);
        }
    }

    public void removeJailExpirationTime() {
        jailExpiration = 0;
    }

    public String getLastCommandMessage() {
        return this.commandtext;
    }

    public void setLastCommandMessage(String text) {
        this.commandtext = text;
    }

    public int getRewardPoints() {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT rewardpoints FROM accounts WHERE id=?;")) {
            ps.setInt(1, accountid);
            try (ResultSet resultSet = ps.executeQuery()) {
                int point = -1;
                if (resultSet.next()) {
                    point = resultSet.getInt(1);
                }
                return point;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void setRewardPoints(int value) {
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Update("accounts").set("rewardpoints", value).where("id", accountid).execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setReborns(int value) {
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Update("characters").set("reborns", value).where("id", id).execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addReborns() {
        setReborns(getReborns() + 1);
    }

    public int getReborns() {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT reborns FROM characters WHERE id=?;")) {
            ps.setInt(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    public void executeReborn() {
        if (getLevel() != 200) {
            return;
        }
        addReborns();
        changeJob(MapleJob.BEGINNER);
        setLevel(0);
        levelUp(true);
    }

    //EVENTS
    private byte team = 0;
    private MapleFitness fitness;
    private MapleOla ola;
    private long snowballattack;

    public byte getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = (byte) team;
    }

    public MapleOla getOla() {
        return ola;
    }

    public void setOla(MapleOla ola) {
        this.ola = ola;
    }

    public MapleFitness getFitness() {
        return fitness;
    }

    public void setFitness(MapleFitness fit) {
        this.fitness = fit;
    }

    public long getLastSnowballAttack() {
        return snowballattack;
    }

    public void setLastSnowballAttack(long time) {
        this.snowballattack = time;
    }

    // MCPQ

    public AriantColiseum ariantColiseum;
    private MonsterCarnival monsterCarnival;
    private MonsterCarnivalParty monsterCarnivalParty = null;

    private int cp = 0;
    private int totCP = 0;
    private int FestivalPoints;
    private boolean challenged = false;
    public short totalCP, availableCP;

    public void gainFestivalPoints(int gain) {
        this.FestivalPoints += gain;
    }

    public int getFestivalPoints() {
        return this.FestivalPoints;
    }

    public void setFestivalPoints(int pontos) {
        this.FestivalPoints = pontos;
    }

    public int getCP() {
        return cp;
    }

    public void addCP(int ammount) {
        totalCP += ammount;
        availableCP += ammount;
    }

    public void useCP(int ammount) {
        availableCP -= ammount;
    }

    public void gainCP(int gain) {
        if (this.getMonsterCarnival() != null) {
            if (gain > 0) {
                this.setTotalCP(this.getTotalCP() + gain);
            }
            this.setCP(this.getCP() + gain);
            if (this.getParty() != null) {
                this.getMonsterCarnival().setCP(this.getMonsterCarnival().getCP(team) + gain, team);
                if (gain > 0) {
                    this.getMonsterCarnival().setTotalCP(this.getMonsterCarnival().getTotalCP(team) + gain, team);
                }
            }
            if (this.getCP() > this.getTotalCP()) {
                this.setTotalCP(this.getCP());
            }
            this.getClient().announce(MonsterCarnivalPacket.Packet.onPersonalCP(this.getCP(), this.getTotalCP()));
            if (this.getParty() != null && getTeam() != -1) {
                this.getMap().broadcastMessage(
                        MonsterCarnivalPacket.Packet.onTeamCP(this.getMonsterCarnival().getCP(team), this.getMonsterCarnival().getTotalCP(team), getTeam()));
            }
        }
    }

    public void setTotalCP(int a) {
        this.totCP = a;
    }

    public void setCP(int a) {
        this.cp = a;
    }

    public int getTotalCP() {
        return totCP;
    }

    public int getAvailableCP() {
        return availableCP;
    }

    public void resetCP() {
        this.cp = 0;
        this.totCP = 0;
        this.monsterCarnival = null;
    }

    public MonsterCarnival getMonsterCarnival() {
        return monsterCarnival;
    }

    public void setMonsterCarnival(MonsterCarnival monsterCarnival) {
        this.monsterCarnival = monsterCarnival;
    }

    public AriantColiseum getAriantColiseum() {
        return ariantColiseum;
    }

    public void setAriantColiseum(AriantColiseum ariantColiseum) {
        this.ariantColiseum = ariantColiseum;
    }

    public MonsterCarnivalParty getMonsterCarnivalParty() {
        return this.monsterCarnivalParty;
    }

    public void setMonsterCarnivalParty(MonsterCarnivalParty mcp) {
        this.monsterCarnivalParty = mcp;
    }

    public boolean isChallenged() {
        return challenged;
    }

    public void setChallenged(boolean challenged) {
        this.challenged = challenged;
    }

    public void gainAriantPoints(int points) {
        this.ariantPoints += points;
    }

    public int getAriantPoints() {
        return this.ariantPoints;
    }


    public AutobanTracker getAutobanTracker() {
        return tracker;
    }

    public boolean isMagician() {
        return job.getId() >= 200 && job.getId() <= 232;
    }

    /*
    used as mod for world tour system
    */
    public boolean isWarriorMod() {
        return (job.getId() >= 100 && job.getId() <= 132 || job.getId() == 510 || job.getId() == 511 || job.getId() == 512 || job.getId() == 0);
    }

    public void gainMaxHp(int gain) {
        maxhp = getMaxHp() + gain;
        updateSingleStat(MapleStat.MAXHP, maxhp);
    }

    public void setWorldTourFinished(String id) {
        finishedWorldTour.add(id);
    }

    public boolean worldTourFinished(String achievementid) {
        return finishedWorldTour.contains(achievementid);
    }

    public void finishWorldTour(WorldTour.AchievementType achievement_type, int target) {
        if (isAlive()) {
            WorldTour.finishWorldTour(this, achievement_type, target);
        }
    }

    public void setLanguage(int num) {
        getClient().setLanguage(num);
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET language = ? WHERE id = ?")) {
            ps.setInt(1, num);
            ps.setInt(2, getClient().getAccID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getLanguage() {
        return getClient().getLanguage();
    }

    public void updatePartySearchAvailability(boolean psearchAvailable) {
        if (psearchAvailable) {
            if (canRecvPartySearchInvite && getParty() == null) {
                this.getWorldServer().getPartySearchCoordinator().attachPlayer(this);
            }
        } else {
            if (canRecvPartySearchInvite) {
                this.getWorldServer().getPartySearchCoordinator().detachPlayer(this);
            }
        }
    }

    public boolean toggleRecvPartySearchInvite() {
        canRecvPartySearchInvite = !canRecvPartySearchInvite;

        if (canRecvPartySearchInvite) {
            updatePartySearchAvailability(getParty() == null);
        } else {
            this.getWorldServer().getPartySearchCoordinator().detachPlayer(this);
        }

        return canRecvPartySearchInvite;
    }

    public boolean isRecvPartySearchInviteEnabled() {
        return canRecvPartySearchInvite;
    }

    public void resetPartySearchInvite(int fromLeaderid) {
        disabledPartySearchInvites.remove(fromLeaderid);
    }

    public void disablePartySearchInvite(int fromLeaderid) {
        disabledPartySearchInvites.add(fromLeaderid);
    }

    public boolean hasDisabledPartySearchInvite(int fromLeaderid) {
        return disabledPartySearchInvites.contains(fromLeaderid);
    }

   /* public void updateActiveEffects() {
        effLock.lock(); // thanks davidlafriniere, maple006, RedHat for pointing a deadlock occurring here
        try {
            Set<MapleBuffStat> updatedBuffs = new LinkedHashSet<>();
            Set<MapleStatEffect> activeEffects = new LinkedHashSet<>();

            for (MapleBuffStatValueHolder mse : effects.values()) {
                activeEffects.add(mse.effect);
            }

            for (Map<MapleBuffStat, MapleBuffStatValueHolder> buff : buffEffects.values()) {
                MapleStatEffect mse = getEffectFromBuffSource(buff);
                if (isUpdatingEffect(activeEffects, mse)) {
                    for (Pair<MapleBuffStat, Integer> p : mse.getStatups()) {
                        updatedBuffs.add(p.getLeft());
                    }
                }
            }

            for (MapleBuffStat mbs : updatedBuffs) {
                effects.remove(mbs);
            }

            updateEffects(updatedBuffs);
        } finally {
            effLock.unlock();
        }
    }*/

    private void cancelInactiveBuffStats(Set<MapleBuffStat> retrievedStats, Set<MapleBuffStat> removedStats) {
        List<MapleBuffStat> inactiveStats = new LinkedList<>();
        for (MapleBuffStat mbs : removedStats) {
            if (!retrievedStats.contains(mbs)) {
                inactiveStats.add(mbs);
            }
        }

        if (!inactiveStats.isEmpty()) {
            client.announce(WvsContext.Packet.cancelBuff(inactiveStats));
            getMap().broadcastMessage(this, UserRemote.Packet.cancelForeignBuff(getId(), inactiveStats), false);
        }
    }

    private static Map<MapleStatEffect, Integer> topologicalSortLeafStatCount(Map<MapleBuffStat, Stack<MapleStatEffect>> buffStack) {
        Map<MapleStatEffect, Integer> leafBuffCount = new LinkedHashMap<>();

        for (Entry<MapleBuffStat, Stack<MapleStatEffect>> e : buffStack.entrySet()) {
            Stack<MapleStatEffect> mseStack = e.getValue();
            if (mseStack.isEmpty()) {
                continue;
            }

            MapleStatEffect mse = mseStack.peek();
            Integer count = leafBuffCount.get(mse);
            if (count == null) {
                leafBuffCount.put(mse, 1);
            } else {
                leafBuffCount.put(mse, count + 1);
            }
        }

        return leafBuffCount;
    }

    private static List<MapleStatEffect> topologicalSortRemoveLeafStats(Map<MapleStatEffect, Set<MapleBuffStat>> stackedBuffStats, Map<MapleBuffStat, Stack<MapleStatEffect>> buffStack, Map<MapleStatEffect, Integer> leafStatCount) {
        List<MapleStatEffect> clearedStatEffects = new LinkedList<>();
        Set<MapleBuffStat> clearedStats = new LinkedHashSet<>();

        for (Entry<MapleStatEffect, Integer> e : leafStatCount.entrySet()) {
            MapleStatEffect mse = e.getKey();

            if (stackedBuffStats.get(mse).size() <= e.getValue()) {
                clearedStatEffects.add(mse);

                for (MapleBuffStat mbs : stackedBuffStats.get(mse)) {
                    clearedStats.add(mbs);
                }
            }
        }

        for (MapleBuffStat mbs : clearedStats) {
            MapleStatEffect mse = buffStack.get(mbs).pop();
            stackedBuffStats.get(mse).remove(mbs);
        }

        return clearedStatEffects;
    }

    public float getCardRate(int itemid) {
        float rate = 100.0f;

        if (itemid == 0) {
            MapleStatEffect mseMeso = getBuffEffect(MapleBuffStat.MESO_UP_BY_ITEM);
            if (mseMeso != null) {
                rate += mseMeso.getCardRate(mapid, itemid);
            }
        } else {
            MapleStatEffect mseItem = getBuffEffect(MapleBuffStat.ITEM_UP_BY_ITEM);
            if (mseItem != null) {
                rate += mseItem.getCardRate(mapid, itemid);
            }
        }

        return rate / 100;
    }

    public void closePartySearchInteractions() {
        this.getWorldServer().getPartySearchCoordinator().unregisterPartyLeader(this);
        if (canRecvPartySearchInvite) {
            this.getWorldServer().getPartySearchCoordinator().detachPlayer(this);
        }
    }

/*    private static MapleStatEffect getEffectFromBuffSource(Map<MapleBuffStat, MapleBuffStatValueHolder> buffSource) {
        try {
            return buffSource.entrySet().iterator().next().getValue().effect;
        } catch (Exception e) {
            return null;
        }
    }*/

/*    private boolean isUpdatingEffect(Set<MapleStatEffect> activeEffects, MapleStatEffect mse) {
        if (mse == null) return false;

        // thanks xinyifly for noticing "Speed Infusion" crashing game when updating buffs during map transition
        boolean active = mse.isActive(this);
        if (active) {
            return !activeEffects.contains(mse);
        } else {
            return activeEffects.contains(mse);
        }
    }*/

    public void setOwnedMap(MapleMap map) {
        ownedMap = new WeakReference<>(map);
    }

    public MapleMap getOwnedMap() {
        return ownedMap.get();
    }

    public void setLoginTime() {
        login_time = System.currentTimeMillis();
    }

    public long getLoginTime() {
        return login_time;
    }

    /**
     * A task is started where every 2 minutes a fishing reward is selected
     * and given to the player. This is dependant on them having the correct
     * bait, map, and chair.
     */
    public void startFishingTask() {
        final int time = 120000; // 2 min
        if (getMapId() != MapConstants.FISH_LAGOON) {
            return;
        }
        cancelFishingTask();

        fishing = TimerManager.getInstance().register(() -> {
            if (!getAbstractPlayerInteraction().hasItem(2300000)) { // bait
                getClient().announceHint("It appears you have run out of the correct bait.", 500);
                cancelFishingTask();
                return;
            }
            MapleInventoryManipulator.removeById(getClient(), MapleInventoryType.USE, 2300000, 1, false, false);
            int randVal = fish.getInstance().getFishingReward();

            switch (randVal) {
                case 0 -> { // Meso
                    final int money = Randomizer.rand(10, 25000);
                    getClient().announceHint("[Fishing] You have gained " + money + " mesos!", 500);
                    gainMeso(money, true);
                }
                case 1 -> { // EXP
                    final int lowGain = Randomizer.rand(1, 500);
                    final int highGain = Randomizer.rand(4000, 10000);
                    int expGain = getLevel() > 50 ? highGain : lowGain;
                    gainExp(expGain, true, true, true);
                    getClient().announceHint("[Fishing] You have gained " + expGain + "exp!", 500);
                }
                default -> {
                    MapleInventoryManipulator.addById(getClient(), randVal, (short) 1);
                    getClient().announceHint(
                            "[Fishing] You have reeled in one " + MapleItemInformationProvider.getInstance().getName(randVal), 500);
                }
            }
        }, time, time);
    }

    /**
     * Clear the task upon a successful interval of fishing
     */
    public void cancelFishingTask() {
        //mayInterruptIfRunning – true if the thread executing this task should be interrupted;
        // otherwise, in-progress tasks are allowed to complete
        if (fishing != null) {
            fishing.cancel(true);
        }

        //fisher.getMap().broadcastMessage(CField.Packet.onDestroyClock());
    }

    public int getDistanceHackCounter() {
        return distanceHackCounter;
    }

    public void setDistanceHackCounter(int distanceHackCounter) {
        this.distanceHackCounter = distanceHackCounter;
    }

    public void incrementDistanceHackCounter() {
        this.distanceHackCounter++;
    }

    protected static class MapleBuffStatValueHolder {

        public MapleStatEffect effect;
        public long startTime;
        public BuffValueHolder value;
        public ScheduledFuture<?> schedule;

        public MapleBuffStatValueHolder(
                MapleStatEffect effect, long startTime,
                ScheduledFuture<?> schedule, BuffValueHolder value) {
            super();
            this.effect = effect;
            this.startTime = startTime;
            this.schedule = schedule;
            this.value = value;
        }
    }

    public static class MapleCoolDownValueHolder {
        public int skillId;
        public long startTime, length;
        public ScheduledFuture<?> timer;

        public MapleCoolDownValueHolder(int skillId, long startTime,
                                        long length, ScheduledFuture<?> timer) {
            super();
            this.skillId = skillId;
            this.startTime = startTime;
            this.length = length;
            this.timer = timer;
        }
    }
}
