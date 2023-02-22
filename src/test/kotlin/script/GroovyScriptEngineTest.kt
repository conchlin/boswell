package script

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import javax.script.ScriptEngineManager

class ScriptManagerTest {

    @Test
    fun testScriptEngine() {
        val engine = ScriptEngineManager().getEngineByExtension("groovy")!!
        val res1 = engine.eval("def x = 3")

        assertEquals(3, res1)
    }

    @Test
    fun testReadScript() {
        val engine = ScriptEngineManager().getEngineByExtension("groovy")!!
        val scriptFile = File("./scripts/test_script.groovy")
        val out = captureOut {
            engine.eval(scriptFile.reader())
        }.lines()

        assertEquals(listOf("hello from test_script"), out)
    }

    private fun captureOut(body: () -> Unit): String {
        val outStream = ByteArrayOutputStream()
        val prevOut = System.out
        System.setOut(PrintStream(outStream))
        try {
            body()
        } finally {
            System.out.flush()
            System.setOut(prevOut)
        }
        return outStream.toString().trim()
    }
}