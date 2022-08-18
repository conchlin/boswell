function enter(pi) {
    if ((pi.hasItem(4032858))) { // temp key (Jenn's Brother quest)
        var brother = pi.getEventManager("JennsBrother");

        brother.setProperty("player", pi.getPlayer().getName());
        brother.startInstance(pi.getPlayer());

        pi.playPortalSound();
        pi.warp(921140100, 1);
        pi.gainItem(4032858, -1); // key "breaks" as described in quest dialogue
    } else if (pi.hasItem(4032832) && (pi.isQuestActive(3139))) { // has actual key but not complete spell breaker
        var spellBreaker = pi.getEventManager("1stSpellBreaker");

        spellBreaker.setProperty("player", pi.getPlayer().getName());
        spellBreaker.startInstance(pi.getPlayer());

        pi.playPortalSound();
        pi.warp(211060201, 1);
    }
        
    pi.message("The tower door is locked.");
    return false;
}