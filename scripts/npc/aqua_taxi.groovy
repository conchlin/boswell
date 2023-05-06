/*
    Npc Name: Dolphin
    Npc ID: 2060009
    Location Name: Aquarium
    Location ID: 230000000
            
    @Author:Connor
    @Created: 2023-04-24
*/

def menu = ""
def payment = false
def atHerbTown = false

if (user.getMapId() == 251000100) {
    atHerbTown = true
}
if (user.haveItem(4031242)) {
    if (atHerbTown) {
        menu = "#L0##bI will use #t4031242##k to move to #b#m230030200##k.#l" +
                "\r\n#L1#Go to #b#m230000000##k after paying #b10000mesos#k.#l"
    } else {
        menu = "#L0##bI will use #t4031242##k to move to #b#m230030200##k.#l" +
                "\r\n#L1#Go to #b#m251000000##k after paying #b10000mesos#k.#l"
    }
} else {
    if (atHerbTown) {
        menu = "#L0#Go to #b#m230030200##k after paying #b1000mesos#k.#l" +
                "\r\n#L1#Go to #b#m230000000##k after paying #b10000mesos#k.#l"
    } else {
        menu = "#L0#Go to #b#m230030200##k after paying #b1000mesos#k.#l" +
                "\r\n#L1#Go to #b#m251000000##k after paying #b10000mesos#k.#l"
    }
    payment = true
}

def sel = script.askMenu("Ocean are all connected to each other. Place you can't reach by foot can easily reached oversea. " +
        "How about taking #bDolphin Taxi#k with us today?\r\n" + menu)

if (sel == 0) {
    if (payment) {
        if (user.getMeso() < 1000) {
            script.say("I don't think you have enough money...")
        } else {
            user.gainMeso(-1000)
        }
    } else {
        user.gainItem(4031242,-1)
    }
    user.changeMap(230030200, 2)
} else if (sel == 1) {
    if (user.getMeso() < 1000) {
        script.say("I don't think you have enough money...")
    } else {
        user.gainMeso(-1000)
        user.changeMap(atHerbTown ? 230000000 : 251000100)
    }
}