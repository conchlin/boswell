/**
 * Feature list
 * This is called through a command (@features)
 * 
 * @author Saffron
 */

var status = -2;
var textOptions;
var nostalMobs = [[8142100, "+20%"], [8141300, "+20%"], // based on info in NostalgicMap.java 
                [8150100, "+20%"], [8150101, "+20%"],
                [7130020, "+20%"], [8140600, "+20%"],
                [3210200, "+15%"], [3210201, "+15%"],
                [3210202, "+15%"], [5120001, "+15%"],
                [5120002, "+15%"], [5120003, "+15%"],
                [4230106, "+15%"], [9500110, "+15%"],
                [5150001, "+15%"], [6230602, "+15%"],
                [6130208, "+15%"], [7130104, "+15%"],
                [5110301, "+12%"], [5110302, "+12%"],
                [8141000, "+10%"], [7160000, "+10%"],
                [7140000, "+10%"], [8141100, "+10%"],
                [8142000, "+10%"], [8143000, "+10%"],
                [7130010, "+10%"], [7130300, "+10%"],
                [8200000, "+10%"], [8200001, "+10%"],
                [8200002, "+10%"], [8200003, "+10%"],
                [8200004, "+10%"], [8200005, "+10%"],
                [8200006, "+10%"], [8200007, "+10%"],
                [8200008, "+10%"], [8200009, "+10%"],
                [8200010, "+10%"], [8200011, "+10%"],
                [8200012, "+10%"], [9420540, "-20%"],
                [9400639, "-20%"], [9400638, "-20%"],
                [9400640, "-50%"]];
var endGameWeps = /* warrior weps */[1302086, 1302175, 1302081, 1402047, 1402113, 1402113, 1402046, 1312038, 1312096, 1312037,
                1412034, /* add 2h axe von leon wep here,*/ 1412033, 1322061, 1322136, 1322060, 1422038, /* add 2h bw von leon wep here,*/
                1422037, 1432049, 1432101, 1432047, 1442067, 1442138, 1442063, 
                /* bowman weps */1452059, 1452131, 1452057, 1462051, 1462120, 1462050,
                /* thief weps */1472071, 1472143, 1472068, 1332076, 1332152, 1332074, 1332075, 1332073,
                /* mage weps */1372045, 1372102, 1372044, 1382059, 1382126, 1382057,
                /* pirate weps*/1482024, 1482104, 1482023, 1492025, 1492103, 1492023];
var features = new Array(
    ["Party Quests", "We offer a range of PQs here at Boswell. We've made the decision to make them open-ended " 
                    + "to help players gain access to each PQs exclusive item. The level ranges for these Pqs are: \r\n\r\n"
                    + "#eHenesysPQ:#n level 10 - 250\r\n"
                    + "#eKerningPQ:#n Level 21 - 250\r\n"
                    + "#eDojoPQ:#n level 25 - 250\r\n"
                    + "#eLudiPQ:#n level 35 - 250\r\n"
                    + "#eEllin ForestPQ:#n Level 44 - 250\r\n"
                    + "#eLudi MazePQ:#n Level 51 - 70"], 
    ["Godly Item System", "The #eGodly Item System#n is a potential buff that can be applied to equipment. This can be triggered for monster drops, "
                        + "Gachapon rewards, and equipment received from NPCs. The only major in-game system that is excluded the from the Godly " 
                        + "Item System is the Maker crafted items. The specifics of this systems are: \r\n\r\n" 
                        + "- There is a 10% chance for equipment to trigger the system\r\n" 
                        + "- The amount of the godly item buff is +5 to the affected stat\r\n"
                        + "- Each line is treated independently so one or multiple lines can be affected "],
    ["Party Play Changes", "Boswell has made some changes to improve the QoL for players training in a party. Those changes are as follows: \r\n\r\n" 
                        + "#e1)#n A Party Play bonus of +10% EXP per party member has been added. \r\n"
                        + "#e2)#n AFK party members trigger the #eleech tax#n where they receive a reduced EXP amount (-60%) and no longer are calculated in Party Play.\r\n" 
                        + "#e3)#n Party Play and Holy Symbol no longer stack. Therefore, parties of 6 would both get the maximum of +50% " 
                        + "bonus exp. Holy Symbol would still provide the advantage for parties of lesser size however the gap would be reduced."],
    ["Nostalgic Mobs", populateMobChanges()],
    ["Trophy System", "This custom system provides a static way to obtain items. \r\n\r\n Unlike the non-static ways of obtaining items, " 
                    + "rewards through this system will always have average stats. \r\n\r\n #eTo view more information about this feature please use the @trophy command!#n"],
    ["Automated Achievements", "Our automated achievement system rewards players with NX for various in-game accomplishments. Since Maplestory is " 
                            + "such a multi-faceted game there are many categories of achievements.\r\n\r\n" 
                            + "#eAll achievement lists can be viewed both from your player profile on the website or the @achievements command.#n"],
    ["HP World Tour", "Relying solely on HP washing for a character to be able to survive the game is an outdated mindset. Boswell provides " 
                    + "an alternative in the form of our #eHP World Tour#n. By defeating bosses you gain permanent HP! \r\n\r\n" 
                    + "#eFor a list of all bosses involved you can check your player profile on the website or use the @achievements command!#n"],
    ["Fishing", "TBD"],
    ["Revamped End-game", populateEndGame()],
    ["Custom Website", "Our website is completely unique to us and has been built from the ground up. It is a wonderful tool " 
                        + "to stay up-to-date on server ongoings. It also has many features and information that updates in " 
                        + "real-time, such as:\r\n\r\n" 
                        + "- Player online count\r\n" 
                        + "- Our droptable\r\n" 
                        + "- User Profiles\r\n" 
                        + "- Our Rankings\r\n"],
    ["Unique Cheater System", "Instead of outright banning cheaters such as many server do, we flag cheaters and restrict their interactions with " 
                        + "the rest of the community. \r\n\r\nOnce flagged, all gameplay done by cheaters is performed on the last channel (ch4) of the server. " 
                        + "This channel is deemed the #e'cheater channel'#n. Accounts that have been flagged by either a staff member or our anti-cheat system " 
                        + "can only play on that channel."]);

function start() {
    status = -2
    action(1, 0, 0);
}
                            
function action(mode, type, selection) {
                                
    if (mode == -1 || mode == 1 && selection == -1) {
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
            cm.sendPrev(features[selection][1]);      
        }
    }
}

function populateMobChanges() {
    var text = "This is our custom way of providing balance to the many mobs of Maplestory. The mobs"
                + " affected by this system can have their EXP boosted or reduced depending on their original value. " 
                + "\r\n\r\nThis system contains the following changes:\r\n";
               for(var i = 0; i < nostalMobs.length; i++) {
                    text += "#e#o" + nostalMobs[i][0] + "##n: "+ nostalMobs[i][1] + " EXP\r\n";
                }
    return text;
}

function populateEndGame() {
    var text = "#eNew Areas:#n Ulu City and Lion Heart Castle\r\n\r\n" 
                + "#eEndgame Weapon sets:#n Changes have been made it the weapon sets that Boswell now has to offer. The weapon sets " 
                + "that are available are the Reverse, Lion Heart, and Timeless sets. Timless has been boosted to be the new level 130 " 
                + "set.All three of these sets can only be crafted through the Maker skill. The specefic changes to these weapons are " 
                + "shown below.\r\n\r\n" + "#eWarrior Weapons:#n \r\n";
                for (var i =0; i < 23; i++) { // 25 when two missing warriors von leon weps are added
                    text += "#i" + endGameWeps[i] + "##z" + endGameWeps[i] + "##n\r\n";
                }

                text += "\r\n#n#eBowman Weapons:#n\r\n";
                for (i = 0; i < 29; i++) {
                    if (i > 22) text += "#i" + endGameWeps[i] + "##z" + endGameWeps[i] + "##n\r\n";
                }

                text += "\r\n#n#eThief Weapons:#n\r\n";
                for (i = 0; i < 37; i++) {
                    if (i > 28) text += "#i" + endGameWeps[i] + "##z" + endGameWeps[i] + "##n\r\n";
                }

                text += "\r\n#n#eMage Weapons:#n\r\n";
                for (i = 0; i < 43; i++) {
                    if (i > 36) text += "#i" + endGameWeps[i] + "##z" + endGameWeps[i] + "##n\r\n";
                }

                text += "\r\n#n#ePirate Weapons:#n\r\n";
                for (i = 0; i < 49; i++) {
                    if (i > 42) text += "#i" + endGameWeps[i] + "##z" + endGameWeps[i] + "##n\r\n";
                }

    return text;
}