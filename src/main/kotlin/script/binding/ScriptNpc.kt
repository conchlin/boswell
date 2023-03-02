package script.binding

import client.MapleClient
import network.packet.ScriptMan
import server.life.MapleNPC

class ScriptNpc(n: MapleNPC, c: MapleClient) {

    val npc: MapleNPC = n
    val client: MapleClient = c

    fun say(msg: String?) {
        client.announce(ScriptMan.onSay(npc.id, msg, back = false, next = false))
    }

}