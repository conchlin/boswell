package script

import client.MapleCharacter
import network.packet.ScriptMan
import java.util.*

class ScriptFunc(
    private val sm: ScriptManager.Companion, private val objectId: Int, private val user: MapleCharacter
) {

    private fun makeMessagePacket(type: Int, mem: ArrayList<Any>) {
        makeMessagePacket(type, user.map.getNPCByObjectId(objectId).id, mem)
    }

    private fun makeMessagePacket(type: Int, templateId: Int, mem: ArrayList<Any>) {
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
                    templateId, mem[0].toString(), sm.posScriptHistory != 0, next
                )
            }

            ScriptMessageType.AskYesNo.type -> {
                hist.packet = ScriptMan.onAskYesNo(templateId, mem[0].toString())
            }

            ScriptMessageType.AskAvatar.type -> {
                hist.couponItemID = mem[1] as Int
                hist.packet = ScriptMan.onAskAvatar(
                    4, templateId, mem[0].toString(), mem[2] as IntArray
                )
            }

            ScriptMessageType.AskMenu.type -> {
                hist.packet = ScriptMan.onAskMenu(4, templateId, mem[0].toString())
            }

            ScriptMessageType.AskText.type -> {
                hist.packet = ScriptMan.onAskText(
                    templateId, mem[0].toString(), mem[1].toString(), mem[2] as Short, mem[3] as Short
                )
            }

            ScriptMessageType.AskNumber.type -> {
                hist.packet = ScriptMan.onAskNumber(
                    templateId, mem[0].toString(), mem[1] as Int, mem[2] as Int, mem[3] as Int
                )
            }

            else -> return
        }

        hist.speakerTemplateID = templateId
        sm.scriptHist.addLast(hist)
        sm.posScriptHistory = sm.scriptHist.size
    }

    /**
     * Broadcasts a regular NPC chat window with no next button
     * This should only be used for NPC scripts
     */
    fun say(msg: String) {
        say(msg, user.map.getNPCByObjectId(objectId).id, false)
    }

    /**
     * Broadcasts a regular NPC chat window with no next button
     * This should be used for non-NPC scripts
     *
     * @param templateId the npcId you want to broadcast
     */
    fun say(msg: String, templateId: Int) {
        say(msg, templateId, false)
    }

    private fun say(msg: String, templateId: Int, next: Boolean) {
        val memory: ArrayList<Any> = ArrayList()
        memory.add(msg)
        memory.add(next)
        makeMessagePacket(ScriptMessageType.Say.type, templateId, memory)
        sendMessageAnswer()
        memory.clear()
        sm.tryCapture()
    }

    /**
     * Broadcasts a regular NPC chat window with a next button
     * This should only be used for NPC scripts
     */
    fun sayNext(msg: String) {
        say(msg, user.map.getNPCByObjectId(objectId).id, true)
    }

    /**
     * Broadcasts a regular NPC chat window with a next button
     * This should be used for non-NPC scripts
     *
     * @param templateId the npcId you want to broadcast
     */
    fun sayNext(msg: String, templateId: Int) {
        say(msg, templateId, true)
    }

    /**
     * Broadcasts a NPC chat window with yes/no buttons
     * This should be used only be used for NPC scripts
     */
    fun askYesNo(msg: String) {
        askYesNo(msg, user.map.getNPCByObjectId(objectId).id)
    }

    /**
     * Broadcasts a NPC chat window with yes/no buttons
     * This should be used for non-NPC scripts
     *
     * @param templateId the npcId you want to broadcast
     */
    fun askYesNo(msg: String, templateId: Int): Any? {
        val memory: ArrayList<Any> = ArrayList()
        memory.add(msg)
        makeMessagePacket(ScriptMessageType.AskYesNo.type, templateId, memory)
        sendMessageAnswer()
        memory.clear()
        sm.tryCapture()

        return sm.value
    }

    fun askMenu(msg: String, templateId: Int): Any? {
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