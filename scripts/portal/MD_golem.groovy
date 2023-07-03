/*
    This is an auto-generated portal script for Boswell!
    
    Portal Script Name: MD_golem            
    Portal Name: MD00
    Portal Position: java.awt.Point[x=717,y=674]
    Location Name: Sleepy Dungeon IV
    Location ID: 105040304
            
    @Author: Connor
    @Created: 2023-07-03 
*/
    
int golemDungeon = 105040320

if (user.getMapId() == 105040304) {
    if (user.getParty() != null) {
        if (user.getId() == user.getParty().getLeaderId()) {
            script.transferNewFieldInstance(golemDungeon)
        } else {
            user.message("Please have your party leader enter, or leave your party.")
        }
    } else {
        script.transferNewFieldInstance(golemDungeon)
    }
} else {
    // when trying to leave
    script.transferOutFieldInstance(user.getInstance())
}
    