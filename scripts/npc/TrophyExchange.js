/**
 * Trophy Exchange System
 * Accessed through Nina in the FM
 * 
 * @author Saffron
 */

var  options = ["How do I get trophies?",
                 "I'd like to view the Bronze Trophy rewards",
                  "I'd like to view the Silver Trophy rewards",
                   "I'd like to view the Gold Trophy rewards"];
// reward lists follow the syntax of [item string, item id, trophy cost]
var bronzeRewards = [["White Maple Bandana", 1002603, 10],
                                ["Yellow Maple Bandana", 1002601, 30],
                                ["Red Maple Bandana", 1002600, 50],
                                ["Blue Maple Bandana", 1002602, 100],
                                ["Maple Cape", 1102166, 20],
                                ["Maple Cape", 1102167, 40],
                                ["Maple Cape", 1102168, 50],
                                ["Maple Earring", 1032040, 20],
                                ["Random 10\% Scroll", 2044500, 100],
                                ["Random 60\% Scroll", 2044501, 110],
                                ["Silver Trophy", 4008001, 100]];
var silverRewards = [["Smiling Mask", 1012108, 3],
                                ["Crying Mask", 1012109, 3],
                                ["Angry Mask", 1012110, 3],
                                ["Sad Mask", 1012111, 3],
                                ["Raccoon Mask", 1022058, 4],
                                ["Bath Towel (Black)", 1050127, 4],
                                ["Bath Towel (Yellow)", 1051140, 4],
                                ["White Raccoon Mask", 1022060, 5],
                                ["Gold Emerald Earrings", 1032026, 5],
                                ["Yellow Snowshoes", 1072239, 5],
                                ["Rotten Apple", 2022998, 8],
                                ["Unripe Onyx Apple", 2012008, 8],
                                ["Gold Trophies", 4008000, 10]];
var goldRewards = [["Onyx Apple", 2022179, 1],
                                ["Radioactive Apple", 2022999, 1],
                                ["Granny Smith Apple", 2022997, 2],
                                ["Earth Apple", 2022996, 2],
                                ["Pink Adventurer Cape", 1102041, 2],
                                ["Purple Adventurer Cape", 1102042, 2],
                                ["Chaos Scroll", 2049100, 2],
                                ["White Scroll", 2340000, 3],
                                ["Pink Gaia Cape", 1102084, 3],
                                ["Purple Gaia Cape", 1102086, 3],
                                ["Silver Deputy Star", 1122014, 5],
                                ["Flamekeepers Cordon", 1082246, 6],
                                ["Stormcaster Gloves", 1082223, 6],
                                ["Strawberry Ice Cream Bar", 1012070, 6],
                                ["Boswell All-Star (Medal)", 1142355, 50]];

var bronze = 4008002;
var silver = 4008001;
var gold = 4008000;
var tier = 0;

function start() {
    status = -2
    action(1, 0, 0);
}
      
// TODO add in the wz edited items to the list

function action(mode, type, selection) {
                                
    if (mode === -1) {
        cm.dispose();
    } else {
        if (mode === 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode === 1)
            status++;
        else
            status--;
                                        
        if(status === -1) {
            var text = "I'm here to explain the trophy system that Boswell has to offer. What aspect would you like to know more about?\r\n";
                for(var i = 0; i < options.length; i++) {
                    text += "#L" + i + "##b" + options[i] + "#k#l\r\n";
                }

            cm.sendSimple(text);
        } else if (status === 0) {

            if (selection === 0) {
                cm.sendOk("Trophies are the building blocks of Boswell.\r\n #i" + bronze + "# #i" + silver + "# #i" + gold 
                + "# They provide a static way to gain many different kinds of useful items. This all starts out with the " 
                + "Bronze trophy and then scales up to the Gold Trophy. The Bronze Trophy is a global drop and can also be " 
                + "obtained through monthly events and GM events! \r\n\r\n To further understand the potential of this system please " 
                + "browse each trophies rewards list.");
                cm.dispose();
            } else if (selection === 1) { // bronze rewards
                var text = "These are the possible rewards you can gain from Bronze Trophies. Which of these would you like?\r\n";
                for(var i = 0; i < bronzeRewards.length; i++) {
                    text += "#L" + i + "##i" + bronzeRewards[i][1] + "#" + "#b#z" + bronzeRewards[i][1] + "# for " + bronzeRewards[i][2] + " Bronze Trophies#l\r\n";
                }

                cm.sendSimple(text);
            } else if (selection === 2) { // silver rewards
                var text = "These are the possible rewards you can gain from Silver Trophies. Which of these would you like?\r\n";
                for(var i = 0; i < silverRewards.length; i++) {
                    text += "#L" + i + "##i" + silverRewards[i][1] + "#" + "#b#z" + silverRewards[i][1] + "# for " + silverRewards[i][2] + " Silver Trophies#l\r\n";
                }

                tier = 1;
                cm.sendSimple(text);
            } else if (selection === 3) { // gold rewards
                var text = "These are the possible rewards you can gain from Gold Trophies. Which of these would you like?\r\n";
                for(var i = 0; i < goldRewards.length; i++) {
                    text += "#L" + i + "##i" + goldRewards[i][1] + "#" + "#b#z" + goldRewards[i][1] + "# for " + goldRewards[i][2] + " Gold Trophies#l\r\n";
                }

                tier = 2;
                cm.sendSimple(text);
            } 
        } else if (status === 1) {
            if (tier === 1) { // silver
                if (cm.haveItem(silver, silverRewards[selection][2])) {
                    if (cm.canHold(silverRewards[selection][1], 1)) {
                        cm.gainItem(silverRewards[selection][1], 1);
                        cm.gainItem(silver, -silverRewards[selection][2]);
                    } else {
                        cm.sendOk("Please clear up some space in your EQP and USE slots.");
                    }
                } else {
                    cm.sendOk("It seems you do not have the required silver trophy amount.");
                }
                cm.dispose();
            } else if (tier === 2) { // gold
                if (cm.haveItem(gold, goldRewards[selection][2])) {
                    if (cm.canHold(goldRewards[selection][1], 1)) {
                        cm.gainItem(goldRewards[selection][1], 1);
                        cm.gainItem(gold, -goldRewards[selection][2]);
                    } else {
                        cm.sendOk("Please clear up some space in your EQP and USE slots.");
                    }
                } else {
                    cm.sendOk("It seems you do not have the required gold trophy amount.");
                }
                cm.dispose();
            } else { // bronze
                if (cm.haveItem(bronze, bronzeRewards[selection][2])) {
                    if (cm.canHold(bronzeRewards[selection][1], 1)) {
                        cm.gainItem(bronzeRewards[selection][1], 1);
                        cm.gainItem(bronze, -bronzeRewards[selection][2]);
                    } else {
                        cm.sendOk("Please clear up some space in your EQP and USE slots.");
                    }
                } else {
                    cm.sendOk("It seems you do not have the required bronze trophy amount.");
                }
                cm.dispose();
            }
        }
    }
}