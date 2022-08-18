var status;
 
function start() {
        status = -1;
        action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        im.dispose();
    } else {
        if (mode == 0 && type > 0) {
            im.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;

        if(status == 0) {
            if (im.getMapId() == 106020400 && im.isQuestActive(2324)) {
                var player = im.getPlayer();
                
                var portal = im.getMap().getPortal("right00");
                if (portal != null && portal.getPosition().distance(player.getPosition()) < 210) {
                    player.gainExp(3300 * player.getExpRate());

                    im.forceCompleteQuest(2324);
                    im.removeAll(2430015);
                    im.playerMessage(5, "You have used the Thorn Remover to clear the path.");
                }
            }
            
            im.dispose();
        }
    }
}