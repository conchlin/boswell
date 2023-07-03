/*
    This is an auto-generated portal script for Boswell!
    
    Portal Script Name: MD_error            
    Portal Name: MD00
    Portal Position: java.awt.Point[x=245,y=-93]
    Location Name: Lab - Area C-1
    Location ID: 261020300
            
    @Author: Connor
    @Created: 2023-07-03 
*/
    
int errorDungeon = 261020301

if (user.getMapId() == 261020300) {
    if (user.getParty() != null) {
        if (user.getId() == user.getParty().getLeaderId()) {
            script.transferNewFieldInstance(errorDungeon)
        } else {
            user.message("Please have your party leader enter, or leave your party.")
        }
    } else {
        script.transferNewFieldInstance(errorDungeon)
    }
} else {
    // when trying to leave
    script.transferOutFieldInstance(user.getInstance())
}
    