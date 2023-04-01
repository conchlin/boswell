package script.binding

import client.MapleClient
import server.quest.MapleQuest

class ScriptQuest(val q: MapleQuest, val c: MapleClient) {

    var quest = q
    var client = c

}