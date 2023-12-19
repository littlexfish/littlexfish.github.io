package command.cmds.fs

import Application
import Translation
import command.Command
import fs.FS
import io.pipeOutText

class Edit : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		val openedFile = pArg.getStandalone()
		if(openedFile.isEmpty()) {
			Application.startApp("editor")
			return 0
		}
		var errorCount = 0
		val actOpened = mutableListOf<String>()
		for(o in openedFile) {
			if(!FS.hasFile(o, env["PWD"])) {
				tunnel.pipeOutText(Translation["command.edit.not_found", "path" to o]) { style.color = "red" }
				errorCount++
			}
			else {
				actOpened.add(o)
			}
		}
		if(errorCount >= openedFile.size) {
			return 1
		}
		Application.startApp("editor")
		for(o in actOpened) {
			val obs = FS.getAbsolutePath(FS.getFile(o, relativeFrom = env["PWD"]))
			Application.sendMessage("editor", "open", mapOf("file" to obs))
		}
		return 0
	}

}