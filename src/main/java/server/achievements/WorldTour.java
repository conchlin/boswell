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

package server.achievements;

import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import client.MapleCharacter;
import constants.life.AreaBoss;
import constants.life.Boss;
import constants.life.Monster;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import tools.MaplePacketCreator;

/**
 *
 * @author Saffron
 * @date 4/28/2019
 * @version 1.2
 */

public class WorldTour {
    
    /*
    960 + 1300 + 2000 + 1800 = 6060 hp
    5560 * 1.5 = 9090 modified for warrior/bucc gain
    */
    
    /*
        => Warriors / Buccaneers get the most HP			(1.5 multiplier)
	=> Thieves / Bowmen / Gunslingers get "normal" extra HP		(1.0 multiplier)
	=> Mages get nothing
   
    
   /*
    - Tier 1: weak Area Bosses (total: 24 bosses * 40 hp)	// Bosses that probably won't kill you

	Mano, Stumpy, Faust, Dyle, Mushmom, King Clang, Zombie Mushmom, Snack Bar, Jr. Balrog, Eliza, Snwoman, Timer,
	Zeno, Nine-tailed Fox, Yellow King Goblin, Blue King Goblin, Green King Goblin, Seruf, Tae Roon, King Sage Cat,
        Giant Centipede, Deo, Kimera, Blue Mushmom
    */
    
    /*
    - Tier 2: strong Area Bosses (total: 10 bosses * 130 hp)	// Bosses that have a chance of killing you
	
	Crimson Balrog, Capt. Latanica, Headless Horseman, Manon, Griffey, Leviathan, Dodo, Lilynouch, Lyka, male boss,
        Kacchu Mushu
    */
    
    /*
    Tier 3: Area bosses that are not tier 2 nor tier 4 (total: 8 bosses * 250 hp)
    
    	Pap, Bigfoot, Black Crow, Pianus (right), Pianus (left), Anego, scarlion, targa
    */
    
    /*
    Tier 4: Krexel, Zakum, horntail (3 * 600)
    */

    public enum AchievementType {
        MONSTERKILL, LEVELUP, PQ, FAME, FAMEGAIN, FAMELOSS, ITEM, DAMAGE
    }

    private static HashMap<Integer, Integer> worldtour_items = new HashMap<>() {{
        put(250, 1000); // chaos scroll
        put(251, 1000); // white scroll
    }};

    private static HashMap<Integer, Integer> worldtour_fame = new HashMap<>() {{
        put(50, 2000);
        put(100, 3000);
        put(150, 4000);
        put(200, 5000);
        put(-50, 3000);
        put(-100, 4000);
        put(-150, 5000);
        put(-200, 6000);
    }};

    private static HashMap<Integer, Integer> worldtour_levels = new HashMap<>() {{
        put(10, 500);
        put(30, 1000);
        put(50, 1500);
        put(70, 2000);
        put(100, 3000);
        put(120, 3500);
        put(130, 4000);
        put(140, 4500);
        put(150, 5000);
        put(160, 5500);
        put(170, 6000);
        put(180, 6500);
        put(190, 7000);
        put(195, 7500);
        put(200, 10000);
    }};

    private static SortedMap<Integer, Integer> worldtour_damage = new TreeMap<>() {{
        put(1000, 500);
        put(2500, 1000);
        put(5000, 1500);
        put(10000, 2000);
        put(20000, 2500);
        put(30000, 3000);
        put(40000, 3500);
        put(50000, 4000);
        put(60000, 4500);
        put(70000, 5000);
        put(80000, 5500);
        put(90000, 6000);
        put(100000, 6500);
    }};

    private static HashMap<Integer, int[]> worldtour_monsters = new HashMap<>() {{
        put(Monster.SNAIL, new int[]{1, 1});
        put(AreaBoss.MANO, new int[]{40, 0});
        put(AreaBoss.STUMPY, new int[]{40, 0});
        put(AreaBoss.FAUST, new int[]{40, 0});
        put(AreaBoss.MUSHMOM, new int[]{40, 0});
        put(AreaBoss.KINGCLANG, new int[]{40, 0});
        put(AreaBoss.ZMUSHMOM, new int[]{40, 0});
        put(AreaBoss.SNACKBAR, new int[]{40, 0});
        put(AreaBoss.JRBALROG, new int[]{40, 0});
        put(AreaBoss.ELIZA, new int[]{40, 0});
        put(AreaBoss.SNOWMAN, new int[]{40, 0});
        put(AreaBoss.TIMER, new int[]{40, 0});
        put(AreaBoss.ZENO, new int[]{40, 0});
        put(AreaBoss.OLDFOX, new int[]{40, 0});
        put(AreaBoss.YELLOWGOBLIN, new int[]{40, 0});
        put(AreaBoss.BLUEGOBLIN, new int[]{40, 0});
        put(AreaBoss.GREENGOBLIN, new int[]{40, 0});
        put(AreaBoss.SERUF, new int[]{40, 0});
        put(AreaBoss.TAEROON, new int[]{40, 0});
        put(AreaBoss.KINGSAGECAT, new int[]{40, 0});
        put(AreaBoss.CENTIPEDE, new int[]{40, 0});
        put(AreaBoss.DEO, new int[]{40, 0});
        put(AreaBoss.CHIMERA, new int[]{40, 0});
        put(AreaBoss.BLUEMUSHMOM, new int[]{40, 0});
        // tier 2
        put(AreaBoss.CRIMSONBALROG, new int[]{130, 0});
        put(AreaBoss.CAPTLATANICA, new int[]{130, 0});
        put(Boss.HH, new int[]{130, 0});
        put(Boss.MANON, new int[]{130, 0});
        put(Boss.GRIFFEY, new int[]{130, 0});
        put(AreaBoss.LEVIATHAN, new int[]{130, 0});
        put(AreaBoss.DODO, new int[]{130, 0});
        put(AreaBoss.LILYNOUCH, new int[]{130, 0});
        put(AreaBoss.LYKA, new int[]{130, 0});
        put(Boss.MALEBOSS, new int[]{130, 0});
        put(AreaBoss.KACCHUUMUSHA, new int[]{130, 0});
        put(Boss.DUNAS, new int[]{130, 0});
        put(Boss.AUFHEBEN, new int[]{130, 0});
        put(Boss.OBERON, new int[]{130, 0});
        put(Boss.NIBELUNG3, new int[]{130, 0});
        put(Boss.BERGAMOT3, new int[]{130, 0});
        // tier 3
        put(Boss.BIGFOOT, new int[]{250, 0});
        put(Boss.BLACKCROW, new int[]{250, 0});
        put(Boss.LEFTPIANUS, new int[]{250, 0});
        put(Boss.RIGHTPIANUS, new int[]{250, 0});
        put(Boss.ANEGO, new int[]{250, 0});
        put(Boss.PAPULATUS, new int[]{250, 0});
        put(Boss.SCARLION, new int[]{250, 0});
        put(Boss.TARGA, new int[]{250, 0});
        put(Boss.BODYGUARD_A, new int[]{250, 0});
        put(Boss.BODYGUARD_B, new int[]{250, 0});
        // tier 4
        put(Boss.CASTELLANTOAD, new int[]{600, 0});
        put(Boss.THE_BOSS, new int[]{600, 0});
        put(Boss.KREXEL, new int[]{600, 0});
        put(Boss.ZAKUM3, new int[]{600, 0});
        put(Boss.HT_LHAND, new int[]{600, 0});
    }};

    public static String getWorldTourId(AchievementType achievementType, int target) {
        if (achievementType == AchievementType.FAMEGAIN || achievementType == AchievementType.FAMELOSS)
            return achievementType.name();
        return achievementType.name() + "_" + target;
    }

    public static void finishWorldTour(MapleCharacter player, AchievementType achievement_type, int target) {
        int hp_reward = 0;
        int nx_reward = 0;
        String achievement_action = "";

        switch (achievement_type) {
            case MONSTERKILL:
                //System.out.println(target);
                if (!worldtour_monsters.containsKey(target)) return;
                if (player.worldTourFinished(getWorldTourId(achievement_type, target))) return;
                player.setWorldTourFinished(getWorldTourId(achievement_type, target));

                MapleMonster monster = MapleLifeFactory.getMonster(target);
                achievement_action = "killing " + (monster != null ? monster.getName() : "monsterid " + target);
                hp_reward = worldtour_monsters.get(target)[0];
                nx_reward = worldtour_monsters.get(target)[1];
                break;
            case PQ:
                // TODO: PQ
                break;
            case FAME:
                if (!worldtour_fame.containsKey(target)) {
                    for (int fame : worldtour_fame.keySet()) {
                        if (player.worldTourFinished((getWorldTourId(achievement_type, fame)))) continue;
                        if ((fame < 0 && target < fame) || (fame > 0 && target > fame)) {
                            finishWorldTour(player, achievement_type, fame);
                            return;
                        }
                    }
                    return;
                }

                if (player.worldTourFinished(getWorldTourId(achievement_type, target))) return;
                player.setWorldTourFinished(getWorldTourId(achievement_type, target));
                achievement_action = "reaching " + target + " fame";
                nx_reward = worldtour_fame.get(target);
                break;
            case FAMEGAIN:
                if (player.worldTourFinished(getWorldTourId(achievement_type, target))) return;
                player.setWorldTourFinished(getWorldTourId(achievement_type, target));
                achievement_action = "gaining fame for the first time";
                nx_reward = 500;
                break;
            case FAMELOSS:
                if (player.worldTourFinished(getWorldTourId(achievement_type, target))) return;
                player.setWorldTourFinished(getWorldTourId(achievement_type, target));
                achievement_action = "losing fame for the first time";
                nx_reward = 500;
                break;
            case ITEM:
                // TODO: items
                break;
            case LEVELUP:
                if (!worldtour_levels.containsKey(target)) return;
                if (player.worldTourFinished(getWorldTourId(achievement_type, target))) return;
                player.setWorldTourFinished(getWorldTourId(achievement_type, target));
                achievement_action = "reaching level " + target;
                nx_reward = worldtour_levels.get(target);
                break;
            case DAMAGE:
                if (!worldtour_damage.containsKey(target)) {
                    for (int damage : worldtour_damage.keySet()) {
                        if (target < damage) return;
                        if (player.worldTourFinished((getWorldTourId(achievement_type, damage)))) continue;
                        finishWorldTour(player, achievement_type, damage);
                        return;
                    }
                    return;
                }

                if (player.worldTourFinished(getWorldTourId(achievement_type, target))) return;
                player.setWorldTourFinished(getWorldTourId(achievement_type, target));
                achievement_action = "dealing " + target + " damage";
                nx_reward = worldtour_damage.get(target);
                break;
            default:
                return;
        }

        String achievement_name = "";

        if (hp_reward > 0) {
            // apply that 50% increase to hp for warriors, buccs and beginners
            if (!player.isMagician()) { // aint no mage gettin shit
                player.gainMaxHp(player.isWarriorMod() ? (int) (hp_reward * 1.5) : hp_reward);
            }
        }

        if (nx_reward > 0) {
            player.getCashShop().gainCash(1, nx_reward);
        }

        if (hp_reward > 0 && nx_reward > 0) {
            achievement_name = "[World Tour] You have gained " + hp_reward + " HP and " + nx_reward + " NX for ";
        } else if (hp_reward > 0) {
            achievement_name = "[World Tour] You have gained " + hp_reward + " HP for ";
        } else if (nx_reward > 0) {
            achievement_name = "[World Tour] You have gained " + nx_reward + " NX for ";
        }

        if (achievement_name.length() > 0) {
            //player.getClient().getSession().write(MaplePacketCreator.earnTitleMessage(achievement_name + achievement_action));
            player.showHint(achievement_name + achievement_action);
            player.getClient().announce(MaplePacketCreator.serverNotice(5, achievement_name + achievement_action));
        }
        player.saveCharToDB(); // let's make sure entries always register in the db
    }
}
