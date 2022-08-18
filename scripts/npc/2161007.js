/**
 * Jenn's Brother (doesn't even get his own name smh)
 * works in conjunction with the JennsBrother.js event script
 */

 function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (cm.getMap().countMonsters() == 0) {
        var eim = cm.getEventInstance();
        
        cm.warp(211060200, 3);
        eim.clearPQ();
        
        cm.gainItem(4032831, 1); // himself lmao
        cm.setQuestProgress(3164, 1);
    } else {
        cm.sendOk("I want to go home... I want to see my brother.");
    }

    cm.dispose();
}

