/*
    This is an auto-generated portal script for Boswell!
    
    Portal Script Name: MD_remember            
    Portal Name: MD00
    Portal Position: java.awt.Point[x=1040,y=1092]
    Location Name: The Dragon Nest Left Behind
    Location ID: 240040511
            
    @Author: Connor
    @Created: 2023-07-03 
*/
    
int rememberDungeon = 240040800

if (user.getMapId() == 240040511) {
    if (user.getParty() != null) {
        if (user.getId() == user.getParty().getLeaderId()) {
            script.transferNewFieldInstance(rememberDungeon)
            script.appendFieldClock(60)
        } else {
            user.message("Please have your party leader enter, or leave your party.")
        }
    } else {
        script.transferNewFieldInstance(rememberDungeon)
        script.appendFieldClock(60)
    }
} else {
    // when trying to leave
    script.transferOutFieldInstance(user.getInstance())
}
    