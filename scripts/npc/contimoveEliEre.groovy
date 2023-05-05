/*
    Npc Name: Kiriru
    Npc ID: 1100007
    Location Name: Sky Ferry &lt;To Ereve>
    Location ID: 101000400
            
    @Author: Connor
    @Created: 2023-04-20 
*/

def ret = script.askYesNo("Oh, and.. so.. this ship will take you to #bEreve#k, the place where you'll find crimson " +
        "leaves soaking up the sun, the gently breeze that glides past the stream, and the Empress of Maple, Cygnus. " +
        "Would you like to head over to #bEreve#k? \r\n\r\n The trip costs #b1000 Mesos#k")

if (ret == 1) {
    if (user.getMeso() < 1000) {
        script.say("Hmm.. Are you sure that you have #b1000 Mesos#k? Check your Inventory and make sure you have " +
                "enough. You must pay the fee or I can't let you get on...")
    } else {
        user.gainMeso(-1000)
        user.changeMap(130000210)
    }
} else {
    script.say("If you're not interested, then oh well...")
}