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
            if (im.getMapId() == 106020300) {
                var portal = im.getMap().getPortal("obstacle");
                if (portal != null && portal.getPosition().distance(im.getPlayer().getPosition()) < 210) {
                    if(!(im.isQuestStarted(100202) || im.isQuestCompleted(100202))) im.startQuest(100202);
                    im.removeAll(2430014);

                    im.message("You have used the Killer Mushroom Spore to open the way.");
                }
            }
            
            im.dispose();
        }
    }
}