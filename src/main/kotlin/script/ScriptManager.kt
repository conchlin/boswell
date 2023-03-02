package script

import client.MapleClient
import network.packet.ScriptMan
import script.binding.ScriptNpc
import server.life.MapleNPC
import tools.FilePrinter
import tools.FilePrinter.NPC_UNCODED
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.util.concurrent.Executors
import javax.script.ScriptEngineManager
import javax.script.ScriptException


class ScriptManager {

    companion object {

        val engine = ScriptEngineManager().getEngineByExtension("groovy")!!
        private const val directory = "./scripts"
        private var script: File? = null
        private val pool = Executors.newCachedThreadPool()

        /**
         * @param client
         * @param npc
         * @param name either the actual script name or the npcId
         */
        fun runScript(client: MapleClient, npc: MapleNPC, name: String) {
            val scriptName = String.format(
                "%s/%s%s", directory,
                name, ".groovy"
            )
            val scriptFile = File(scriptName)
            val exists: Boolean = scriptFile.exists()

            if (!exists) {
                // todo auto generate simple script
                // if script doesn't exist we send a very simple in-game error message
                client.announce(
                    ScriptMan.onSay(
                        npc.id,
                        "The following script does not exist -> $name.groovy",
                        back = false,
                        next = false
                    )
                )

                return FilePrinter.printError(NPC_UNCODED, "the following script does not exist -> $scriptName")
            } else {
                script = scriptFile

                try {
                    pool.submit {
                        engine.put("npc", ScriptNpc(npc, client))
                        engine.eval(script?.let { FileReader(it) })
                    }
                } catch (se: ScriptException) {
                    se.printStackTrace()
                } catch (fnfe: FileNotFoundException) {
                    fnfe.printStackTrace()
                }
            }

        }
    }
}

