package script

import client.MapleCharacter
import network.packet.ScriptMan
import java.util.*

class ScriptFunc(
    private val sm: ScriptManager.Companion,
    private val objectId: Int,
    private val user: MapleCharacter
) {

    private fun makeMessagePacket(type: Int, mem: ArrayList<Any>) {
        if (sm.status.get() == ScriptManager.Finishing) {
            throw RuntimeException("Attempting to execute script after finish status.")
        }
        if (!sm.scriptHist.isEmpty() && (type != ScriptMessageType.Say.type || sm.scriptHist.first.type != ScriptMessageType.Say.type)) {
            clearHistory()
        }
        val hist = ScriptHistory(type)
        hist.memory?.addAll(mem)

        when (type) {
            ScriptMessageType.Say.type -> {
                val next = mem[1] as Boolean
                hist.packet = ScriptMan.onSay(
                    user.map.getNPCByObjectId(objectId).id,
                    mem[0].toString(),
                    sm.posScriptHistory != 0,
                    next
                )
            }

            ScriptMessageType.AskYesNo.type -> {
                hist.packet = ScriptMan.onAskYesNo(user.map.getNPCByObjectId(objectId).id, mem[0].toString())
            }

            ScriptMessageType.AskAvatar.type -> {
                hist.couponItemID = mem[1] as Int
                hist.packet = ScriptMan.onAskAvatar(
                    4,
                    user.map.getNPCByObjectId(objectId).id,
                    mem[0].toString(),
                    mem[2] as IntArray
                )
            }

            ScriptMessageType.AskMenu.type -> {
                hist.packet = ScriptMan.onAskMenu(4, user.map.getNPCByObjectId(objectId).id, mem[0].toString())
            }

            ScriptMessageType.AskText.type -> {
                hist.packet =
                    ScriptMan.onAskText(
                        user.map.getNPCByObjectId(objectId).id,
                        mem[0].toString(),
                        mem[1].toString(),
                        mem[2] as Short,
                        mem[3] as Short
                    )
            }

            ScriptMessageType.AskNumber.type -> {
                hist.packet =
                    ScriptMan.onAskNumber(
                        user.map.getNPCByObjectId(objectId).id,
                        mem[0].toString(),
                        mem[1] as Int,
                        mem[2] as Int,
                        mem[3] as Int
                    )
            }

            else -> return
        }

        hist.speakerTemplateID = user.map.getNPCByObjectId(objectId).id
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
        sendMessageAnswer()
        memory.clear()
        sm.tryCapture()
    }

    fun sayNext(msg: String) {
        say(msg, true)
    }

    fun askYesNo(msg: String): Any? {
        val memory: ArrayList<Any> = ArrayList()
        memory.add(msg)
        makeMessagePacket(ScriptMessageType.AskYesNo.type, memory)
        sendMessageAnswer()
        memory.clear()
        sm.tryCapture()

        return sm.value
    }

    fun askMenu(msg: String): Any? {
        val memory: ArrayList<Any> = ArrayList()
        memory.add(msg)
        makeMessagePacket(ScriptMessageType.AskMenu.type, memory)
        sendMessageAnswer()
        memory.clear()
        sm.tryCapture()

        return sm.value
    }

    fun askAvatar(msg: String, vararg candidate: Int): Any? {
        return askAvatar(msg, -1, candidate)
    }

    fun askAvatar(msg: String, couponItemID: Int, candidate: IntArray): Any? {
        val memory: ArrayList<Any> = ArrayList()
        memory.add(msg)
        memory.add(couponItemID)
        memory.add(candidate)
        makeMessagePacket(ScriptMessageType.AskAvatar.type, memory)
        sendMessageAnswer()
        memory.clear()
        sm.tryCapture()

        return sm.value
    }

    fun askNumber(msg: String, def: Int, min: Int, max: Int): Any? {
        val memory: ArrayList<Any> = ArrayList()
        memory.add(msg)
        memory.add(def)
        memory.add(min)
        memory.add(max)
        makeMessagePacket(ScriptMessageType.AskNumber.type, memory)
        sendMessageAnswer()
        memory.clear()
        sm.tryCapture()

        return sm.value
    }

    fun askText(msg: String): Any? {
        return askText(msg, "", 0, 0)
    }

    fun askText(msg: String, msgDefault: String, lengthMin: Int, lengthMax: Int): Any? {
        val memory: ArrayList<Any> = ArrayList()
        memory.add(msg)
        memory.add(msgDefault)
        memory.add(lengthMin)
        memory.add(lengthMax)
        makeMessagePacket(ScriptMessageType.AskText.type, memory)
        sendMessageAnswer()
        memory.clear()
        sm.tryCapture()

        return sm.value
    }

    private fun sendMessageAnswer() {
        user.announce(sm.scriptHist[sm.posScriptHistory - 1].packet)
        sm.status.set(ScriptManager.Message)
    }

    private fun clearHistory() {
        sm.scriptHist.clear()
        sm.posScriptHistory = 0
    }
}