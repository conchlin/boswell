var  options = ["What is the Daily Challenge System?",
                 "I'd like to check my progress!",
                 "I'd like to redeem my daily login challenge!",
                  "When do Daily Challenges reset?",
                   "But wait... What's in it for me?"];

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
            var text = "I'm here to help with all your Daily Challenge needs! What do you want help with today?\r\n";
            for(var i = 0; i < options.length; i++) {
                text += "#L" + i + "##b" + options[i] + "#k#l\r\n";
            }

        cm.sendSimple(text);
        } else if (status == 0) {

            if (selection == 0) {
                cm.sendPrev("Each day you have the ability to complete the Boswell daily challenges. This provides " 
                + "a way to break up the monotonous grind of leveling and can be asteady income of trophies! " 
                + "These tasks range from collecting ETC, to betting on races, or even searching the ocean for " 
                + "buried treasure! \r\n \r\n For more information you can view your daily progress with me anytime.");
            } else if (selection == 1) {
                var login = cm.hasDailyEntry("LOGIN") ? "#e#gcompleted#n#k" : "#e#rnot complete#k#n";
                var treasure = cm.hasDailyEntry("TREASURE") ? "#e#gcompleted#n#k" : "#e#rnot complete#k#n";
                var mason = cm.hasDailyEntry("MASON") ? "#e#gcompleted#n#k" : "#e#rnot complete#k#n";
                var oasis = cm.hasDailyEntry("OASIS") ? "#e#gcompleted#n#k" : "#e#rnot complete#k#n";
                var watchtower = cm.hasDailyEntry("WATCHTOWER") ? "#e#gcompleted#n#k" : "#e#rnot complete#k#n";
                var topspin = cm.hasDailyEntry("TOPSPIN") ? "#e#gcompleted#n#k" : "#e#rnot complete#k#n";

                cm.sendPrev("This is your current daily challenge progress: \r\n \r\n"
                + "#eLogin:#n " + login + "\r\n"
                + "#eBuried Treasure:#n " + treasure + "\r\n"
                + "#eMason, the Collector:#n " + mason + "\r\n"
                + "#ePalace Oasis:#n " + oasis + "\r\n"
                + "#eWatchtower:#n " + watchtower + "\r\n"
                + "#eTop-spinning Battle:#n " + topspin + "\r\n");
            } else if (selection == 2) {
                if (cm.hasProgressEntry("LOGIN")) { // meaning login req was completed
                    cm.completeDaily("LOGIN", true);
                } else {
                    cm.sendPrev("It appears you have not met the requirements for the login reward."
                            + "Please try again when you have completed them.");
                }
            } else if (selection == 3) {
                cm.sendPrev("The Boswell Daily System resets everyday at 0:00 UTC. You can keep track of the current " 
                + "server time by using @time or by keeping an eye out for the daily reset server notice.");
            } else if (selection == 4) {
                cm.sendPrev("There is a wide array of prizes that is dependant on the specific challenge. The most " 
                + "common rewards would be: \r\n \r\n " 
                + "TBD" // provide list of rewards here
                );
            }

        } 
    } // no third window needed all challenges are completed through their own npc
}