/* Author: michu
	NPC Name: 		Peter
	Map(s): 		Maple Road: Entrance - Mushroom Town Training Camp (3)
	Description: 	Job selector, warps to respective maps with beginner equipment
*/

var status = -1;
var choice = -1;

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

        if (mode === 1)
            status++;
        else
            status--;

        if (status === 0) {
            cm.sendSimple("Thanks for participating in the Boswell Beta period! " +
                "\r\n\r\nBegin your new journey by selecting the hero you would like to become below." +
                "\r\n #L0##b Explorer#l" +
                "\r\n #L1##b Knight of Cygnus#l" +
                "\r\n #L2##b Aran#l");

        } else if (status === 1) {

            if (selection === 0) {
                choice = 0;
                cm.sendYesNo("Are you sure you want to be an #bExplorer#l? " +
                    "\r\n#r[Clicking YES will select your class and warp you to Maple Island]");
            } else if (selection === 1) {
                choice = 1;
                cm.sendYesNo("Are you sure you want to be a #bKnight of Cygnus#l? " +
                    "\r\n#r[Clicking YES will select your class and warp you to Ereve]");
            } else if (selection === 2) {
                choice = 2;
                cm.sendYesNo("Are you sure you want to be an #bAran#l? " +
                    "\r\n#r[Clicking YES will select your class and warp you to Rien]");
            }

        } else if (status === 2) {

            if (choice === 0) {
                explorer();
                cm.dispose();
            } else if (choice === 1) {
                cygnus();
                cm.dispose();
            } else if (choice === 2) {
                aran();
                cm.dispose();
            }
        }
    }
}

function explorer() {
    cm.warp(10000);
    cm.changeJobById(0);
    gainItemBasedOnGender();
    cm.gainItem(4161001); // Beginner's Guide
}

function cygnus() {
    cm.warp(130030000);
    cm.changeJobById(1000);
    gainItemBasedOnGender();
    cm.gainItem(4161047); // Noblesse Guide
}

function aran() {
    cm.warp(914000000);
    cm.changeJobById(2000);
    cm.gainItem(1042167); // Simple Warrior Top
    cm.gainItem(1062115); // Simple Warrior Pants
    cm.gainItem(1072383); // Average Musashi Shoes
    cm.gainItem(1442079); // Simple Polearm
    cm.gainItem(4161048); // Aran's Guide
}
function gainItemBasedOnGender() {
    if (cm.getPlayer().getGender() === 0) {
        cm.gainItem(1040002); // White Undershirt
        cm.gainItem(1060002); // Brown Cotton Shorts
    } else {
        cm.gainItem(1041002); // White Tubetop
        cm.gainItem(1061002); // Red Miniskirt
    }
    cm.gainItem(1072005); // Leather Sandals
    cm.gainItem(1322005); // Wooden Club
}