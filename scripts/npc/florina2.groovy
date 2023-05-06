/*
    Npc Name: Pason            
    Npc ID: 1002002
    Location Name: Lith Harbor
    Location ID: 104000000
            
    @Author: Connor
    @Created: 2023-04-09
*/

def sel = script.askMenu("Have you heard of the beach with a spectacular view of the ocean called #bFlorina Beach#k, " +
        "located near Lith Harbor? I can take you there right now for either #b1500 mesos#k, or if you have a " +
        "#bVIP Ticket to Florina Beach#k with you, in which case you'll be there for free.\r\n\r\n" +
        "#L0##b I'll pay 1500 mesos.#l\r\n#L1# I have a VIP Ticket to Florina Beach.#l\r\n#L2# What is a VIP Ticket to Florina Beach#k?#l")
if (sel == 0) {
    if (user.getMeso() < 1500) {
        script.say("I think you're lacking mesos. There are many ways to gather up some money, you know, like... selling your " +
                "armor... defeating monsters... doing quests... you know what I'm talking about.")
    } else {
        user.gainMeso(-1500)
        user.saveLocation("FLORINA")
        user.changeMap(110000000, "st00")
    }
} else if (sel == 1) {
    def ret = script.askYesNo("So you have a #bVIP Ticket to Florina Beach#k? You can always head over to Florina Beach " +
            "with that. Alright then, but just be aware that you may be running into some monsters there too. Okay, " +
            "would you like to head over to Florina Beach right now?")
    if (ret == 1) {
        if (user.haveItem(4031134)) {
            user.saveLocation("FLORINA")
            user.changeMap(110000000, "st00")
        } else {
            script.say("Hmmm, so where exactly is your #bVIP Ticket to Florina\r\nBeach#k? Are you sure you have one? Please double-check.")
        }
    } else {
        script.say("You must have some business to take care of here. You must be tired from all that traveling and hunting. " +
                "Go take some rest, and if you feel like changing your mind, then come talk to me.")
    }
} else if (sel == 2) {
    script.sayNext("You must be curious about a #bVIP Ticket to Florina Beach#k. Haha, that's very understandable. " +
            "A VIP Ticket to Florina Beach is an item where as long as you have in possession, you may make your " +
            "way to Florina Beach for free. It's such a rare item that even we had to buy those, but unfortunately " +
            "I lost mine a few weeks ago during my precious summer break.")
    script.say("I came back without it, and it just feels awful not having it. Hopefully someone picked it up and put it " +
            "somewhere safe. Anyway, this is my story and who knows, you may be able to pick it up and put it to good " +
            "use. If you have any questions, feel free to ask.")
} else {
    script.say("You must have some business to take care of here. You must be tired from all that traveling and hunting. " +
            "Go take some rest, and if you feel like changing your mind, then come talk to me.")
}
