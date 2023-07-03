/*
    This is an auto-generated portal script for Boswell!
    
    Portal Script Name: MD_protect            
    Portal Name: MD00
    Portal Position: java.awt.Point[x=-1082,y=1106]
    Location Name: Destroyed Dragon Nest
    Location ID: 240040520
            
    @Author: Connor
    @Created: 2023-07-03 
*/
    
int protectDungeon = 240040900

if (user.getMapId() == 240040520) {
    if (user.getParty() != null) {
        if (user.getId() == user.getParty().getLeaderId()) {
            script.transferNewFieldInstance(protectDungeon)
        } else {
            user.message("Please have your party leader enter, or leave your party.")
        }
    } else {
        script.transferNewFieldInstance(protectDungeon)
    }
} else {
    // when trying to leave
    script.transferOutFieldInstance(user.getInstance())
}
    