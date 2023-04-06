/*
    Npc Name: Phil            
    Npc ID: 1002000
    Location Name: Lith Harbor
    Location ID: 104000000
            
    @Author: Connor
    @Created: 2023-04-06
*/

def towns = [["Perion", 102000000, 120], ["Ellinia", 101000000, 120], ["Henesys", 100000000, 80], ["Kerning City", 103000000, 100], ["Nautilus Harbor", 120000000, 100]]
def cost = 1
def menu = ""
def menu1 = ""

private void goTown(String fieldName, int fieldID, int fee) {
    def ret = npc.askYesNo("You don't have anything else to do here, right? Would you like to go to #b" + fieldName + "#k? " +
            "It will cost #b" + fee + " mesos#k.")
    if (ret == 1) { //yes
        if (user.getMeso() < fee) {
            npc.say("You don't have enough money. Without enough money, you won't be able to ride the taxi.")
        } else {
            user.changeMap(fieldID, 0)
        }
    } else {
        npc.say("There's a lot to see in this city. Come back and look for us when you need to go somewhere else.")
    }
}

npc.sayNext("Do you wanna head over to some other town? With a little money involved, I can make it happen. It's a tad " +
        "expensive, but I run a special 90% discount for beginners.")
def sel = npc.askMenu("It's understandable that you may be confused about this place if this is your first " + "time around. " +
        "If you got any questions about this place, fire away.\r\n#L0##bWhat kind of towns are here in Victoria Island?" +
        "#l\r\n#L1#Please take me somewhere else.#k#l")
if (sel == 0) {
    def sel2 = npc.askMenu("There are 6 big towns here in Victoria Island. Which of those do you want to know more of?" +
            "\r\n#b#L0#Lith Harbor#l\r\n#L1#Perion#l\r\n#L2#Ellinia#l\r\n#L3#Henesys#l\r\n#L4#Kerning City#l\r\n#L5#Nautilus Harbor#l\r\n#L6#Sleepywood#l")
    if (sel2 == 0) {
        npc.sayNext("The town you are at is Lith Harbor! Alright I'll explain to you more about #bLith Harbor#k. " +
                "It's the place you landed on Victoria Island by riding The Victoria. That's Lith Harbor. " +
                "A lot of beginners who just got here from Maple Island start their journey here.")
        npc.sayNext("It's a quiet town with the wide body of water on the back of it, thanks to the fact that the " +
                "harbor is located at the west end of the island. Most of the people here are, or used to be fisherman," +
                " so they may look intimidating, but if you strike up a conversation with them, they'll be friendly to you.")
        npc.sayNext("Around town lies a beautiful prairie. Most of the monsters there are small and gentle, " +
                "perfect for beginners. If you haven't chosen your job yet, this is a good place to boost up your level.")
    } else if (sel2 == 1) {
        npc.sayNext("Alright I'll explain to you more about #bPerion#k. It's a warrior-town located at the " +
                "northern-most part of Victoria Island, surrounded by rocky mountains. With an unfriendly atmosphere," +
                " only the strong survives there.")
        npc.sayNext("Around the highland you'll find a really skinny tree, a wild hog running around the place, and " +
                "monkeys that live all over the island. There's also a deep valley, and when you go deep into it, " +
                "you'll find a humongous dragon with the power to match his size. Better go in there very carefully, " +
                "or don't go at all.")
        npc.sayNext("If you want to be a the #bWarrior#k then find #r#p1022000##k, the chief of Perion. If you're " +
                "level 10 or higher, along with a good STR level, he may make you a warrior afterall. If not, better " +
                "keep training yourself until you reach that level.")
    } else if (sel2 == 2) {
        npc.sayNext("Alright I'll explain to you more about #bEllinia#k. It's a magician-town located at the fart east " +
                "of Victoria Island, and covered in tall, mystic trees. You'll find some fairies there, too; They don't " +
                "like humans in general so it'll be best for you to be on their good side and stay quiet.")
        npc.sayNext("Near the forest you'll find green slimes, walking mushrooms, monkeys and zombie monkeys all " +
                "residing there. Walk deeper into the forest and you'll find witches with the flying broomstick " +
                "navigating the skies. A word of warning: unless you are really strong, I recommend you don't go near them.")
        npc.sayNext("If you want to be the #bMagician#k, search for #r#p1032001##k, the head wizard of Ellinia. He may " +
                "make you a wizard if you're at or above level 8 with a decent amount of INT. If that's not the case, " +
                "you may have to hunt more and train yourself to get there.")
    } else if (sel2 == 3) {
        npc.sayNext("Alright I'll explain to you more about #bHenesys#k. It's a bowman-town located at the southernmost " +
                "part of the island, made on a flatland in the midst of a deep forest and prairies. The weather's just " +
                "right, and everything is plentiful around that town, perfect for living. Go check it out.")
        npc.sayNext("Around the prairie you'll find weak monsters such as snails, mushrooms, and pigs. According to " +
                "what I hear, though, in the deepest part of the Pig Park, which is connected to the town somewhere, " +
                "you'll find a humongous, powerful mushroom called Mushmom every now and then.")
        npc.sayNext("If you want to be the #bBowman#k, you need to go see #r#p1012100##k at Henesys. With a level " +
                "at or above 10 and a decent amount of DEX, she may make you be one afterall. If not, go train yourself," +
                " make yourself stronger, then try again.")
    } else if (sel2 == 4) {
        npc.sayNext("Alright I'll explain to you more about #bKerning City#k. It's a thief-town located at the " +
                "northwest part of Victoria Island, and there are buildings up there that have just this strange " +
                "feeling around them. It's mostly covered in black clouds, but if you can go up to a really high place, " +
                "you'll be able to see a very beautiful sunset there.")
        npc.sayNext("From Kerning City, you can go into several dungeons. You can go to a swamp where alligators " +
                "and snakes are abound, or hit the subway full of ghosts and bats. At the deepest part of the " +
                "underground, you'll find Lace, who is just as big and dangerous as a dragon.")
        npc.sayNext("If you want to be the #bThief#k, seek #r#p1052001##k, the heart of darkness of Kerning City. " +
                "He may well make you the thief if you're at or above level 10 with a good amount of DEX. If not, " +
                "go hunt and train yourself to reach there.")
    } else if (sel2 == 5) {
        npc.sayNext("Here's a little information on #b#m120000000##k. It's a submarine that's currently parked in " +
                "between Ellinia and Henesys in Victoria Island. That submarine serves as home to numerous pirates. " +
                "You can have just as beautiful a view of the ocean there as you do here in Lith Harbor.")
        npc.sendNext("#m120000000# is parked in between Henesys and Ellinia, so if you step out just a bit, you'll " +
                "be able to enjoy the view of both towns. All the pirates you'll meet in town are very gregarious and friendly as well.")
        npc.sendNext("If you are serious about becoming a #bPirate#k, then you better meet the captain of " +
                "#m120000000#, #r#p1090000##k. If you are over Level 10 with 20 DEX, then she may let you become one. " +
                "If you aren't up to that level, then you'll need to train harder to get there!")
    } else if (sel2 == 6) {
        npc.sayNext("Alright I'll explain to you more about #bSleepywood#k. It's a forest town located at the southeast " +
                "side of Victoria Island. It's pretty much in between Henesys and the ant-tunnel dungeon. There's a " +
                "hotel there, so you can rest up after a long day at the dungeon ... it's a quiet town in general.")
        npc.sayNext("In front of the hotel there's an old buddhist monk by the name of #r#p1061000##k. Nobody knows " +
                "a thing about that monk. Apparently he collects materials from the travellers and create something, " +
                "but I am not too sure about the details. If you have any business going around that area, please check that out for me.")
        npc.sayNext("From Sleepywood, head east and you'll find the ant tunnel connected to the deepest part of the " +
                "Victoria Island. Lots of nasty, powerful monsters abound so if you walk in thinking it's a walk in " +
                "the park, you'll be coming out as a corpse. You need to fully prepare yourself for a rough ride before going in.")
        npc.sayNext("And this is what I hear ... apparently, at Sleepywood there's a secret entrance leading you " +
                "to an unknown place. Apparently, once you move in deep, you'll find a stack of black rocks that " +
                "actually move around. I want to see that for myself in the near future ...")
    }
} else if (sel == 1) {
    menu = "There's a special 90% discount for all beginners. Alright, where would you want to go?#b"
    if (!user.isBeginnerJob()) {
        cost = 10
        menu = "Oh you aren't a beginner, huh? Then I'm afraid I may have to charge you full price. Where would you like to go?#b"
    }
    for (int i = 0; i < towns.size(); i++) {
        menu += "\r\n#L" + i + "#" + towns[i][0] + " (" + (towns[i][2]) + " mesos)#l"
    }
    def sel3 = npc.askMenu(menu + menu1)
    if (sel3 in (0..<towns.size())) {
        goTown(towns[sel3][0], towns[sel3][1], towns[sel3][2])
    }
}
