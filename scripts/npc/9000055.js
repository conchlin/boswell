/**
 * NPC: Aramia
 * MAP: Maple Hill
 * Function: Golden Maple Leaf Tree event
 * @Author Saffron - avenue gang
 */

var status = -1;
var sunshine = 4001165;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }

        if (mode == 1) status++;
        else status--;

        if (status == 0) {
            var treeSize = parseInt(cm.getTreeSize());
            var progress = treeSize / 5000;
            var pct = (progress * 100).toFixed(2) + "%";

            cm.sendSimple("Welcome to the Maple Hill! \r\n\r\n Summer is my favorite season and this is my favorite summer spot! It is home to " 
                + "the most beautiful Maple Tree. I'd really love to see it again! All it needs is a little warm summer sunshine. \r\n\r\n" +
                "Would you help me collect sunshine?\r\n\r\n"
                + "Current Progress: " +  pct
                + "\r\n\r\n#L0##bI'd like to hand over some sunshine" 
                + "\r\n#L2##bWhat do I do with my Golden Maple Leaf?");

        } else if (status == 1) {

            if (selection === 0) {
                cm.sendGetNumber("How many sunshine would you like to give me? \r\n You currently have "
                    + cm.itemQuantity(sunshine) + " Sunshines.\r\n\r\n", 1, 1, 2000); // min : 1 max: 2000 per transaction
            } else if (selection == 2) {
                cm.sendOk("What a beautiful Golden Leaf that is. I heard Nina is collecting them so you should make your way to the Free Market. " 
                    + "I'm only a kid so I don't have many prizes to hand out.");
                cm.dispose();
            }

        } else if (status === 2) {

            if (selection > 0) {
                if (!cm.haveItem(sunshine, selection)) {
                    cm.sendOk("It looks like you do not have that many sunshines.");
                } else {
                    cm.gainItem(sunshine, -selection);
                    var amount = parseInt(selection);
                    cm.addSunshines(amount);
                    cm.sendOk("Thank you for the " + amount + " Sunshines! Doesn't my tree look great!");
                }

                cm.dispose();
            }

        }
    }
}
