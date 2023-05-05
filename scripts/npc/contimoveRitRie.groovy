/*
    Npc Name: Puro
    Npc ID: 1200004
    Location Name: Lith Harbor
    Location ID: 104000000
            
    @Author: Connor
    @Created: 2023-04-20
*/

def ret = script.askYesNo("Are you thinking about leaving Victoria Island and heading to our town? If you board " +
        "this ship, I can take you from #bLith Harbor#k to #bRien#k and back. Would you like to go to #bRien#k?" +
        "\r\n\r\n The trip costs #b1000 Mesos#k")

if (ret == 1) {
    if (user.getMeso() < 1000) {
        script.say("Hmm.. Are you sure that you have #b1000 Mesos#k? Check your Inventory and make sure you have " +
                "enough. You must pay the fee or I can't let you get on...")
    } else {
        user.gainMeso(-1000)
        user.changeMap(140020300, 0)
    }
} else {
    script.say("If you're not interested, then oh well...")
}