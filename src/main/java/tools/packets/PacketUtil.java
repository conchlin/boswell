package tools.packets;

import client.*;
import client.inventory.*;
import client.newyear.NewYearCardRecord;
import constants.ExpTable;
import constants.GameConstants;
import constants.ItemConstants;
import database.tables.CharactersTbl;
import net.server.PlayerCoolDownValueHolder;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import server.MapleItemInformationProvider;
import server.maps.MapleMiniGame;
import server.maps.MaplePlayerShop;
import server.movement.LifeMovementFragment;
import server.skills.PlayerSkill;
import tools.Pair;
import tools.Randomizer;
import tools.StringUtil;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

import java.util.*;

public class PacketUtil {

    /**
     * This is a collection of supplemental methods that aid packet building. These are shared/used in many
     * of the packet structures found previously in tools/MaplePacketCreator.java which now are in
     * kotlin/network/packet/*. Eventually this code should be refactored or rewritten in kotlin.
     */

    // normalize with timezone offset suggested by Ari
    private final static long FT_UT_OFFSET =
            116444736010800000L + (10000L * TimeZone.getDefault().getOffset(System.currentTimeMillis()));
    private final static long DEFAULT_TIME = 150842304000000000L; //00 80 05 BB 46 E6 17 02
    public final static long ZERO_TIME = 94354848000000000L; //00 40 E0 FD 3B 37 4F 01
    private final static long PERMANENT = 150841440000000000L; // 00 C0 9B 90 7D E5 17 02

    public static long getTime(long utcTimestamp) {
        if (utcTimestamp < 0 && utcTimestamp >= -3) {
            if (utcTimestamp == -1) {
                return DEFAULT_TIME;    //high number ll
            } else if (utcTimestamp == -2) {
                return ZERO_TIME;
            } else {
                return PERMANENT;
            }
        }
        return utcTimestamp * 10000 + FT_UT_OFFSET;
    }

    public static void addCharEntry(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean viewall) {
        addCharStats(mplew, chr);
        addCharLook(mplew, chr, false);
        if (!viewall) {
            mplew.write(0);
        }
        if (chr.isGM()) {
            mplew.write(0);
            return;
        }
        mplew.write(1); // world rank enabled (next 4 ints are not sent if disabled) Short??
        mplew.writeInt(chr.getPlayerRank()); // world rank
        mplew.writeInt(chr.getRankMove()); // move (negative is downwards)
        mplew.writeInt(chr.getJobRank()); // job rank
        mplew.writeInt(chr.getJobRankMove()); // move (negative is downwards)
    }

     private static void addCharStats(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getId()); // character id
        mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getName(), '\0', 13));
        mplew.write(chr.getGender()); // gender (0 = male, 1 = female)
        mplew.write(chr.getSkinColor().getId()); // skin color
        mplew.writeInt(chr.getFace()); // face
        mplew.writeInt(chr.getHair()); // hair

        for (int i = 0; i < 3; i++) {
            MaplePet pet = chr.getPet(i);
            if (pet != null) //Checked GMS.. and your pets stay when going into the cash shop.
            {
                mplew.writeLong(pet.getUniqueId());
            } else {
                mplew.writeLong(0);
            }
        }

        mplew.write(chr.getLevel()); // level
        mplew.writeShort(chr.getJob().getId()); // job
        mplew.writeShort(chr.getStr()); // str
        mplew.writeShort(chr.getDex()); // dex
        mplew.writeShort(chr.getInt()); // int
        mplew.writeShort(chr.getLuk()); // luk
        mplew.writeShort(chr.getHp()); // hp (?)
        mplew.writeShort(chr.getClientMaxHp()); // maxhp
        mplew.writeShort(chr.getMp()); // mp (?)
        mplew.writeShort(chr.getClientMaxMp()); // maxmp
        mplew.writeShort(chr.getRemainingAp()); // remaining ap
        if (GameConstants.hasSPTable(chr.getJob())) {
            addRemainingSkillInfo(mplew, chr);
        } else {
            mplew.writeShort(chr.getRemainingSp()); // remaining sp
        }
        mplew.writeInt(chr.getExp()); // current exp
        mplew.writeShort(chr.getFame()); // fame
        mplew.writeInt(chr.getGachaExp()); //Gacha Exp
        mplew.writeInt(chr.getMapId()); // current map id
        mplew.write(chr.getInitialSpawnpoint()); // spawnpoint
        mplew.writeInt(0);
    }

    public static void addCharLook(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean mega) {
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor().getId()); // skin color
        mplew.writeInt(chr.getFace()); // face
        mplew.write(mega ? 0 : 1);
        mplew.writeInt(chr.getHair()); // hair
        addCharEquips(mplew, chr);
    }

    private static void addRemainingSkillInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        int[] remainingSp = chr.getRemainingSps();
        int effectiveLength = 0;
        for (int j : remainingSp) {
            if (j > 0) {
                effectiveLength++;
            }
        }

        mplew.write(effectiveLength);
        for (int i = 0; i < remainingSp.length; i++) {
            if (remainingSp[i] > 0) {
                mplew.write(i + 1);
                mplew.write(remainingSp[i]);
            }
        }
    }

    public static void addCharacterInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeLong(-1);
        mplew.write(0);
        addCharStats(mplew, chr);
        mplew.write(chr.getBuddylist().getCapacity());

        if (chr.getLinkedName() == null) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(chr.getLinkedName());
        }

        mplew.writeInt(chr.getMeso());
        addInventoryInfo(mplew, chr);
        addSkillInfo(mplew, chr);
        addQuestInfo(mplew, chr);
        mplew.writeShort(0); // addMiniGameInfo
        addRingInfo(mplew, chr);
        addTeleportInfo(mplew, chr);
        addMonsterBookInfo(mplew, chr);
        addNewYearInfo(mplew, chr);
        addAreaInfo(mplew, chr);//assuming it stayed here xd
        mplew.writeShort(0);
    }

    private static void addNewYearInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        Set<NewYearCardRecord> received = chr.getReceivedNewYearRecords();

        mplew.writeShort(received.size());
        for (NewYearCardRecord nyc : received) {
            encodeNewYearCard(nyc, mplew);
        }
    }

    private static void addTeleportInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        final List<Integer> tele = chr.getTrockMaps();
        final List<Integer> viptele = chr.getVipTrockMaps();
        for (int i = 0; i < 5; i++) {
            mplew.writeInt(tele.get(i));
        }
        for (int i = 0; i < 10; i++) {
            mplew.writeInt(viptele.get(i));
        }
    }

    private static void addAreaInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        Map<Short, String> areaInfos = chr.getAreaInfos();
        mplew.writeShort(areaInfos.size());
        for (Short area : areaInfos.keySet()) {
            mplew.writeShort(area);
            mplew.writeMapleAsciiString(areaInfos.get(area));
        }
    }

    private static void addCharEquips(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);
        Collection<Item> ii = MapleItemInformationProvider.getInstance().canWearEquipment(chr, equip.list());
        Map<Short, Integer> myEquip = new LinkedHashMap<>();
        Map<Short, Integer> maskedEquip = new LinkedHashMap<>();
        for (Item item : ii) {
            short pos = (byte) (item.getPosition() * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, item.getItemId());
            } else if (pos > 100 && pos != 111) { // don't ask. o.o
                pos -= 100;
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, item.getItemId());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, item.getItemId());
            }
        }
        for (Map.Entry<Short, Integer> entry : myEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        for (Map.Entry<Short, Integer> entry : maskedEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        Item cWeapon = equip.getItem((short) -111);
        mplew.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
        for (int i = 0; i < 3; i++) {
            if (chr.getPet(i) != null) {
                mplew.writeInt(chr.getPet(i).getItemId());
            } else {
                mplew.writeInt(0);
            }
        }
    }

    private static void addQuestInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeShort(chr.getStartedQuestsSize());
        for (MapleQuestStatus q : chr.getStartedQuests()) {
            mplew.writeShort(q.getQuest().getId());
            mplew.writeMapleAsciiString(q.getQuestData());
            if (q.getQuest().getInfoNumber() > 0) {
                mplew.writeShort(q.getQuest().getInfoNumber());
                mplew.writeMapleAsciiString(q.getQuestData());
            }
        }
        List<MapleQuestStatus> completed = chr.getCompletedQuests();
        mplew.writeShort(completed.size());
        for (MapleQuestStatus q : completed) {
            mplew.writeShort(q.getQuest().getId());
            mplew.writeLong(PacketUtil.getTime(q.getCompletionTime()));
        }
    }

    private static void addInventoryInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        for (byte i = 1; i <= 5; i++) {
            mplew.write(chr.getInventory(Objects.requireNonNull(MapleInventoryType.getByType(i))).getSlotLimit());
        }
        mplew.writeLong(PacketUtil.getTime(-2));
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        Collection<Item> equippedC = iv.list();
        List<Item> equipped = new ArrayList<>(equippedC.size());
        List<Item> equippedCash = new ArrayList<>(equippedC.size());
        for (Item item : equippedC) {
            if (item.getPosition() <= -100) {
                equippedCash.add(item);
            } else {
                equipped.add(item);
            }
        }
        Collections.sort(equipped);
        for (Item item : equipped) {
            addItemInfo(mplew, item);
        }
        mplew.writeShort(0); // start of equip cash
        for (Item item : equippedCash) {
            addItemInfo(mplew, item);
        }
        mplew.writeShort(0); // start of equip inventory
        for (Item item : chr.getInventory(MapleInventoryType.EQUIP).list()) {
            addItemInfo(mplew, item);
        }
        mplew.writeInt(0);
        for (Item item : chr.getInventory(MapleInventoryType.USE).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        for (Item item : chr.getInventory(MapleInventoryType.SETUP).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        for (Item item : chr.getInventory(MapleInventoryType.ETC).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        for (Item item : chr.getInventory(MapleInventoryType.CASH).list()) {
            addItemInfo(mplew, item);
        }
    }

    private static void addSkillInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.write(0); // start of skills
        Map<PlayerSkill, MapleCharacter.SkillEntry> skills = chr.getSkills();
        int skillsSize = skills.size();
        // We don't want to include any hidden skill in this, so subtract them from the size list and ignore them.
        for (Map.Entry<PlayerSkill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
            if (GameConstants.isHiddenSkills(skill.getKey().getId())) {
                skillsSize--;
            }
        }
        mplew.writeShort(skillsSize);
        for (Map.Entry<PlayerSkill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
            if (GameConstants.isHiddenSkills(skill.getKey().getId())) {
                continue;
            }
            mplew.writeInt(skill.getKey().getId());
            mplew.writeInt(skill.getValue().skillevel);
            mplew.writeLong(PacketUtil.getTime(skill.getValue().expiration));
            if (skill.getKey().isFourthJob()) {
                mplew.writeInt(skill.getValue().masterlevel);
            }
        }
        mplew.writeShort(chr.getAllCooldowns().size());
        for (PlayerCoolDownValueHolder cooling : chr.getAllCooldowns()) {
            mplew.writeInt(cooling.skillId);
            int timeLeft = (int) (cooling.length + cooling.startTime - System.currentTimeMillis());
            mplew.writeShort(timeLeft / 1000);
        }
    }

    private static void addMonsterBookInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getMonsterBookCover()); // cover
        mplew.write(0);
        Map<Integer, Integer> cards = chr.getMonsterBook().getCards();
        mplew.writeShort(cards.size());
        for (Map.Entry<Integer, Integer> all : cards.entrySet()) {
            mplew.writeShort(all.getKey() % 10000); // Id
            mplew.write(all.getValue()); // Level
        }
    }

    public static void encodeNewYearCardInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        Set<NewYearCardRecord> newyears = chr.getReceivedNewYearRecords();
        if (!newyears.isEmpty()) {
            mplew.write(1);

            mplew.writeInt(newyears.size());
            for (NewYearCardRecord nyc : newyears) {
                mplew.writeInt(nyc.getId());
            }
        } else {
            mplew.write(0);
        }
    }

    public static void encodeNewYearCard(NewYearCardRecord newyear, MaplePacketLittleEndianWriter mplew) {
        mplew.writeInt(newyear.getId());
        mplew.writeInt(newyear.getSenderId());
        mplew.writeMapleAsciiString(newyear.getSenderName());
        mplew.writeBool(newyear.isSenderCardDiscarded());
        mplew.writeLong(newyear.getDateSent());
        mplew.writeInt(newyear.getReceiverId());
        mplew.writeMapleAsciiString(newyear.getReceiverName());
        mplew.writeBool(newyear.isReceiverCardDiscarded());
        mplew.writeBool(newyear.isReceiverCardReceived());
        mplew.writeLong(newyear.getDateReceived());
        mplew.writeMapleAsciiString(newyear.getMessage());
    }

    private static void addRingInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeShort(chr.getCrushRings().size());
        for (MapleRing ring : chr.getCrushRings()) {
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(PacketUtil.getRightPaddedStr(ring.getPartnerName(), '\0', 13));
            mplew.writeInt(ring.getRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getPartnerRingId());
            mplew.writeInt(0);
        }
        mplew.writeShort(chr.getFriendshipRings().size());
        for (MapleRing ring : chr.getFriendshipRings()) {
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(PacketUtil.getRightPaddedStr(ring.getPartnerName(), '\0', 13));
            mplew.writeInt(ring.getRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getPartnerRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getItemId());
        }

        if (chr.getPartnerId() > 0) {
            MapleRing marriageRing = chr.getMarriageRing();

            mplew.writeShort(1);
            mplew.writeInt(chr.getRelationshipId());
            mplew.writeInt(chr.getGender() == 0 ? chr.getId() : chr.getPartnerId());
            mplew.writeInt(chr.getGender() == 0 ? chr.getPartnerId() : chr.getId());
            mplew.writeShort((marriageRing != null) ? 3 : 1);
            if (marriageRing != null) {
                mplew.writeInt(marriageRing.getItemId());
                mplew.writeInt(marriageRing.getItemId());
            } else {
                mplew.writeInt(1112803); // Engagement Ring's Outcome (doesn't matter for engagement)
                mplew.writeInt(1112803); // Engagement Ring's Outcome (doesn't matter for engagement)
            }
            mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getGender() == 0 ? chr.getName() : CharactersTbl.loadNameById(chr.getPartnerId()), '\0', 13));
            mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getGender() == 0 ? CharactersTbl.loadNameById(chr.getPartnerId()) : chr.getName(), '\0', 13));
        } else {
            mplew.writeShort(0);
        }
    }

    public static void addRingLook(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean crush) {
        List<MapleRing> rings;
        if (crush) {
            rings = chr.getCrushRings();
        } else {
            rings = chr.getFriendshipRings();
        }
        boolean yes = false;
        for (MapleRing ring : rings) {
            if (ring.equipped()) {
                if (!yes) {
                    yes = true;
                    mplew.write(1);
                }
                mplew.writeInt(ring.getRingId());
                mplew.writeInt(0);
                mplew.writeInt(ring.getPartnerRingId());
                mplew.writeInt(0);
                mplew.writeInt(ring.getItemId());
            }
        }
        if (!yes) {
            mplew.write(0);
        }
    }

    public static void addMarriageRingLook(MapleClient target, final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        MapleRing ring = chr.getMarriageRing();

        if (ring == null || !ring.equipped()) {
            mplew.write(0);
        } else {
            mplew.write(1);

            MapleCharacter targetChr = target.getPlayer();
            if (targetChr != null && targetChr.getPartnerId() == chr.getId()) {
                mplew.writeInt(0);
                mplew.writeInt(0);
            } else {
                mplew.writeInt(chr.getId());
                mplew.writeInt(ring.getPartnerChrId());
            }

            mplew.writeInt(ring.getItemId());
        }
    }

    private static void addItemInfo(final MaplePacketLittleEndianWriter mplew, Item item) {
        addItemInfo(mplew, item, false);
    }

    public static void addItemInfoZeroPos(final MaplePacketLittleEndianWriter mplew, Item item) {
        addItemInfo(mplew, item, true);
    }

    protected static void addItemInfo(final MaplePacketLittleEndianWriter mplew, Item item, boolean zeroPosition) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        boolean isCash = ii.isCash(item.getItemId());
        boolean isPet = item.getPetId() > -1;
        boolean isRing = false;
        Equip equip = null;
        short pos = item.getPosition();
        byte itemType = item.getItemType();
        if (itemType == 1) {
            equip = (Equip) item;
            isRing = equip.getRingId() > -1;
        }
        if (!zeroPosition) {
            if (equip != null) {
                if (pos < 0) {
                    pos *= -1;
                }
                mplew.writeShort(pos > 100 ? pos - 100 : pos);
            } else {
                mplew.write(pos);
            }
        }
        mplew.write(itemType);
        mplew.writeInt(item.getItemId());
        mplew.writeBool(isCash);
        if (isCash) {
            mplew.writeLong(isPet ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId());
        }
        mplew.writeLong(PacketUtil.getTime(item.getExpiration()));
        if (isPet) {
            MaplePet pet = item.getPet();
            mplew.writeAsciiString(StringUtil.getRightPaddedStr(pet.getName(), '\0', 13));
            mplew.write(pet.getLevel());
            mplew.writeShort(pet.getCloseness());
            mplew.write(pet.getFullness());
            mplew.writeLong(PacketUtil.getTime(item.getExpiration()));
            mplew.writeInt(pet.getPetFlag());
            /* pet flags found by -- lrenex & Spoon */

            mplew.write(new byte[]{(byte) 0x50, (byte) 0x46}); //wonder what this is
            mplew.writeInt(0);
            return;
        }
        if (equip == null) {
            mplew.writeShort(item.getQuantity());
            mplew.writeMapleAsciiString(item.getOwner());
            mplew.writeShort(item.getFlag()); // flag

            if (ItemConstants.isRechargeable(item.getItemId())) {
                mplew.writeInt(2);
                mplew.write(new byte[]{(byte) 0x54, 0, 0, (byte) 0x34});
            }
            return;
        }
        mplew.write(equip.getUpgradeSlots()); // upgrade slots
        mplew.write(equip.getLevel()); // level
        mplew.writeShort(equip.getStr()); // str
        mplew.writeShort(equip.getDex()); // dex
        mplew.writeShort(equip.getInt()); // int
        mplew.writeShort(equip.getLuk()); // luk
        mplew.writeShort(equip.getHp()); // hp
        mplew.writeShort(equip.getMp()); // mp
        mplew.writeShort(equip.getWatk()); // watk
        mplew.writeShort(equip.getMatk()); // matk
        mplew.writeShort(equip.getWdef()); // wdef
        mplew.writeShort(equip.getMdef()); // mdef
        mplew.writeShort(equip.getAcc()); // accuracy
        mplew.writeShort(equip.getAvoid()); // avoid
        mplew.writeShort(equip.getHands()); // hands
        mplew.writeShort(equip.getSpeed()); // speed
        mplew.writeShort(equip.getJump()); // jump
        mplew.writeMapleAsciiString(equip.getOwner()); // owner name
        mplew.writeShort(equip.getFlag()); //Item Flags

        if (isCash) {
            for (int i = 0; i < 10; i++) {
                mplew.write(0x40);
            }
        } else {
            int itemLevel = equip.getItemLevel();

            long expNibble = (ExpTable.INSTANCE.getExpNeededForLevel(ii.getEquipLevelReq(item.getItemId())) * equip.getItemExp());
            expNibble /= ExpTable.INSTANCE.getEquipExpNeededForLevel(itemLevel);

            mplew.write(0);
            mplew.write(itemLevel); //Item Level
            mplew.writeInt((int) expNibble);
            mplew.writeInt(equip.getVicious()); //WTF NEXON ARE YOU SERIOUS?
            mplew.writeLong(0);
        }
        mplew.writeLong(PacketUtil.getTime(-2));
        mplew.writeInt(-1);

    }

    public static void getGuildInfo(final MaplePacketLittleEndianWriter mplew, MapleGuild guild) {
        mplew.writeInt(guild.getId());
        mplew.writeMapleAsciiString(guild.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(guild.getRankTitle(i));
        }
        Collection<MapleGuildCharacter> members = guild.getMembers();
        mplew.write(members.size());
        for (MapleGuildCharacter mgc : members) {
            mplew.writeInt(mgc.getId());
        }
        for (MapleGuildCharacter mgc : members) {
            mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
            mplew.writeInt(mgc.getJobId());
            mplew.writeInt(mgc.getLevel());
            mplew.writeInt(mgc.getGuildRank());
            mplew.writeInt(mgc.isOnline() ? 1 : 0);
            mplew.writeInt(guild.getSignature());
            mplew.writeInt(mgc.getAllianceRank());
        }
        mplew.writeInt(guild.getCapacity());
        mplew.writeShort(guild.getLogoBG());
        mplew.write(guild.getLogoBGColor());
        mplew.writeShort(guild.getLogo());
        mplew.write(guild.getLogoColor());
        mplew.writeMapleAsciiString(guild.getNotice());
        mplew.writeInt(guild.getGP());
        mplew.writeInt(guild.getAllianceId());
    }

    public static void addPartyStatus(int forchannel, MapleParty party, LittleEndianWriter lew, boolean leaving) {
        List<MaplePartyCharacter> partymembers = new ArrayList<>(party.getMembers());
        while (partymembers.size() < 6) {
            partymembers.add(new MaplePartyCharacter());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeAsciiString(PacketUtil.getRightPaddedStr(partychar.getName(), '\0', 13));
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getJobId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getLevel());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.isOnline()) {
                lew.writeInt(partychar.getChannel() - 1);
            } else {
                lew.writeInt(-2);
            }
        }
        lew.writeInt(party.getLeader().getId());
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel) {
                lew.writeInt(partychar.getMapId());
            } else {
                lew.writeInt(0);
            }
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel && !leaving) {
                if (partychar.getDoor() != null) {
                    lew.writeInt(partychar.getDoor().getTown().getId());
                    lew.writeInt(partychar.getDoor().getTarget().getId());
                    lew.writeInt(partychar.getDoor().getTargetPosition().x);
                    lew.writeInt(partychar.getDoor().getTargetPosition().y);
                } else {
                    lew.writeInt(999999999);
                    lew.writeInt(999999999);
                    lew.writeInt(0);
                    lew.writeInt(0);
                }
            } else {
                lew.writeInt(999999999);
                lew.writeInt(999999999);
                lew.writeInt(0);
                lew.writeInt(0);
            }
        }
    }

    public static void addPetInfo(final MaplePacketLittleEndianWriter mplew, MaplePet pet, boolean showpet) {
        mplew.write(1);
        if (showpet) mplew.write(0);

        mplew.writeInt(pet.getItemId());
        mplew.writeMapleAsciiString(pet.getName());
        mplew.writeLong(pet.getUniqueId());
        mplew.writePos(pet.getPos());
        mplew.write(pet.getStance());
        mplew.writeInt(pet.getFh());
    }

    public static void writeForeignBuffs(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        int[] buffmask = new int[4];
        List<Pair<Integer, Integer>> buffList = new ArrayList<>();

        buffmask[MapleBuffStat.ENERGY_CHARGE.getSet()] |= MapleBuffStat.ENERGY_CHARGE.getMask();
        buffmask[MapleBuffStat.DASH_SPEED.getSet()] |= MapleBuffStat.DASH_SPEED.getMask();
        buffmask[MapleBuffStat.DASH_JUMP.getSet()] |= MapleBuffStat.DASH_JUMP.getMask();
        buffmask[MapleBuffStat.MONSTER_RIDING.getSet()] |= MapleBuffStat.MONSTER_RIDING.getMask();
        buffmask[MapleBuffStat.SPEED_INFUSION.getSet()] |= MapleBuffStat.SPEED_INFUSION.getMask();
        buffmask[MapleBuffStat.HOMING_BEACON.getSet()] |= MapleBuffStat.HOMING_BEACON.getMask();
        buffmask[MapleBuffStat.ZOMBIFY.getSet()] |= MapleBuffStat.ZOMBIFY.getMask();

        if (chr.getBuffedValue(MapleBuffStat.SPEED) != null) {
            buffmask[MapleBuffStat.SPEED.getSet()] |= MapleBuffStat.SPEED.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.SPEED), 1));
        }
        if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
            buffmask[MapleBuffStat.COMBO.getSet()] |= MapleBuffStat.COMBO.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.COMBO), 1));
        }
        if (chr.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) {
            buffmask[MapleBuffStat.WK_CHARGE.getSet()] |= MapleBuffStat.WK_CHARGE.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.WK_CHARGE), 4));
        }
        if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
            buffmask[MapleBuffStat.SHADOWPARTNER.getSet()] |= MapleBuffStat.SHADOWPARTNER.getMask();
        }
        if (chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null) {
            buffmask[MapleBuffStat.DARKSIGHT.getSet()] |= MapleBuffStat.DARKSIGHT.getMask();
        }
        if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
            buffmask[MapleBuffStat.SOULARROW.getSet()] |= MapleBuffStat.SOULARROW.getMask();
        }
        if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
            buffmask[MapleBuffStat.MORPH.getSet()] |= MapleBuffStat.MORPH.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.MORPH), 2));
        }
        if (chr.getBuffedValue(MapleBuffStat.GHOST_MORPH) != null) {
            buffmask[MapleBuffStat.GHOST_MORPH.getSet()] |= MapleBuffStat.GHOST_MORPH.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.GHOST_MORPH), 2));
        }
        if (chr.getBuffedValue(MapleBuffStat.SEDUCE) != null) {
            buffmask[MapleBuffStat.SEDUCE.getSet()] |= MapleBuffStat.SEDUCE.getMask();
            buffList.add(new Pair<>(chr.getBuffSourceLevel(MapleBuffStat.SEDUCE), 2));
            buffList.add(new Pair<>(chr.getBuffSource(MapleBuffStat.SEDUCE), 2));
        }
        if (chr.getBuffedValue(MapleBuffStat.SHADOW_CLAW) != null) {
            buffmask[MapleBuffStat.SHADOW_CLAW.getSet()] |= MapleBuffStat.SHADOW_CLAW.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.SHADOW_CLAW), 4));
        }
        if (chr.getBuffedValue(MapleBuffStat.BAN_MAP) != null) {
            buffmask[MapleBuffStat.BAN_MAP.getSet()] |= MapleBuffStat.BAN_MAP.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.BAN_MAP), 4));
        }
        if (chr.getBuffedValue(MapleBuffStat.BARRIER) != null) {
            buffmask[MapleBuffStat.BARRIER.getSet()] |= MapleBuffStat.BARRIER.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.BARRIER), 4));
        }
        if (chr.getBuffedValue(MapleBuffStat.DOJANG_SHIELD) != null) {
            buffmask[MapleBuffStat.DOJANG_SHIELD.getSet()] |= MapleBuffStat.DOJANG_SHIELD.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.DOJANG_SHIELD), 4));
        }
        if (chr.getBuffedValue(MapleBuffStat.CONFUSE) != null) {
            buffmask[MapleBuffStat.CONFUSE.getSet()] |= MapleBuffStat.CONFUSE.getMask();
            buffList.add(new Pair<>(chr.getBuffSourceLevel(MapleBuffStat.CONFUSE), 2));
            buffList.add(new Pair<>(chr.getBuffSource(MapleBuffStat.CONFUSE), 2));
        }
        if (chr.getBuffedValue(MapleBuffStat.RESPECT_PIMMUNE) != null) {
            buffmask[MapleBuffStat.RESPECT_PIMMUNE.getSet()] |= MapleBuffStat.RESPECT_PIMMUNE.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.RESPECT_PIMMUNE), 4));
        }
        if (chr.getBuffedValue(MapleBuffStat.RESPECT_MIMMUNE) != null) {
            buffmask[MapleBuffStat.RESPECT_MIMMUNE.getSet()] |= MapleBuffStat.RESPECT_MIMMUNE.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.RESPECT_MIMMUNE), 4));
        }
        if (chr.getBuffedValue(MapleBuffStat.DEFENSE_ATT) != null) {
            buffmask[MapleBuffStat.DEFENSE_ATT.getSet()] |= MapleBuffStat.DEFENSE_ATT.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.DEFENSE_ATT), 4));
        }
        if (chr.getBuffedValue(MapleBuffStat.DEFENSE_STATE) != null) {
            buffmask[MapleBuffStat.DEFENSE_STATE.getSet()] |= MapleBuffStat.DEFENSE_STATE.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.DEFENSE_STATE), 4));
        }
        if (chr.getBuffedValue(MapleBuffStat.BERSERK_FURY) != null) {
            buffmask[MapleBuffStat.BERSERK_FURY.getSet()] |= MapleBuffStat.BERSERK_FURY.getMask();
        }
        if (chr.getBuffedValue(MapleBuffStat.DIVINE_BODY) != null) {
            buffmask[MapleBuffStat.DIVINE_BODY.getSet()] |= MapleBuffStat.DIVINE_BODY.getMask();
        }
        if (chr.getBuffedValue(MapleBuffStat.WIND_WALK) != null) {
            buffmask[MapleBuffStat.WIND_WALK.getSet()] |= MapleBuffStat.WIND_WALK.getMask();
        }
        if (chr.getBuffedValue(MapleBuffStat.REPEAT_EFFECT) != null) {
            buffmask[MapleBuffStat.REPEAT_EFFECT.getSet()] |= MapleBuffStat.REPEAT_EFFECT.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.REPEAT_EFFECT), 4));
        }
        if (chr.getBuffedValue(MapleBuffStat.STOP_POTION) != null) {
            buffmask[MapleBuffStat.STOP_POTION.getSet()] |= MapleBuffStat.STOP_POTION.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.STOP_POTION), 4));
        }
        if (chr.getBuffedValue(MapleBuffStat.STOP_MOTION) != null) {
            buffmask[MapleBuffStat.STOP_MOTION.getSet()] |= MapleBuffStat.STOP_MOTION.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.STOP_MOTION), 4));
        }
        if (chr.getBuffedValue(MapleBuffStat.FEAR) != null) {
            buffmask[MapleBuffStat.FEAR.getSet()] |= MapleBuffStat.FEAR.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.FEAR), 4));
        }
        if (chr.getBuffedValue(MapleBuffStat.STUN) != null) {
            buffmask[MapleBuffStat.STUN.getSet()] |= MapleBuffStat.STUN.getMask();
            buffList.add(new Pair<>(chr.getBuffSourceLevel(MapleBuffStat.STUN), 2));
            buffList.add(new Pair<>(chr.getBuffSource(MapleBuffStat.STUN), 2));
        }
        if (chr.getBuffedValue(MapleBuffStat.DARKNESS) != null) {
            buffmask[MapleBuffStat.DARKNESS.getSet()] |= MapleBuffStat.DARKNESS.getMask();
            buffList.add(new Pair<>(chr.getBuffSourceLevel(MapleBuffStat.DARKNESS), 2));
            buffList.add(new Pair<>(chr.getBuffSource(MapleBuffStat.DARKNESS), 2));
        }
        if (chr.getBuffedValue(MapleBuffStat.SEAL) != null) {
            buffmask[MapleBuffStat.SEAL.getSet()] |= MapleBuffStat.SEAL.getMask();
            buffList.add(new Pair<>(chr.getBuffSourceLevel(MapleBuffStat.SEAL), 2));
            buffList.add(new Pair<>(chr.getBuffSource(MapleBuffStat.SEAL), 2));
        }
        if (chr.getBuffedValue(MapleBuffStat.WEAKEN) != null) {
            buffmask[MapleBuffStat.WEAKEN.getSet()] |= MapleBuffStat.WEAKEN.getMask();
            buffList.add(new Pair<>(chr.getBuffSourceLevel(MapleBuffStat.WEAKEN), 2));
            buffList.add(new Pair<>(chr.getBuffSource(MapleBuffStat.WEAKEN), 2));
        }
        if (chr.getBuffedValue(MapleBuffStat.CURSE) != null) {
            buffmask[MapleBuffStat.CURSE.getSet()] |= MapleBuffStat.CURSE.getMask();
            buffList.add(new Pair<>(chr.getBuffSourceLevel(MapleBuffStat.CURSE), 2));
            buffList.add(new Pair<>(chr.getBuffSource(MapleBuffStat.CURSE), 2));
        }
        if (chr.getBuffedValue(MapleBuffStat.POISON) != null) {
            buffmask[MapleBuffStat.POISON.getSet()] |= MapleBuffStat.POISON.getMask();
            buffList.add(new Pair<>(chr.getBuffedValue(MapleBuffStat.POISON), 2));
            buffList.add(new Pair<>(chr.getBuffSourceLevel(MapleBuffStat.POISON), 2));
            buffList.add(new Pair<>(chr.getBuffSource(MapleBuffStat.POISON), 2));
        }

        for (int i = 3; i >= 0; i--) {
            mplew.writeInt(buffmask[i]);
        }
        for (Pair<Integer, Integer> buff : buffList) {
            if (buff.right == 4) {
                mplew.writeInt(buff.left);
            } else if (buff.right == 2) {
                mplew.writeShort(buff.left);
            } else if (buff.right == 1) {
                mplew.write(buff.left);
            }
        }

        int randomTcur = Randomizer.nextInt();
        // Energy Charge
        mplew.skip(9);
        mplew.writeInt(randomTcur);
        mplew.writeShort(0);

        // Dash Speed
        mplew.skip(9);
        mplew.writeInt(randomTcur);
        mplew.writeShort(0);

        // Dash Jump
        mplew.skip(9);
        mplew.writeInt(randomTcur);
        mplew.writeShort(0);

        // Monster Riding
        if (chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            mplew.writeInt(chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING));
            mplew.writeInt(chr.getBuffSource(MapleBuffStat.MONSTER_RIDING));
        } else {
            mplew.writeLong(0);
        }
        mplew.write(1);
        mplew.writeInt(randomTcur);//tCur

        // Speed Infusion (party booster)
        mplew.skip(9);
        mplew.writeInt(randomTcur);
        mplew.write(0);
        mplew.writeInt(randomTcur);
        mplew.writeShort(0);
        // Homing Beacon (Guided bullet)
        mplew.skip(9);
        mplew.writeInt(randomTcur);
        mplew.writeInt(0);
        // Zombify  (undead)
        mplew.skip(9);
        mplew.writeInt(randomTcur);
        mplew.writeShort(0);

        mplew.writeShort(0);
    }

    /**
     * Adds an announcement box to an existing MaplePacketLittleEndianWriter.
     *
     * @param mplew The MaplePacketLittleEndianWriter to add an announcement box
     * to.
     * @param shop The shop to announce.
     */
    public static void addAnnounceBox(final MaplePacketLittleEndianWriter mplew, MaplePlayerShop shop, int availability) {
        mplew.write(4);
        mplew.writeInt(shop.getObjectId());
        mplew.writeMapleAsciiString(shop.getDescription());
        mplew.write(0);
        mplew.write(0);
        mplew.write(1);
        mplew.write(availability);
        mplew.write(0);
    }

    public static void addAnnounceBox(final MaplePacketLittleEndianWriter mplew, MapleMiniGame game, int ammount, int joinable) {
        mplew.write(game.getGameType().getValue());
        mplew.writeInt(game.getObjectId()); // gameid/shopid
        mplew.writeMapleAsciiString(game.getDescription()); // desc
        mplew.writeBool(!game.getPassword().isEmpty());    // password here, thanks GabrielSin!
        mplew.write(game.getPieceType());
        mplew.write(ammount);
        mplew.write(2);         //player capacity
        mplew.write(joinable);
    }

    public static void serializeMovementList(LittleEndianWriter lew, List<LifeMovementFragment> moves) {
        lew.write(moves.size());
        for (LifeMovementFragment move : moves) {
            move.serialize(lew);
        }
    }

    public static String getRightPaddedStr(String in, char padchar, int length) {
        return in + String.valueOf(padchar).repeat(Math.max(0, length - in.length()));
    }

    // someone thought it was a good idea to handle floating point representation through packets ROFL
    public static int doubleToShortBits(double d) {
        return (int) (Double.doubleToLongBits(d) >> 48);
    }

    public static Pair<Integer, Integer> normalizedCustomMaxHP(long currHP, long maxHP) {
        int sendHP, sendMaxHP;

        if (maxHP <= Integer.MAX_VALUE) {
            sendHP = (int) currHP;
            sendMaxHP = (int) maxHP;
        } else {
            float f = ((float) currHP) / maxHP;

            sendHP = (int) (Integer.MAX_VALUE * f);
            sendMaxHP = Integer.MAX_VALUE;
        }

        return new Pair<>(sendHP, sendMaxHP);
    }
}
