package script.template

import client.MapleClient
import server.MaplePortal
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class AutoGeneratedPortalScriptTemplate(private val portal: MaplePortal, private val client: MapleClient) {

    /**
     * this populates a very simple script for all portals with a script node
     * in their xml tree. While the generated script is not very helpful, it
     * is nice to provide the creation of a file with pre-populated info.
     */

    private val path = "./scripts/portal/"
    private val df = SimpleDateFormat("yyyy-MM-dd")
    private val date: String = df.format(Date())

    private val scriptHeader = """/*
    This is an auto-generated portal script for Boswell!
    
    Portal Script Name: ${portal.scriptName}            
    Portal Name: ${portal.name}
    Portal Position: ${portal.position}
    Location Name: ${client.player.map.mapName}
    Location ID: ${client.player.mapId}
            
    @Author: Auto Generated
    @Created: $date 
    */
    """
    private val scriptContents = """
    user.message("A script file has been generated for this portal. However, the portal does not currently work.")
    """

    fun create() {
        val fileName = "$path${portal.scriptName}.groovy"
        val newFile = File(fileName)

        if (newFile.createNewFile()) {
            val fw = FileWriter(newFile)
            fw.append(scriptHeader)
            fw.append(scriptContents)
            fw.close()
        }
    }

}