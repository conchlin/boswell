package script

import client.MapleClient
import network.packet.ScriptMan
import server.life.MapleNPC
import tools.FilePrinter
import tools.FilePrinter.NPC_UNCODED
import java.io.File
import javax.script.ScriptEngineManager


class ScriptManager {

    companion object {

        val engine = ScriptEngineManager().getEngineByExtension("groovy")!!
        private const val directory = "./scripts"

        fun say(npcId: Int, msg: String?): ByteArray? {
            return ScriptMan.onSay(npcId, msg, back = false, next = false)
        }

        /**
         * @param client
         * @param npc
         * @param name either the actual script name or the npcId
         */
        fun startScript(client: MapleClient, npc: MapleNPC, name: String) {
            val scriptName = String.format(
                "%s/%s%s", directory,
                name, ".groovy"
            )
            val scriptFile = File(scriptName)
            val exists: Boolean = scriptFile.exists()

            if (!exists) {
                // if script doesn't exist we send a very simple in-game error message
                client.announce(
                    say(npc.id, "The following script does not exist -> $name.groovy")
                )

                return FilePrinter.printError(NPC_UNCODED, "the following script does not exist -> $scriptName")
            }

            //engine.eval(scriptFile.reader())
        }
    }
}