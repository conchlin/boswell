/*
    This is an auto-generated portal script for Boswell!
    
    Portal Script Name: MD_rabbit            
    Portal Name: MD00
    Portal Position: java.awt.Point[x=-233,y=-1572]
    Location Name: Eos Tower 76th ~ 90th Floor
    Location ID: 221023400
            
    @Author: Connor
    @Created: 2023-07-03 
*/
    
int rabbitDungeon = 221023401

if (user.getMapId() == 221023400) {
    if (user.getParty() != null) {
        if (user.getId() == user.getParty().getLeaderId()) {
            script.transferNewFieldInstance(rabbitDungeon)
            script.appendFieldClock(60)
        } else {
            user.message("Please have your party leader enter, or leave your party.")
        }
    } else {
        script.transferNewFieldInstance(rabbitDungeon)
        script.appendFieldClock(60)
    }
} else {
    // when trying to leave
    script.transferOutFieldInstance(user.getInstance())
}
    