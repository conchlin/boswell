package script.binding

import client.MapleClient
import network.packet.ScriptMan
import server.life.MapleNPC
import java.lang.Exception

class ScriptNpc(n: MapleNPC, c: MapleClient) {

    val npc: MapleNPC = n
    val client: MapleClient = c

    fun say(msg: String?) {
        try {
            client.announce(ScriptMan.onSay(npc.id, msg, back = false, next = false))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}