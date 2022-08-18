var status = -1;
// var medal1 = 1142097; // Beta Tester
// var medal2 = 1142098; // Established Beta Tester
// var medal3 = 1142099; // Outstanding Beta Tester
// var trophies = 4000038;
// var choice = 0;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0) {
            cm.dispose();
            return;
        }

        if (mode == 1)
            status++;
        else
            status--;

        if (status == 0) {
            cm.sendOk("I'm telling you, I really AM from WIZET!");
            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}

//
// function action(mode, type, selection) {
//
//     if (mode == -1)
//         cm.dispose();
//     else {
//         if (mode == 0) {
//             cm.dispose();
//             return;
//         }
//
//         if (mode == 1)
//             status++;
//         else
//             status--;
//
//         if (status == 0) {
//             if (cm.getPlayer().isCheater()) {
//                 cm.sendOk("Sorry, you do not qualify for my Beta rewards. Speak to #rNina#l in the #bFree Market#l" +
//                     " instead");
//                 cm.dispose();
//             } else {
//                 cm.sendSimple("Those #bevent trophies#k look real shiny, would you like to trade me some for a beta medal? " +
//                 "#r[Please unequip the previous tier medal when exchanging]#k" +
//                 "\r\n #L0##b #i1142097# Beta Tester \r\n(40 Event Trophies)#l" +
//                 "\r\n #L1##b #i1142098# Established Beta Tester \r\n(95 Event Trophies and Beta Tester)#l" +
//                 "\r\n #L2##b #i1142099# Outstanding Beta Tester \r\n(195 Event Trophies and Established Beta Tester#l" +
//                 "\r\n \r\n #L3##b I would like to collect some missed trophies!#l");
//             }
//         } else if (status == 1) {
//
//             if (selection == 0) {
//                 if (cm.haveItem(trophies, 40)) { // you should have 40 trophies at level 30
//                     if (cm.canHold(medal1)) {
//                         cm.gainItem(medal1, 1);
//                         cm.gainItem(trophies, -40);
//                         cm.sendOk("Enjoy the open beta!");
//                         cm.dispose();
//                     } else {
//                         cm.sendOk("Please clear some inventory space so you can accept the beta reward.");
//                         cm.dispose();
//                     }
//                 } else {
//                     cm.sendOk("It looks like you don't have enough trophies.");
//                     cm.dispose();
//                 }
//
//             } else if (selection == 1) {
//
//                 if (cm.haveItem(medal1, 1) && cm.haveItem(trophies, 95)) { // you should have 95 trophies at level 55
//                     if (cm.canHold(medal2)) {
//                         cm.gainItem(medal2, 1);
//                         cm.gainItem(medal1, -1);
//                         cm.gainItem(trophies, -95);
//                         cm.sendOk("Enjoy the open beta!");
//                         cm.dispose();
//                     } else {
//                         cm.sendOk("Please clear some inventory space so you can accept the beta reward.");
//                         cm.dispose();
//                     }
//                 } else {
//                     cm.sendOk("It looks like you're missing something.");
//                     cm.dispose();
//                 }
//
//             } else if (selection == 2) {
//
//                 if (cm.haveItem(medal2, 1) && cm.haveItem(trophies, 195)) { // you should have 195 trophies at level 85
//                     if (cm.canHold(medal3)) {
//                         cm.gainItem(medal3, 1);
//                         cm.gainItem(medal2, -1);
//                         cm.gainItem(trophies, -195);
//                         cm.sendOk("Enjoy the open beta!");
//                         cm.dispose();
//                     } else {
//                         cm.sendOk("Please clear some inventory space so you can accept the beta reward.");
//                         cm.dispose();
//                     }
//                 } else {
//                     cm.sendOk("It looks like you're missing something.");
//                     cm.dispose();
//                 }
//
//             } else if (selection == 3) {
//                 choice = 1;
//                 cm.sendYesNo("It looks like you currently have " + cm.getClearance() + " trophies that you can collect.\r\n\r\n Would you like to collect them now?");
//             }
//         } else if (status == 2) {
//             if (choice == 1) {
//                 var eventCost = cm.getClearance();
//                 cm.gainItem(trophies, eventCost);
//                 cm.setClearance(0);
//                 cm.dispose();
//             }
//         }
//     }
// }