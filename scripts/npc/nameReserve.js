var  options = ["How do I become eligible to reserve an IGN for release?",
                 "I'd like to reserve my current IGN!",
                 "I'd like to reserve a different IGN!"];
var currName;
var nameToReserve;

function start() {
    status = -2
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;

        if(status == -1) { // first window
            var nameCheck = cm.getNameReserve() == null ? "No Reservation" : cm.getNameReserve();
            var text = "I've been doing a lot of research lately and I "
            + "think i've figured out how to reserve names! But it's only for a limited time because I need "
            + "to go back to the moon!\r\n#eCurrent name reservation is:#n#k " + nameCheck + "\r\n";
            var optionCheck = (cm.getPlayer().getLevel() >= 80) && (cm.getPlayer().getClearance() == 15) ?
                                options.length
                                : 1;
            for (var i = 0; i < optionCheck; i++) {
                text += "#L" + i + "##b" + options[i] + "#k#l\r\n";
            }

        cm.sendSimple(text);
        } else if (status == 0) {

            if (selection == 0) {
                cm.sendPrev("Well you need to complete the Hype for Wipe event, silly! #eAll you have to do is make a "
                + "character during the event period (November 14th - December 2nd). Any character made during that "
                + "time that reaches level 80 can reserve a name!#n\r\n\r\n Once you've done that come back to me "
                + "and we can secure you a really cool name.");
            } else if (selection == 1) {
                cm.sendYesNo("Are you sure you want to reserve the name #h #?");

            } else if (selection == 2) {
                cm.sendGetText("What name would you like to reserve?");
            }
        } else if (status == 1) {

            if (type == 1) {
                currName = cm.getPlayer().getName();
                print(currName);
                if (cm.hasNameReserve()) {
                    cm.sendOk("It looks like this account has already reserved a name!");
                } else if (cm.isNameAvailable(currName)) {
                    cm.sendOk("It looks like someone has already reserved the name #h #. Please try another name.");
                } else {
                    cm.sendOk("I've reserved the name #h # for you. #h # really is a nice name!");
                    cm.addNameReserve(currName);
                }
                cm.dispose();
            } else if (type == 2) {
             nameToReserve = cm.getText();
             cm.sendYesNo("Are you sure you want to reserve the name " + nameToReserve + "?");
            }
        } else if (status == 2) {

            if (mode == 1) {
                if (cm.hasNameReserve()) {
                    cm.sendOk("It looks like this account has already reserved a name!");
                } else if (cm.isNameAvailable(nameToReserve)) {
                    cm.sendOk("It looks like someone has already reserved the name " + nameToReserve + ". Please try another name.");
                } else {
                    cm.sendOk("I've reserved the name " + nameToReserve + " for you. "+ nameToReserve +" really is a nice name!");
                    cm.addNameReserve(nameToReserve);
                }
                cm.dispose();
            }
        }
    }
}