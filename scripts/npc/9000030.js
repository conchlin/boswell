var status = -2;
/*var partyPlayMaps = new Array(104040000, 104040001, 104040002, 104030000, 104020000, 104010000, 104010001, 104010002, 104000300, 
    104000200, 104000100, 103030000, 103030100, 103030200, 103020200, 103020100, 103020000, 103010000, 103000101,
    103000102, 103000103, 100030000, 100010000, 103000104, 103000105, 103000200, 103000201, 103000202, 102050000,
    102040000, 102030000, 102020000, 102020100, 102020200, 102020300, 102010000, 101040000, 101030400, 101030300,
    101030200, 101030100, 101030000, 101030401, 101030402, 101030403, 101030405, 101030406, 101020000, 101010000,
    100040000, 100050000, 100040100, 105030000, 105040000, 105040100, 105040200, 200010000, 200010100, 200010110,
    200010111, 200010200, 200010120, 200010121, 200010130, 200010131, 200020000, 200030000, 200040000, 200040001,
    200050000, 200060000, 200070000, 200080000, 230040200, 220060000, 220060100, 220060200, 220060300, 220070000,
    220070100, 220070200, 220070300, 220070201, 101030110, 101030111, 101030112, 101030105, 101030106, 101030107,
    101030108, 101030109, 211041100, 211041200, 211041300, 211041400, 211041500, 211041600, 211041700, 211041800,
    250020200, 250020300, 251010400, 251010500, 540020100);
var boostedMobs = new Array(); */
var features = new Array(
    ["Party Quests", "We offer a range of PQs here at Boswell. We've made the decision to make some of these quests open-ended " 
                    + "to help players have access to each PQs exclusive item. The level ranges for these Pqs are: \r\n\r\n"
                    + "HenesysPQ: level 10 - 200\r\n"
                    + "KerningPQ: Level 21 - 200\r\n"
                    + "DojoPQ: level 25 - 200\r\n"
                    + "LudiPQ: level 35 - 200\r\n"
                    + "PratePQ: level 51 - 70 \r\n"
                    + "Ludi MazePQ: Level 51 - 70\r\n" 
                    + "Ellin ForestPQ: Level 44 - 200"], 
    ["Godly Item System", "The godly item system is a potential buff that can happen to equipment. This applies to monster drops, gachapon rewards, " 
                        + "and equipment recieved from NPCs. A major weapon aspect that doesnt use the Godly Item system is the Maker scrafted " 
                        + "equipment. The specifics of this systems are: \r\n\r\n" 
                        + "- A 15% chance for equipment to trigger the system\r\n" 
                        + "- The amount of the godly item buff is +5 to the affected stat\r\n"
                        + "- Each line is treated independently so one or multiple lines can be affected"],
    ["Party Play Maps", "Training can be more fun with your friends. Therefore, we've added an assortment of maps that reward you for bringing your " 
                        + "friends along. These maps range from a +10% to +25% exp boosted based of your party size. The full list of maps in this " 
                        + "system are: \r\n\r\n" 
                        + "To view the full list please use @features <partyplay> (Coming soon)"],
    ["Nostalgia Boosted Mobs","As Maplestory has evolved certain areas and mobs get left behind. These areas are no longer attractive to players " 
                                + "because much better alteratives exist. Nostalgia boosted mobs is our way of bringing these areas back to being " 
                                + "relevant by boosting their exp. The mobs that are affected by this buff include: \r\n\r\n"
                                + "To view the full list please use @features <boostedmobs> (Coming soon)"],
    ["Automated Achievements", "Our automated achievmeent system rewards players with NX for various in-game accomplishments. Since Maplestory is such " 
                                + "a multi-faceted game there are many categories of achievements. These are based on aspects like level, fame, " 
                                + "gachapon, PQ, damage, etc. All achievements available for your specific character can be viewed on our website!"],
    ["HP World Tour", "Relying solely on HP washing for a character to be able to boss is an outdated mindset. Boswell provides an " 
                    + "alternative which is our HP World Tour. All bosses are broken down into categories that when defeated give you " 
                    + "permanent HP gain! This amount rewarded is based on whether you are a melee or ranged class.\r\rn\r\n" 
                    + "- Melee classes are rewarded 7,140 HP through the system\r\n" 
                    + "- Ranged classes are rewarded 4,760 HP through the system\r\n\r\n" 
                    + "For a list of all bosses involved make sure to check out your account info on the website!"],
    ["Voter Appreciation Ring", "We want to give back to those that support us! By voting at least 20 time in a month you are able to " 
                                + "receive a Boswell Voter Appreciation Ring. When obtained you will be able to use the item for " 
                                + "the whole month after which it will expire. While equipped the player will experience a +1.5 boost " 
                                + "to the exp rate."],
    ["Cygnus Bonus", "We have changed how Blessing of Fairy works in Boswell. We have added a passive Cygnus Bonus in addition " 
                        + "to a diminished Blessing of Fairy. The Cygnus Bonus is obtained when a cygnus character reaches level " 
                        + "120 and affects all characters on the account. The new numbers for these are as follows: \r\n\r\n" 
                        + "- Blessing of Fairy rewards +5wa/+10ma at level 20\r\n" 
                        + "- The Cygnus Bonus rewards 15wa/30ma to all characters on account\r\n\r\n" 
                        + "We feel this change brings back the importance of Knights of Cygnus characters and provide nice " 
                        + "supplemental damage to the adventurer classes."],
    ["Custom Website", "Our website is completely unique to us and has been built from the ground up. It is a wonderful tool " 
                        + "to stay up-to-date on server ongoings. It also has many features and information that updates in " 
                        + "real-time, such as:\r\n\r\n" 
                        + "- Player online count\r\n" 
                        + "- Our droptable\r\n" + "- User Profile\r\n" 
                        + "- Our Rankings\r\n"],
    ["Unique Cheater System", "Instead of outright banning cheaters such as many server we do, we flag cheaters and restrict their " 
                            + "interactions with the rest of the community. Once flagged, all gameplay done by cheaters is " 
                            + "performedon the last channel of the server. This channel is deemed the 'cheater channel'. " 
                            + "All players can access this channel but only non-cheaters can leave. Cheaters can only interact " 
                            + "with players outside of the 'cheater channel' through the usage of 'safe-haven' maps. The list of " 
                            + "safe-haven maps are:\r\n\r\n"
                            + "Free Market Entrance\r\n\r\n" 
                            + "In addition, their in-game actions such as partying, any item exchange, or any mechanic that would" 
                            + "benefit a non-cheater has been disabled. They can however still perform these with fellow cheaters."]);

function start() {
    status = -2
    action(1, 0, 0);
}
                            
function action(mode, type, selection) {
                                
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
                                        
        if(status == -1) {
            var text = "I'm here to explain all of the various features that Boswell has to offer. What feature would you like to know more about?\r\n";
                for(var i = 0; i < features.length; i++) {
                    text += "#L" + i + "##b" + features[i][0] + "#k#l\r\n";
                }

            cm.sendSimple(text);
        }
        else if (status == 0) {
            cm.sendOk(features[selection][1]);
            cm.dispose();       
        }
    }
}