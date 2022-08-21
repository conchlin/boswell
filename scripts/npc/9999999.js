/* RIP Nina
@Map - Free market
@Function - event handler
*/

var medal1 = 1142097; // Beta Tester
var medal2 = 1142098; // Established Beta Tester
var medal3 = 1142099; // Outstanding Beta Tester
var redChair = 3010376;
var blueChair = 3010377;
var powerChair = 3010301;
var elixirChair = 3010302;
var trophy = 4008002;
var alphaCompensation = [1012064, 3019999, 1052081, 1002562];
var weeklyCompensation = [5150044, 5150041, 2049100, 2340000];

var status;
var rewardPackage = 0;

// Boswell Beta Script
// function start() {
//     status = -2;
//     action(1, 0, 0);
// }
//
// function action(mode, type, selection) {
//
//     if (mode === -1) {
//         cm.dispose();
//     } else {
//         if (mode === 0) {
//             cm.dispose();
//             return;
//         }
//
//         if (mode === 1) status++;
//         else status--;
//
//         if (status === -1) {
//             cm.sendSimple("Welcome to Boswell! You can exchange #bBronze Trophies#k here for beta " +
//                 "reward packages. What package would you like?\r\n"
//                 + "#L0##i" + trophy + "##bPackage #1 #d(100 Trophies)#b\r\n"
//                 + "#L1##i" + trophy + "##bPackage #2 #d(250 Trophies)#b\r\n"
//                 + "#L2##i" + trophy + "##bPackage #3 #d(500 Trophies)#b\r\n");
//
//         } else if (status === 0) {
//
//             if (selection === 0) {
//                 rewardPackage = 1;
//                 cm.sendNext("Package #1 contains:\r\n\r\n" +
//                     "#i" + medal1 + "# #b#t" + medal1 + "#\r\n" +
//                     "  #v" + redChair + "#  #b#t" + redChair + "#\r\n\r\n" +
//                     "#kWould you like to exchange #r100 Trophies#k for these items?");
//             } else if (selection === 1) {
//                 rewardPackage = 2;
//                 cm.sendNext("Package #2 contains:\r\n\r\n" +
//                     "#i" + medal1 + "# #b#t" + medal1 + "#\r\n" +
//                     " #i" + blueChair + "#   #b#t" + blueChair + "#\r\n" +
//                     "#v" + powerChair + "#  #b#t" + powerChair + "#\r\n\r\n" +
//                     "#kWould you like to exchange #r250 Trophies#k for these items?");
//             } else if (selection === 2) {
//                 rewardPackage = 3;
//                 cm.sendNext("Package #3 contains:\r\n\r\n" +
//                     "#i" + medal1 + "# #b#t" + medal1 + "#\r\n" +
//                     "  #v" + redChair + "#  #b#t" + redChair + "#\r\n" +
//                     " #i" + blueChair + "#   #b#t" + blueChair + "#\r\n" +
//                     "#v" + powerChair + "#  #b#t" + powerChair + "#\r\n" +
//                     "#v" + elixirChair + "#  #b#t" + elixirChair + "#\r\n\r\n" +
//                     "#kWould you like to exchange #r500 Trophies#k for these items?");
//             }
//
//         } else if (status === 1) {
//
//             if (rewardPackage === 1) {
//                 if (cm.haveItem(trophy, 100)) {
//                     var curr = cm.getTrophy();
//                     cm.gainItem(medal1);
//                     cm.gainItem(redChair);
//                     cm.gainItem(trophy, -100)
//                     cm.setTrophy(curr + 100);
//                     cm.sendOk("Thanks for beta-testing Boswell!")
//                 } else {
//                     cm.sendOk("You don't seem to have enough #i" + trophy + "# #bBronze Trophies#k. Come back when you have #r100#k.")
//                 }
//             } else if (rewardPackage === 2) {
//                 if (cm.haveItem(trophy, 250)) {
//                     var curr = cm.getTrophy();
//                     cm.gainItem(medal2);
//                     cm.gainItem(blueChair);
//                     cm.gainItem(powerChair);
//                     cm.gainItem(trophy, -250)
//                     cm.setTrophy(curr + 250);
//                     cm.sendOk("Thanks for beta-testing Boswell!")
//                 } else {
//                     cm.sendOk("You don't seem to have enough #i" + trophy + "# #bBronze Trophies#k. Come back when you have #r250#k.")
//                 }
//             } else if (rewardPackage === 3) {
//                 if (cm.haveItem(trophy, 500)) {
//                     var curr = cm.getTrophy();
//                     cm.gainItem(medal3);
//                     cm.gainItem(redChair);
//                     cm.gainItem(blueChair);
//                     cm.gainItem(elixirChair);
//                     cm.gainItem(powerChair);
//                     cm.gainItem(trophy, -500)
//                     print(curr);
//                     print(cm.getTrophy());
//                     cm.setTrophy(curr + 500);
//                     print(curr);
//                     print(cm.getTrophy());
//                     cm.sendOk("Thanks for beta-testing Boswell!")
//                 } else {
//                     cm.sendOk("You don't seem to have enough #i" + trophy + "# #bBronze Trophies#k. Come back when you have #r500#k.")
//                 }
//             }
//             cm.dispose();
//
//         }
//     }
// }

// Boswell launch script
function start() {
    status = -1;
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

            cm.sendSimple("Which of our server features would you like to access?\r\n\r\n"
                + "#L1##bI'd like to check out the Boswell's feature list\r\n"
                + "#L2##bI'd like to learn about the trophy system\r\n"
                + "#L3##bI'd like to know about the Weekly PQ Tour");

        } else if (status === 1) {

            if (selection === 1) {
                cm.dispose(); // close current script start new one
                cm.openNpc(9999999, "features");

            } else if (selection === 2) {
                cm.dispose();
                cm.openNpc(9200000, "TrophyExchange");

            } else if (selection === 3) {
                cm.sendOk("The Weekly PQ Tour highlights a different party quest each week."
                + " The featured party quest will have a chance to drop Chaos/White Scrolls.\r\n\r\n"
                + " This week's featured party quest is: #e" + cm.getTourPQ());
                cm.dispose();
            }
        }
    }
}