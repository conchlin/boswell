/*
	CHAIR GACHAPON - HENESYS
 */

var status = -1;
var ticketId = 5220002;
var mesoCost = 1000000000;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (cm.getPlayer().getLevel() < 50) {
        cm.sendOk("You must be level 50 to use the Gachapon.");
        cm.dispose();
    } else {
        if (mode < 0)
            cm.dispose();
        else {
            if (mode === 1)
                status++;
            else
                status--;

            if (status === 0 && mode === 1) {
                cm.sendSimple("Welcome to the Chair Gachapon. Would you like to use a ticket or mesos?" +
                    "\r\n\r\n #L0# #i5220002# Chair Gachapon Ticket#l" +
                    "\r\n\r\n #L1# #i5200000# 1,000,000,000 Mesos#l");
            } else if (status === 1) {

                if (cm.canHold(3010001)) { // check slots

                    if (selection === 0) {

                        if (cm.haveItem(ticketId)) {
                            cm.gainItem(ticketId, -1);
                            cm.doGachapon();
                        } else {
                            cm.sendOk("You do not seem to have a #bChair Gachapon Ticket#k");
                            cm.dispose;
                        }

                    } else {

                        if (cm.getMeso() > mesoCost) {
                            cm.gainMeso(-mesoCost);
                            cm.doGachapon();
                        } else {
                            cm.sendOk("You do not seem to have #b1,000,000,000 mesos.#k");
                            cm.dispose;
                        }
                    }
                } else {
                    cm.sendOk("Clear some room in your #rSET-UP#k slot");
                    cm.dispose;

                }
            } else {
                cm.dispose();
            }
        }
    }
}