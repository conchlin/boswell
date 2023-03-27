package script.binding

import client.MapleClient
import network.packet.ScriptMan
import script.ScriptHistory
import script.ScriptManager
import script.ScriptMessageType
import server.life.MapleNPC


class ScriptNpc(s: ScriptManager.Companion, n: MapleNPC, c: MapleClient) {

    val npc: MapleNPC = n
    val client: MapleClient = c
    val sm: ScriptManager.Companion = s
    private val continuation: Object = Object()


    private fun makeMessagePacket(type: Int, mem: ArrayList<Any>) {
        if (!sm.scriptHist.isEmpty() && (type != ScriptMessageType.Say.type || sm.scriptHist.first.type != ScriptMessageType.Say.type)) {
            clearHistory()
        }
        val hist = ScriptHistory(type)
        hist.memory?.addAll(mem)

        when (type) {
            ScriptMessageType.Say.type -> {
                val next = mem[1] as Boolean
                hist.packet = ScriptMan.onSay(npc.id, mem[0].toString(), sm.posScriptHistory != 0, next)
            }
        }

        hist.speakerTemplateID = npc.id
        sm.scriptHist.addLast(hist)
        sm.posScriptHistory = sm.scriptHist.size
    }

    fun say(msg: String) {
        say(msg, false)
    }

    fun say(msg: String, next: Boolean) {
        val memory: ArrayList<Any> = ArrayList()
        memory.add(msg)
        memory.add(next)
        makeMessagePacket(ScriptMessageType.Say.type, memory)
        client.announce(sm.scriptHist[sm.posScriptHistory - 1].packet)
        memory.clear()
        tryCapture()
    }

    fun sayNext(msg: String) {
        say(msg, true)
    }

    private fun clearHistory() {
        sm.scriptHist.clear()
        sm.posScriptHistory = 0
    }

    private fun tryCapture() {
        synchronized(continuation) {
            try {
                continuation.wait()
            } catch (ex: InterruptedException) {
                continuation.notifyAll()
            }
        }
    }
}