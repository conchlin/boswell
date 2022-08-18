/* Madrick
@Author Saffron
@Map - Fishing Lagoon
@Function - Fishing Handler
*/

var status = -1;
// array is formatted as [item string, item id, golden fish cost]
var goldenRewards = [["Gachapon Ticket", 5220000, 10],
                                ["Chocolate Ice Cream Bar", 1012071, 25],
                                ["Melon Ice Cream Bar", 1012072, 25],
                                ["Watermelon Ice Cream Bar", 1012073, 25],
                                ["Strawberry Ice Cream Bar", 1012070, 50],
                                ["Shark-sicle", 1702366, 100],
                                ["Watermelon Slice", 1702775, 100],
                                ["Shark (Pet)", 5000141, 100],
                                ["Fisherman's Patience", 1142356, 250]];
var fishType = [["Carp", 4031630], ["Whitebait", 4031627], ["Sailfish", 4031628], ["Salmon", 4031631]];
var carpRewards = [["Teleport Rock", 5040000, 3],
                                ["VIP Teleport Rock", 5041000, 6],
                                ["Safety Charm", 5130000, 8],
                                ["Bronze Trophy", 4008002, 10]];
var whitebaitRewards = [["Teleport Rock", 5040000, 3],
                                ["VIP Teleport Rock", 5041000, 6],
                                ["Safety Charm", 5130000, 8],
                                ["Bronze Trophy", 4008002, 10]];
var sailfishRewards = [["Teleport Rock", 5040000, 3],
                                ["VIP Teleport Rock", 5041000, 6],
                                ["Safety Charm", 5130000, 8],
                                ["Bronze Trophy", 4008002, 10]];
var salmonRewards = [["Teleport Rock", 5040000, 3],
                                ["VIP Teleport Rock", 5041000, 6],
                                ["Safety Charm", 5130000, 8],
                                ["Bronze Trophy", 4008002, 10]];
var goldenFish = 4031626;
var tier = 0, fish = 0;
var carp30 = 4031630, carp53 = 4031637, carp60 = 4031638, carp110 = 4031639, carp113 = 4031640;
var wbait3 = 4031627, wbait36 = 4031633, wbait5 = 4031634, wbait65 = 4031635, wbait10 = 4031636;
var sail120 = 4031628, sail128 = 4031641, sail131 = 4031642, sail140 = 4031643, sail148 = 4031644;
var salmon150 = 4031631, salmon166 = 4031645, salmon183 = 4031646, salmon227 = 4031647, salmon228 = 4031648;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (mode === -1) {
        cm.dispose();
    } else {
        if (mode === 0) {
            cm.dispose();
            return;
        }

        if (mode === 1) status++;
        else status--;

        if (status === 0) {
            cm.sendSimple("What do you want to do?\n\r #b#L1#Buy fishing baits#l \n\r #L2#Buy fishing chair (50,000 Meso)#l \n\r"
                + " #L3#Guide on fishing#l \n\r #L4#Trade in Golden Fish\n\r #b#L5#Exchange Fish for Scrolls");

        } else if (status === 1) {
            if (selection === 1) {
                cm.sendYesNo("It requires 5,000 meso for 120 bait. Do you want to purchase?");
            } else if (selection === 2) {
                if (cm.haveItem(3011000)) {
                    cm.sendNext("You already have a fishing chair. Each character can only have 1 fishing chair.");
                    } else {
                        if (cm.canHold(3011000) && cm.getMeso() >= 50000) {
                            cm.gainItem(3011000, 1);
                            cm.gainMeso(-50000);
                            cm.sendNext("Happy Fishing~");
                        } else {
                            cm.sendOk("Please check if you have the required amount of meso or sufficient inventory slot.");
                        }
                    }
                cm.dispose();
            } else if (selection === 3) {
                cm.sendOk("Fishing is quite simle but not for the feint of heart. It requires much patience and a bit of luck!"
                            + "There are many fish in this lagoon. If you bring them back to me I can reward you with various prizes.\r\n"
                            + "#i4031630# #i4031631# #i4031633# #i4031641# \r\n"
                            + "The best of these fishes is the elusive Golden Fish!\r\n"
                            + "#i4031626# \r\n"
                            + "In order to successfully fish you need both the fishing char and bait. Without these items you "
                            + "will not be able to catch anything.\r\n\r\n My advice for you would be to relax, sit back, and reel it in.");
                cm.dispose();
            } else if (selection === 4) {
                var text = "It takes much patience and experience to reel in Golden Fish. "
                            + "But it seems like you are no ordinary fisher. Would you like to exchange those fish??\r\n";
                for(var i = 0; i < goldenRewards.length; i++) {
                    text += "#L" + i + "##i" + goldenRewards[i][1] + "#" + "#b#z" + goldenRewards[i][1] + "# for " + goldenRewards[i][2] + " Golden Fish#l\r\n";
                }

                cm.sendSimple(text);
                tier = 1;
            } else if (selection === 5) {
                var text = "Seems like you have a lot of different fish. Which of these would you like to exchange?\r\n";
                for(var i = 0; i < fishType.length; i++) {
                    text += "#L" + i + "##i" + fishType[i][1] + "#" + "#b Exchange " + fishType[i][0] + "#l\r\n";
                }

                cm.sendSimple(text);
                tier = 2;
            }
        } else if (status === 2) {
            if (tier === 0 && selection < 0) {
                if (cm.canHold(2300000) && cm.getMeso() >= 3000) {
                    if (!cm.haveItem(2300000)) {
                        cm.gainItem(2300000, 120);
                        cm.gainMeso(-5000);
                        cm.sendNext("Happy Fishing~");
                    } else {
                        cm.sendNext("You already have fishing bait.");
                    }
                } else {
                    cm.sendOk("Please check if you have the required meso or free space in the respective inventory slot.");
                }
                cm.dispose();
            } else if (tier === 1) {
                if (cm.haveItem(goldenFish, goldenRewards[selection][2])) {
                    if (cm.canHold(goldenRewards[selection][1], 1)) {
                        cm.gainItem(goldenRewards[selection][1], 1);
                        cm.gainItem(goldenFish, -goldenRewards[selection][2]);
                    } else {
                        cm.sendOk("Please free up some room in the respective inventory slot.");
                    }
                } else {
                    cm.sendOk("It seems you do not have the required golden fish amount.");
                }
                cm.dispose();
            } else if (tier === 2) {
                switch (selection) {
                    case 0: // carp
                    var text = "It takes much patience and experience to reel in Carp. "
                                +  "There are 5 total sizes of Carp out there. \r\n\r\n"
                                + "#i4031630# #i4031637# #i4031638# #i4031639# #i4031640# \r\n\r\n"
                                + " If you can bring me full sets of them I will reward you!"
                                + " Would you like to exchange your fish??\r\n";
                    for(var i = 0; i < carpRewards.length; i++) {
                        text += "#L" + i + "##i" + carpRewards[i][1] + "#" + "#b#z" + carpRewards[i][1]
                                + "# for " + carpRewards[i][2] + " sets of Carp#l\r\n";
                    }

                    cm.sendSimple(text);
                    break;
                case 1: // whitebait
                    var text = "It takes much patience and experience to reel in Whitebait. "
                                +  "There are 5 total sizes of Whitebait out there. \r\n\r\n"
                                + "#i4031630# #i4031637# #i4031638# #i4031639# #i4031640# \r\n\r\n"
                                + " If you can bring me full sets of them I will reward you!"
                                + " Would you like to exchange your fish??\r\n";
                    for(var i = 0; i < whitebaitRewards.length; i++) {
                        text += "#L" + i + "##i" + whitebaitRewards[i][1] + "#" + "#b#z" + whitebaitRewards[i][1]
                                + "# for " + whitebaitRewards[i][2] + " sets of Carp#l\r\n";
                    }

                    cm.sendSimple(text);
                    fish = 1;
                    break;
                case 2: // sailfish
                    var text = "It takes much patience and experience to reel in Sailfish. "
                            +  "There are 5 total sizes of Sailfish out there. \r\n\r\n"
                            + "#i4031630# #i4031637# #i4031638# #i4031639# #i4031640# \r\n\r\n"
                            + " If you can bring me full sets of them I will reward you!"
                            + " Would you like to exchange your fish??\r\n";
                    for(var i = 0; i < sailfishRewards.length; i++) {
                        text += "#L" + i + "##i" + sailfishRewards[i][1] + "#" + "#b#z" + sailfishRewards[i][1]
                                + "# for " + sailfishRewards[i][2] + " sets of Carp#l\r\n";
                        }

                    cm.sendSimple(text);
                    fish = 2;
                    break;
                case 3: // salmon
                    var text = "It takes much patience and experience to reel in Salmon. "
                        +  "There are 5 total sizes of Salmon out there. \r\n\r\n"
                        + "#i4031630# #i4031637# #i4031638# #i4031639# #i4031640# \r\n\r\n"
                        + " If you can bring me full sets of them I will reward you!"
                        + " Would you like to exchange your fish??\r\n";
                    for(var i = 0; i < salmonRewards.length; i++) {
                        text += "#L" + i + "##i" + salmonRewards[i][1] + "#" + "#b#z" + salmonRewards[i][1]
                                + "# for " + salmonRewards[i][2] + " sets of Carp#l\r\n";
                    }

                    cm.sendSimple(text);
                    fish = 3;
                    break;
                }
            tier = 3;
        }
    } else if (status === 3) {
        switch (fish) {
            case 0:
                if (cm.haveItem(carp30, carpRewards[selection][2])
                    && cm.haveItem(carp53, carpRewards[selection][2])
                    && cm.haveItem(carp60, carpRewards[selection][2])
                    && cm.haveItem(carp110, carpRewards[selection][2])
                    && cm.haveItem(carp113, carpRewards[selection][2])) {
                    if (cm.canHold(carpRewards[selection][1], 1)) {
                        cm.gainItem(carpRewards[selection][1], 1);
                        cm.gainItem(carp30, -carpRewards[selection][2]);
                        cm.gainItem(carp53, -carpRewards[selection][2]);
                        cm.gainItem(carp60, -carpRewards[selection][2]);
                        cm.gainItem(carp110, -carpRewards[selection][2]);
                        cm.gainItem(carp113, -carpRewards[selection][2]);
                    } else {
                        cm.sendOk("Please free up some room in the respective inventory slot.");
                    }
                } else {
                    cm.sendOk("It seems you do not have the required Carp amount.");
                }
            break;
            case 1:
                if (cm.haveItem(wbait3, whitebaitRewards[selection][2])
                    && cm.haveItem(wbait36, whitebaitRewards[selection][2])
                    && cm.haveItem(wbait5, whitebaitRewards[selection][2])
                    && cm.haveItem(wbait65, whitebaitRewards[selection][2])
                    && cm.haveItem(wbait10, whitebaitRewards[selection][2])) {
                    if (cm.canHold(whitebaitRewards[selection][1], 1)) {
                        cm.gainItem(whitebaitRewards[selection][1], 1);
                        cm.gainItem(wbait3, -whitebaitRewards[selection][2]);
                        cm.gainItem(wbait36, -whitebaitRewards[selection][2]);
                        cm.gainItem(wbait5, -whitebaitRewards[selection][2]);
                        cm.gainItem(wbait65, -whitebaitRewards[selection][2]);
                        cm.gainItem(wbait10, -whitebaitRewards[selection][2]);
                    } else {
                        cm.sendOk("Please free up some room in the respective inventory slot.");
                    }
                } else {
                    cm.sendOk("It seems you do not have the required Whitebait amount.");
                }
            break;
            case 2:
                if (cm.haveItem(sail120, sailfishRewards[selection][2])
                    && cm.haveItem(sail128, sailfishRewards[selection][2])
                    && cm.haveItem(sail131, sailfishRewards[selection][2])
                    && cm.haveItem(sail140, sailfishRewards[selection][2])
                    && cm.haveItem(sail148, sailfishRewards[selection][2])) {
                    if (cm.canHold(sailfishRewards[selection][1], 1)) {
                        cm.gainItem(sailfishRewards[selection][1], 1);
                        cm.gainItem(sail120, -sailfishRewards[selection][2]);
                        cm.gainItem(sail128, -sailfishRewards[selection][2]);
                        cm.gainItem(sail131, -sailfishRewards[selection][2]);
                        cm.gainItem(sail140, -sailfishRewards[selection][2]);
                        cm.gainItem(sail148, -sailfishRewards[selection][2]);
                    } else {
                        cm.sendOk("Please free up some room in the respective inventory slot.");
                    }
                } else {
                    cm.sendOk("It seems you do not have the required Sailfish amount.");
                }
            break;
            case 3:
                if (cm.haveItem(salmon150, salmonRewards[selection][2])
                    && cm.haveItem(salmon166, salmonRewards[selection][2])
                    && cm.haveItem(salmon183, salmonRewards[selection][2])
                    && cm.haveItem(salmon227, salmonRewards[selection][2])
                    && cm.haveItem(salmon228, salmonRewards[selection][2])) {
                    if (cm.canHold(salmonRewards[selection][1], 1)) {
                        cm.gainItem(salmonRewards[selection][1], 1);
                        cm.gainItem(salmon150, -salmonRewards[selection][2]);
                        cm.gainItem(salmon166, -salmonRewards[selection][2]);
                        cm.gainItem(salmon183, -salmonRewards[selection][2]);
                        cm.gainItem(salmon227, -salmonRewards[selection][2]);
                        cm.gainItem(salmon228, -salmonRewards[selection][2]);
                    } else {
                        cm.sendOk("Please free up some room in the respective inventory slot.");
                    }
                } else {
                    cm.sendOk("It seems you do not have the required Salmon amount.");
                }
        }
        cm.dispose();
    }
}
}