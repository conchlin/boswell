package script

import client.MapleCharacter
import client.MapleQuestStatus
import network.packet.ScriptMan
import server.quest.MapleQuest
import java.lang.NullPointerException
import java.util.*

class ScriptFunc(
    private val sm: ScriptManager.Companion, private val objectId: Int, private val user: MapleCharacter
) {
    constructor(
        sm: ScriptManager.Companion,
        objectId: Int,
        user: MapleCharacter,
        templateID: Int
    ) : this(sm, objectId, user) {
        this.speaker = templateID
    }

    private var speaker = 0

    private fun makeMessagePacket(type: Int, mem: ArrayList<Any>) {
        if (speaker == 0) {
            speaker = user.map.getNPCByObjectId(objectId).id
        }
        if (sm.status.get() == ScriptManager.Finishing) {
            throw RuntimeException("Attempting to execute script after finish status.")
        }
        if (sm.scriptHist.isNotEmpty() && (type != ScriptMessageType.Say.type || sm.scriptHist.first().type != ScriptMessageType.Say.type)) {
            clearHistory()
        }
        val hist = ScriptHistory(type)
        hist.memory?.addAll(mem)

        when (type) {
            ScriptMessageType.Say.type -> {
                val next = mem[1] as Boolean
                hist.packet = ScriptMan.onSay(
                    speaker, mem[0].toString(), sm.posScriptHistory != 0, next
                )
            }

            ScriptMessageType.AskYesNo.type -> {
                hist.packet = ScriptMan.onAskYesNo(speaker, mem[0].toString())
            }

            ScriptMessageType.AskAvatar.type -> {
                hist.couponItemID = mem[1] as Int
                hist.packet = ScriptMan.onAskAvatar(
                    4, speaker, mem[0].toString(), mem[2] as IntArray
                )
            }

            ScriptMessageType.AskMenu.type -> {
                hist.packet = ScriptMan.onAskMenu(4, speaker, mem[0].toString())
            }

            ScriptMessageType.AskText.type -> {
                hist.packet = ScriptMan.onAskText(
                    speaker, mem[0].toString(), mem[1].toString(), mem[2] as Short, mem[3] as Short
                )
            }

            ScriptMessageType.AskNumber.type -> {
                hist.packet = ScriptMan.onAskNumber(
                    speaker, mem[0].toString(), mem[1] as Int, mem[2] as Int, mem[3] as Int
                )
            }

            ScriptMessageType.AskAccept.type -> {
                hist.packet = ScriptMan.onAskAccept(speaker, mem[0].toString())
            }

            else -> return
        }

        hist.speakerTemplateID = speaker
        sm.scriptHist.add(hist)
        sm.posScriptHistory = sm.scriptHist.size
    }

    /**
     * Broadcasts a regular NPC chat window with no next button
     */
    fun say(msg: String) {
        say(msg, false)
    }

    private fun say(msg: String, next: Boolean) {
        val memory: ArrayList<Any> = ArrayList()
        memory.add(msg)
        memory.add(next)
        makeMessagePacket(ScriptMessageType.Say.type, memory)
        sendMessageAnswer()
        memory.clear()
        sm.tryCapture()
    }

    /**
     * Broadcasts a regular NPC chat window with a next button
     */
    fun sayNext(msg: String) {
        say(msg, true)
    }

    /**
     * Broadcasts a NPC chat window with yes/no buttons
     */
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

    fun askAccept(msg: String): Any? {
        val memory: ArrayList<Any> = ArrayList()
        memory.add(msg)
        makeMessagePacket(ScriptMessageType.AskAccept.type, memory)
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

    /* -- start of Quest scripting functions -- */

    fun startQuest(questId: Int) {
        try {
            MapleQuest.getInstance(questId).start(user, speaker)
        } catch (npe: NullPointerException) {
            throw IllegalArgumentException("Error starting quest: $questId", npe)
        }
    }

    fun forceStartQuest(questId: Int) {
        try {
            MapleQuest.getInstance(questId).forceStart(user, speaker)
        } catch (npe: NullPointerException) {
            throw IllegalArgumentException("Error starting quest: $questId", npe)
        }
    }

    fun completeQuest(questId: Int) {
        try {
            MapleQuest.getInstance(questId).complete(user, speaker)
        } catch (npe: NullPointerException) {
            throw IllegalArgumentException("Error completing quest: $questId", npe)
        }
    }

    fun forceCompleteQuest(questId: Int) {
        try {
            MapleQuest.getInstance(questId).forceComplete(user, speaker)
        } catch (npe: NullPointerException) {
            throw IllegalArgumentException("Error completing quest: $questId", npe)
        }
    }

    fun hasStartedQuest(questId: Int): Boolean {
        val status = user.getQuest(MapleQuest.getInstance(questId)).status
        return try {
            status == MapleQuestStatus.Status.STARTED
        } catch (npe: NullPointerException) {
            throw IllegalArgumentException("Quest Error: $questId", npe)
        }
    }

    fun hasCompletedQuest(questId: Int): Boolean {
        val status = user.getQuest(MapleQuest.getInstance(questId)).status
        return try {
            status == MapleQuestStatus.Status.COMPLETED
        } catch (npe: NullPointerException) {
            throw IllegalArgumentException("Quest Error: $questId", npe)
        }
    }

    /* -- end of Quest scripting functions -- */
}