/* 
* @Author Saffron
* @NPC Shururu
* @Map Zipangu: Outside Ninja Castle
* @Description: Used for the TOPSPIN daily challenge
*/

var spinCombo = new Array( // pairs day variable with a top selection option
    [0, 4], // [sunday, maple lead spinning top] 
    [1, 2], // [monday, black spinning top] 
    [2, 5], // [tuesday, beyblade spinning top]
    [3, 6], // [wednesday, bright yellow spinning top]
    [4, 3], // [thursday, vintage wooden spinning top]
    [5, 0], // [friday, glow-in-the-dark spinning top]
    [6, 1]); // [saturday, rainbow spinning top]

var date = new Date();
var day = date.getDay(); // returns 0-6 value representing day of week

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && type == 0 && status != 2) {
        status--;
    } else if (mode == -1) {
        cm.dispose();
        return;
    } else {
        status++;
    }

    if (status == 0) {
        if (!cm.hasDailyEntry("TOPSPIN")) {       
            cm.sendYesNo("#d#eBoswell Daily#k\r\n \r\n#nI'm Shururu, and I am the king of top-spinning. Would you like to challenge me in a duel?");
        } else {
            cm.sendOk("Try again tomorrow, you have already completed this daily challenge!");
            status = 1;  
            cm.dispose();      
        }
    } else if (status == 1) {
        if (mode == 0) {//decline
            cm.sendNext("In that case you should come back later.");
        } else {
            cm.sendSimple("Very well, I hope you're ready to battle! I'll even let you pick any Spinning Top you want since you're going to " 
                        + "lose anyway. What top would you like to pick?  \r\n"
                        + "#L0# #bGlow-in-the-dark Spinning Top#k#l\r\n"
                        + "#L1# #bRainbow Spinning Top#k#l\r\n"
                        + "#L2# #bBlack Spinning Top#k#l\r\n"
                        + "#L7# #bYour Mother's Top#k#l\r\n" // hehe
                        + "#L3# #bVintage Wooden Spinning Top#k#l\r\n"
                        + "#L4# #bMaple Leaf Spinning Top#k#l\r\n"
                        + "#L5# #bBeyblade Spinning Top#k#l\r\n"
                        + "#L6# #bBright Yellow Spinning Top#k#l");
        }
    } else if (status == 2) {
        if (selection == 7) {
            cm.sendNext("#e...#n \r\n\r\n That's not even a Spinning Top!!! What are you some sort of pervert? How dare you say that about my Mom!!! \r\n\r\n #eShururu was unamused");
        } else if (selection == spinCombo[day][1]) {
            cm.sendNext("You're Spinning Top choice is doing surprisingly well. In fact, you watch the color drain from Shururu's face " 
                        + "as his Spinning Top finally comes to a full stop. \r\n\r\n You've beaten the master top-spinner! \r\n\r\n" 
                        + " #eShururu gives you 30 Bronze Trophy as a prize");
        } else if (selection == -1) { // if they exit out the window before choosing an option
            cm.sendNext("I understand not everyone wants to face me in a match of top-spinning.");
        } else {
            cm.sendNext("It looks like you got a pretty good spin going on your top. But you're still no match for me and my skills. " 
                        + "\r\n\r\n Just as Shururu had said, you watch as you're Spinning Top slowly falls. \r\n \r\n #eShururu is victorious!");
        }
        cm.completeDaily("TOPSPIN", false);
        cm.dispose();
    }
}