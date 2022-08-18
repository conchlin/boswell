/**
 * quest: Desolate Castle
 * npc: krag
 * player is prompted at level 110 to teleport to desolate moor in order to talk to Krag
 * this starts the LHC questline
 */

var status = -1;

function start(mode, type, selection) {
    
    if (mode == 1) status++;

    if (status == 0) {
        qm.sendNext("(You've recieved a strange letter, but there isn't a name on it...) I guess I'll give it a read....", 2);
    } else if (status == 1) {
        qm.sendNext("#b#p2161012##k, a Silent Crusade agent, wrote the letter and he's trying to investigate the #rLion " 
                    + "King's Castle#k. How would you even get there?", 2);
    } else if (status == 2) {
        qm.sendNext("The spell cast on the letter teleported you! While you're here, help #b#p2161012##k investigate the " 
                    + "Dark Energy that's pouring from the Lion King's Castle.", 2); 
    } else if (status ==3) {
        qm.warp(211060000, 0);
        qm.startQuest(3138);
        qm.dispose();
    }
}