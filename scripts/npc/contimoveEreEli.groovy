/*
    Npc Name: Kiriru
    Npc ID: 1100003
    Location Name: Sky Ferry 
    Location ID: 130000210
            
    @Author: Connor
    @Created: 2023-04-20 
*/

def ret = npc.askYesNo(" Oh Hello...again. Do you want to leave Ereve and go somewhere else? If so, you've come" +
        " to the right place. I operate a ferry that goes from #bEreve#k to #bVictoria Island#k, " +
        "would you like to head over to #bVictoria Island#k? \r\n\r\n The trip costs #b1000 Mesos#k")

if (ret == 1) {
    if (user.getMeso() < 1000) {
        npc.say("Hmm.. Are you sure that you have #b1000 Mesos#k? Check your Inventory and make sure you have " +
                "enough. You must pay the fee or I can't let you get on...")
    } else {
        user.gainMeso(-1000)
        user.changeMap(101000400)
    }
} else {
    npc.say("If you're not interested, then oh well...")
}