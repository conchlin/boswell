/*
    Npc Name: Camel Cab
    Npc ID: 2110005
    Location Name: Sahel 1
    Location ID: 260020700
            
    @Author: Connor
    @Created: 2023-04-24
*/

def toMagatia = "Would you like to take the #bCamel Cab#k to #bMagatia#k, the town of Alchemy? The fare is #b1500 mesos#k."
def toAriant = "Would you like to take the #bCamel Cab#k to #bAriant#k, the town of Burning Roads? The fare is #b1500 mesos#k."

def ret = script.askYesNo(user.getMapId() == 260020000 ? toMagatia : toAriant)

if (ret == 1) {
    if (user.getMeso() < 1500) {
        script.say("I am sorry, but I think you are short on mesos. I am afraid I can't let you ride this if you do " +
                "not have enough money to do so. Please come back when you have enough money to use this.")
    } else {
        user.changeMap(user.getMapId() == 260020000 ? 260020700 : 260020000)
        user.gainMeso(-1500)
    }
} else {
    script.say("Hmmm... too busy to do it right now? If you feel like doing it, though, come back and find me.")
}