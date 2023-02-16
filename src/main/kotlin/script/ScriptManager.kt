package script

import tools.FilePrinter
import tools.FilePrinter.NPC_UNCODED
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager


class ScriptManager {

    companion object {

        val engine = ScriptEngineManager().getEngineByExtension("groovy")!!
        private const val directory = "./scripts"

        fun startScript(name: String) {
            val scriptName = String.format(
                "%s/%s%s", directory,
                name, ".groovy"
            )
            val scriptFile = File(scriptName)
            val exists: Boolean = scriptFile.exists()

            if (!exists) {
                return FilePrinter.printError(NPC_UNCODED, "the following script does not exist -> $scriptName")
            }

            engine.eval(scriptFile.reader())
        }
    }
}