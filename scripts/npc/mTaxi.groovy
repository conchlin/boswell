/*
    
    Npc Name: VIP Cab            
    Npc ID: 1032005
    Location Name: Ellinia/Lith Harbor
    Location ID: 101000000
            
    @Author: Auto Generated
    @Created: 2023-03-03 
*/

def beginner = false
def cost = 10000

if (user.isBeginnerJob()) {
    beginner = true
}

npc.sayNext("Hi there! This cab is for VIP customers only. Instead of just taking you to different towns " +
        "like the regular cabs, we offer a much better service worthy of VIP class. It's a bit pricey, " +
        "but... for only 10,000 mesos, we'll take you safely to the \r\n#bAnt Tunnel#k.")
def ret = npc.askYesNo(beginner ? "We have a special 90% discount for beginners. " +
        "The Ant Tunnel is located deep inside in the dungeon that's placed at the center of the Victoria Island," +
        " where the 24 Hr Mobile Store is. Would you like to go there for #b1,000 mesos#k?" :
        "The regular fee applies for all non-beginners. The Ant Tunnel is located deep inside in the dungeon " +
        "that's placed at the center of the Victoria Island, where 24 Hr Mobile Store is. " +
        "Would you like to go there for #b10,000 mesos#k?")
cost /= beginner ? 10 : 1
if (ret == 1) {
    if (user.getMeso() < cost) {
        npc.say("It looks like you don't have enough mesos. Sorry but you won't be able to use this without it.")
    } else {
        user.gainMeso(-(int)cost)
        user.changeMap(105070001, 0)
    }
}
// empty no