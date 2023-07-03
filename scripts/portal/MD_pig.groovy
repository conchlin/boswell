/*
    This is an auto-generated portal script for Boswell!
    
    Portal Script Name: MD_pig            
    Portal Name: MD00
    Portal Position: java.awt.Point[x=720,y=-175]
    Location Name: The Rain-Forest East of Henesys
    Location ID: 100020000
            
    @Author: Connor
    @Created: 2023-07-02 
    */

int pigDungeon = 100020100

if (user.getMapId() == 100020000) {
    if (user.getParty() != null) {
        if (user.getId() == user.getParty().getLeaderId()) {
            script.transferNewFieldInstance(pigDungeon)
        } else {
            user.message("Please have your party leader enter, or leave your party.")
        }
    } else {
        script.transferNewFieldInstance(pigDungeon)
    }
} else {
    // when trying to leave
    script.transferOutFieldInstance(user.getInstance())
}
    