var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1)
            status++;
        else
            status--;

        if (status == 0) {
            if (qm.isQuestCompleted(3249) && qm.haveItem(4031992, 30)) {
                qm.gainItem(4031992, -30);
                qm.sendOk("Alright, it seems you brought the items I needed to raise this cute little Timer. Great Job!");
                qm.gainFame(11);
                qm.forceCompleteQuest();
            } else {
                qm.sendOk("Hmmm ... gotta concentrate!");
            }
            qm.dispose();
        }
    }
}