/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    Copyleft (L) 2016 - 2018 RonanLana (HeavenMS)

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
package client.processor;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import server.skills.PlayerSkill;
import server.skills.Skill;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ServerConstants;
import constants.skills.BlazeWizard;
import constants.skills.Brawler;
import constants.skills.DawnWarrior;
import constants.skills.Magician;
import constants.skills.Warrior;

import java.util.Collection;

import server.ThreadManager;
import server.skills.SkillFactory;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

public class AssignAPProcessor {

    public static void APAutoAssignAction(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr.getRemainingAp() < 1) return;

        Collection<Item> equippedC = chr.getInventory(MapleInventoryType.EQUIPPED).list();

        c.lockClient();
        try {
            int[] statGain = new int[4];
            int[] statUpdate = new int[4];
            statGain[0] = 0;
            statGain[1] = 0;
            statGain[2] = 0;
            statGain[3] = 0;

            int remainingAp = chr.getRemainingAp();
            slea.skip(8);


            if (slea.available() < 16) {
                AutobanFactory.PACKET_EDIT.alert(chr, "Didn't send full packet for Auto Assign.");

                final MapleClient client = c;
                ThreadManager.getInstance().newTask(new Runnable() {
                    @Override
                    public void run() {
                        client.disconnect(false, false);
                    }
                });

                return;
            }

            for (int i = 0; i < 2; i++) {
                int type = slea.readInt();
                int tempVal = slea.readInt();
                if (tempVal < 0 || tempVal > remainingAp) {
                    return;
                }

                gainStatByType(MapleStat.getBy5ByteEncoding(type), statGain, tempVal, statUpdate);
            }

            chr.assignStrDexIntLuk(statGain[0], statGain[1], statGain[3], statGain[2]);
            c.announce(MaplePacketCreator.enableActions());
        } finally {
            c.unlockClient();
        }
    }

    private static int gainStatByType(MapleStat type, int[] statGain, int gain, int statUpdate[]) {
        if (gain <= 0) return 0;

        int newVal = 0;
        if (type.equals(MapleStat.STR)) {
            newVal = statUpdate[0] + gain;
            if (newVal > ServerConstants.MAX_AP) {
                statGain[0] += (gain - (newVal - ServerConstants.MAX_AP));
                statUpdate[0] = ServerConstants.MAX_AP;
            } else {
                statGain[0] += gain;
                statUpdate[0] = newVal;
            }
        } else if (type.equals(MapleStat.INT)) {
            newVal = statUpdate[3] + gain;
            if (newVal > ServerConstants.MAX_AP) {
                statGain[3] += (gain - (newVal - ServerConstants.MAX_AP));
                statUpdate[3] = ServerConstants.MAX_AP;
            } else {
                statGain[3] += gain;
                statUpdate[3] = newVal;
            }
        } else if (type.equals(MapleStat.LUK)) {
            newVal = statUpdate[2] + gain;
            if (newVal > ServerConstants.MAX_AP) {
                statGain[2] += (gain - (newVal - ServerConstants.MAX_AP));
                statUpdate[2] = ServerConstants.MAX_AP;
            } else {
                statGain[2] += gain;
                statUpdate[2] = newVal;
            }
        } else if (type.equals(MapleStat.DEX)) {
            newVal = statUpdate[1] + gain;
            if (newVal > ServerConstants.MAX_AP) {
                statGain[1] += (gain - (newVal - ServerConstants.MAX_AP));
                statUpdate[1] = ServerConstants.MAX_AP;
            } else {
                statGain[1] += gain;
                statUpdate[1] = newVal;
            }
        }

        if (newVal > ServerConstants.MAX_AP) {
            return newVal - ServerConstants.MAX_AP;
        }
        return 0;
    }

    private static MapleStat getQuaternaryStat(MapleJob stance) {
        if (stance != MapleJob.MAGICIAN) return MapleStat.INT;
        return MapleStat.STR;
    }

    public static boolean APResetAction(MapleClient c, int APFrom, int APTo) {
        c.lockClient();
        try {
            MapleCharacter player = c.getPlayer();

            switch (APFrom) {
                case 64 -> { // str
                    if (player.getStr() < 5) {
                        player.message("You don't have the minimum STR required to swap.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    if (!player.assignStr(-1)) {
                        player.message("Couldn't execute AP reset operation.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                }
                case 128 -> { // dex
                    if (player.getDex() < 5) {
                        player.message("You don't have the minimum DEX required to swap.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    if (!player.assignDex(-1)) {
                        player.message("Couldn't execute AP reset operation.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                }
                case 256 -> { // int
                    if (player.getInt() < 5) {
                        player.message("You don't have the minimum INT required to swap.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    if (!player.assignInt(-1)) {
                        player.message("Couldn't execute AP reset operation.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                }
                case 512 -> { // luk
                    if (player.getLuk() < 5) {
                        player.message("You don't have the minimum LUK required to swap.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    if (!player.assignLuk(-1)) {
                        player.message("Couldn't execute AP reset operation.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                }
                case 2048 -> { // HP
                    if (ServerConstants.USE_ENFORCE_HPMP_SWAP) {
                        if (APTo != 8192) {
                            player.message("You can only swap HP ability points to MP.");
                            c.announce(MaplePacketCreator.enableActions());
                            return false;
                        }
                    }
                    if (player.getHpMpApUsed() < 1) {
                        player.message("You don't have enough HPMP stat points to spend on AP Reset.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    int hp = player.getMaxHp();
                    int level_ = player.getLevel();
                    if (hp < level_ * 14 + 148) {
                        player.message("You don't have the minimum HP pool required to swap.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    int curHp = player.getHp();
                    int hplose = -takeHp(player.getJob());
                    player.assignHP(hplose, -1);
                    player.updateHp(Math.max(1, curHp + hplose));
                }
                case 8192 -> { // MP
                    if (ServerConstants.USE_ENFORCE_HPMP_SWAP) {
                        if (APTo != 2048) {
                            player.message("You can only swap MP ability points to HP.");
                            c.announce(MaplePacketCreator.enableActions());
                            return false;
                        }
                    }
                    if (player.getHpMpApUsed() < 1) {
                        player.message("You don't have enough HPMP stat points to spend on AP Reset.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    int mp = player.getMaxMp();
                    int level = player.getLevel();
                    MapleJob job = player.getJob();
                    boolean canWash = true;
                    if (job.isA(MapleJob.SPEARMAN) && mp < 4 * level + 156) {
                        canWash = false;
                    } else if ((job.isA(MapleJob.FIGHTER) || job.isA(MapleJob.ARAN1)) && mp < 4 * level + 56) {
                        canWash = false;
                    } else if (job.isA(MapleJob.THIEF) && job.getId() % 100 > 0 && mp < level * 14 - 4) {
                        canWash = false;
                    } else if (mp < level * 14 + 148) {
                        canWash = false;
                    }
                    if (!canWash) {
                        player.message("You don't have the minimum MP pool required to swap.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    int curMp = player.getMp();
                    int mplose = -takeMp(job);
                    player.assignMP(mplose, -1);
                    player.updateMp(Math.max(0, curMp + mplose));
                }
                default -> {
                    c.announce(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, player));
                    return false;
                }
            }

            addStat(player, APTo, true);
            return true;
        } finally {
            c.unlockClient();
        }
    }

    public static void APAssignAction(MapleClient c, int num) {
        c.lockClient();
        try {
            addStat(c.getPlayer(), num, false);
        } finally {
            c.unlockClient();
        }
    }

    private static boolean addStat(MapleCharacter chr, int apTo, boolean usedAPReset) {
        switch (apTo) {
            case 64:
                if (!chr.assignStr(1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.announce(MaplePacketCreator.enableActions());
                    return false;
                }
                break;
            case 128: // Dex
                if (!chr.assignDex(1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.announce(MaplePacketCreator.enableActions());
                    return false;
                }
                break;
            case 256: // Int
                if (!chr.assignInt(1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.announce(MaplePacketCreator.enableActions());
                    return false;
                }
                break;
            case 512: // Luk
                if (!chr.assignLuk(1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.announce(MaplePacketCreator.enableActions());
                    return false;
                }
                break;
            case 2048:
                if (!chr.assignHP(calcHpChange(chr, usedAPReset), 1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.announce(MaplePacketCreator.enableActions());
                    return false;
                }
                break;
            case 8192:
                if (!chr.assignMP(calcMpChange(chr, usedAPReset), 1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.announce(MaplePacketCreator.enableActions());
                    return false;
                }
                break;
            default:
                chr.announce(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, chr));
                return false;
        }
        return true;
    }

    private static int calcHpChange(MapleCharacter player, boolean usedAPReset) {
        MapleJob job = player.getJob();
        int MaxHP = 0;

        if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1)) {
            if (!usedAPReset) {
                PlayerSkill increaseHP = SkillFactory.getSkill(job.isA(MapleJob.DAWNWARRIOR1) ? DawnWarrior.MAX_HP_INCREASE : Warrior.IMPROVED_MAXHP);
                int sLvl = player.getSkillLevel(increaseHP);

                if (sLvl > 0)
                    MaxHP += increaseHP.getEffect(sLvl).getY();
            }

            if (usedAPReset) {
                MaxHP += 20;
            } else {
                MaxHP += Randomizer.rand(18, 22);
            }
        } else if (job.isA(MapleJob.ARAN1)) {
            if (usedAPReset) {
                MaxHP += 20;
            } else {
                MaxHP += Randomizer.rand(26, 30);
            }
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            if (usedAPReset) {
                MaxHP += 6;
            } else {
                MaxHP += Randomizer.rand(5, 9);
            }
        } else if (job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
            if (usedAPReset) {
                MaxHP += 16;
            } else {
                MaxHP += Randomizer.rand(14, 18);
            }
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1)) {
            if (usedAPReset) {
                MaxHP += 16;
            } else {
                MaxHP += Randomizer.rand(14, 18);
            }
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            if (!usedAPReset) {
                PlayerSkill increaseHP = SkillFactory.getSkill(Brawler.IMPROVE_MAX_HP);
                int sLvl = player.getSkillLevel(increaseHP);

                if (sLvl > 0)
                    MaxHP += increaseHP.getEffect(sLvl).getY();
            }

            if (usedAPReset) {
                MaxHP += 18;
            } else {
                MaxHP += Randomizer.rand(16, 20);
            }
        } else if (usedAPReset) {
            MaxHP += 8;
        } else {
            MaxHP += Randomizer.rand(8, 12);
        }

        return MaxHP;
    }

    private static int calcMpChange(MapleCharacter player, boolean usedAPReset) {
        MapleJob job = player.getJob();
        int MaxMP = 0;

        if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1) || job.isA(MapleJob.ARAN1)) {
            if (!usedAPReset) {
                MaxMP += (Randomizer.rand(2, 4) + (player.getInt() / 10));
            } else {
                MaxMP += 2;
            }
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            if (!usedAPReset) {
                PlayerSkill increaseMP = SkillFactory.getSkill(job.isA(MapleJob.BLAZEWIZARD1) ? BlazeWizard.INCREASING_MAX_MP : Magician.IMPROVED_MAX_MP_INCREASE);
                int sLvl = player.getSkillLevel(increaseMP);

                if (sLvl > 0)
                    MaxMP += increaseMP.getEffect(sLvl).getY();
            }

            if (!usedAPReset) {
                MaxMP += (Randomizer.rand(12, 16) + (player.getInt() / 20));
            } else {
                MaxMP += 18;
            }
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1)) {
            if (!usedAPReset) {
                MaxMP += (Randomizer.rand(6, 8) + (player.getInt() / 10));
            } else {
                MaxMP += 10;
            }
        } else if (job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
            if (!usedAPReset) {
                MaxMP += (Randomizer.rand(6, 8) + (player.getInt() / 10));
            } else {
                MaxMP += 10;
            }
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            if (!usedAPReset) {
                MaxMP += (Randomizer.rand(7, 9) + (player.getInt() / 10));
            } else {
                MaxMP += 14;
            }
        } else {
            if (!usedAPReset) {
                MaxMP += (Randomizer.rand(4, 6) + (player.getInt() / 10));
            } else {
                MaxMP += 6;
            }
        }

        return MaxMP;
    }

    private static int takeHp(MapleJob job) {
        int MaxHP = 0;

        if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1) || job.isA(MapleJob.ARAN1)) {
            MaxHP += 54;
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            MaxHP += 10;
        } else if (job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
            MaxHP += 20;
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1)) {
            MaxHP += 20;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            MaxHP += 42;
        } else {
            MaxHP += 12;
        }

        return MaxHP;
    }

    private static int takeMp(MapleJob job) {
        int MaxMP = 0;

        if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1) || job.isA(MapleJob.ARAN1)) {
            MaxMP += 4;
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            MaxMP += 31;
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1)) {
            MaxMP += 12;
        } else if (job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
            MaxMP += 12;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            MaxMP += 16;
        } else {
            MaxMP += 8;
        }

        return MaxMP;
    }

}
