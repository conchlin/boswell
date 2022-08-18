/* RIP Nina
@Map - Free market
@Function - event handler
*/

var status = -1;
var goldenLeaf = 4001168;

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
            
            var player = cm.getPlayer();
            
            if (cm.haveItem(4001168)) { // if they have the leaf we dont need to track them
                cm.removeTreeContributor(player);
            }

            cm.sendSimple("Summer is tied for my favorite season! In fact, the weather has been so nice that Sunshine is " 
            + "literally dropping from all mobs in Boswell.\r\n\r\n" 
            + "#L1##bWhat Should I do with all this Sunshine?\r\n" 
            + "#L2##bI have a Golden Leaf for you!\r\n"
            + "#L3##bI would like to redeem my Maple Tree contribution#l#k\r\n\r\n" 

            + "Or would you rather gain access to some of our new features?\r\n\r\n"
            + "#L4##bI'd like to check out the Ores of the Month\r\n"
            + "#L5##bI'd like to learn about the trophy system\r\n"
            + "#L6##bI'd like to test my skill with a Scavenger Hunt!");

        } else if (status == 1) {

            if (selection == 1) {
                cm.sendOk("You can use the Dimensional Portal found in most towns to travel to Maple Hill and see Aramia. " 
                + "She is collecting Sunshine so that her tree can grow.");
                cm.dispose();
            } else if (selection == 2) {
                if (cm.haveItem(goldenLeaf, 1)) {
                    cm.giveEventDrop();
                } else {
                    cm.sendOk("It seems you do not have a Golden Maple Leaf.");
                }
                cm.dispose();
            } else if (selection == 3) {
                var player = cm.getPlayer();
                if (cm.isTreeContributor(player)) {
                    cm.giveEventDrop();
                    //cm.removeTreeContributor(player);
                } else {
                    cm.sendOk("It appears that you do not meet the requirements to use this feature.");
                }
                cm.dispose();
            
            } else if (selection == 4) {
                cm.dispose(); // close current script start new one
                cm.openNpc(9010002);
            } else if (selection == 5) {
                cm.dispose(); // close current script start new one
                cm.openNpc(9200000, "TrophyExchange");
            } else if (selection == 6) {
                cm.dispose(); // close current script start new one
                cm.openNpc(9201051, "ScavengerHunt");
            }

        }
    }
}