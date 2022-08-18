

var status = 0;
var ticketSelection = -1;
var text = "Here's the ticket reader.";
var hasTicket = false;
var NLC = false;
var em;

function start() {
	cm.sendSimple("Pick your destination.\n\r\n#L0##bKerning Square Shopping Center#l\n\n\r\n#L1#Enter Contruction Site#l");
}

function action(mode, type, selection) {
    if (mode == -1) {
    	cm.dispose();
    	return;
    } else if (mode == 0) {
           cm.dispose();
           return;
    } else {
    	status++;
    }
    if (status == 1) {
        if (selection == 0) {
    		var em = cm.getEventManager("KerningTrain");
                if (!em.startInstance(cm.getPlayer())) {
                    cm.sendOk("The passenger wagon is already full. Try again a bit later.");
                }
                
        	cm.dispose();
        	return;
        } else if (selection == 1) {
            if (cm.haveItem(4031036) || cm.haveItem(4031037) || cm.haveItem(4031038)) {
                text += " You will be brought in immediately. Which ticket you would like to use?#b";
                for (var i = 0; i < 3; i++) {
	                if (cm.haveItem(4031036 + i)) {
	                    text += "\r\n#b#L" + (i + 1) + "##t" + (4031036 + i) +"#";
	        		}
	            }
                cm.sendSimple(text);  
                hasTicket = true;
            } else { 
            	cm.sendOk("It seems as though you don't have a ticket!");
            	cm.dispose();
            	return;
            }
        } 
    } else if (status == 2) {
    	if (hasTicket) {
    		ticketSelection = selection;
            if (ticketSelection > -1) {
                cm.gainItem(4031035 + ticketSelection, -1);
                cm.warp(103000897 + (ticketSelection * 3), 0);
                hasTicket = false;
                cm.dispose();
                return;
            }
    	}
    }
}