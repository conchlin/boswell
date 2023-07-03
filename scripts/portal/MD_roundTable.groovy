/*
    This is an auto-generated portal script for Boswell!
    
    Portal Script Name: MD_roundTable            
    Portal Name: MD00
    Portal Position: java.awt.Point[x=765,y=120]
    Location Name: Battlefield of Fire and Water
    Location ID: 240020500
            
    @Author: Connor
    @Created: 2023-07-03 
*/
    
int roundTableDungeon = 240020512

if (user.getMapId() == 240020500) {
    if (user.getParty() != null) {
        if (user.getId() == user.getParty().getLeaderId()) {
            script.transferNewFieldInstance(roundTableDungeon)
            script.appendFieldClock(60)
        } else {
            user.message("Please have your party leader enter, or leave your party.")
        }
    } else {
        script.transferNewFieldInstance(roundTableDungeon)
    }
} else {
    // when trying to leave
    script.transferOutFieldInstance(user.getInstance())
}
    