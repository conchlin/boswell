/*
    Npc Name: Shanks            
    Npc ID: 22000
    Location Name: Southperry
    Location ID: 2000000
            
    @Author: Connor
    @Created: 2023-04-06
*/

def ret = npc.askYesNo("Take this ship and you'll head off to a bigger continent. For #e150 mesos#n, I'll take you to " +
        "#bVictoria Island#k. The thing is, once you leave this place, you can't ever come back. What do you think? " +
        "Do you want to go to Victoria Island?")
if (ret == 1) { // yes
    if (user.haveItem(4031801)) {
        npc.sayNext("Okay, now give me 150 mesos... Hey, what's that? Is that the recommendation letter from Lucas, " +
                "the chief of Amherst? Hey, you should have told me you had this. I, Shanks, recognize greatness when " +
                "I see one, and since you have been recommended by Lucas, I see that you have a great, great potential " +
                "as an adventurer. No way would I charge you for this trip!")
        user.gainItem(4031801, 1)
        user.changeMap(104000000, 0)
    } else {
        if (user.getLevel() > 6) {
            if (user.getMeso() < 150) {
                npc.say("What? You're telling me you wanted to go without any money? You're one weirdo...")
            } else {
                npc.sayNext("Awesome! #e150#n mesos accepted! Alright, off to Victoria Island!")
                user.gainMeso(-150)
                user.changeMap(104000000, 0)
            }
        } else {
            npc.say("Let's see... I don't think you are strong enough. You'll have to be at least Level 7 to go to Victoria Island.")
        }
    }
} else {
    npc.say("Hmm... I guess you still have things to do here?")
}