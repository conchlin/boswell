/**
----------------------------------------------------------------------------------
	Boat from Ellinia to Orbis
    Cherry
    Gives two options an (1) instant warp or (2) regular ship ride
----------------------------------------------------------------------------------
**/
var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (status == 0) {
        cm.sendSimple("The ship is ready for takoff! Do you want to travel to #bOrbis#k? Or, would you rather board the ship? Please do hurry and decide before it leaves!" +
        "\n\r\n\r #L0##bI would like to travel to Orbis#k (10,000 fee) \r\n #L1# #bI would like to ride the ship#k");
    } else if (status == 1) {

        if (selection == 0) {
            if (cm.getMeso() < 10000) {
                cm.sendOk("Hmm.. Are you sure that you have #b1000 Mesos#k? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you get on...");
                cm.dispose();
            } else {
                cm.gainMeso(-10000);
                cm.warp(200000100, 0);
                cm.dispose();
            }
        } else {
            var em = cm.getEventManager("Boat");
            if (em.getProperty("entry") == "true") {
                cm.warp(101000301);
                cm.dispose();
            } else {
                cm.sendOk("The boat to Orbis has already taken off, please be patient for the next one.");
                cm.dispose();
            }
        }
    }
}