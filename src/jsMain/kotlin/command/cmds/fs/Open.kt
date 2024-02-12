package command.cmds.fs

import Application
import Translation
import app.FileViewer
import command.Command
import command.CommandType
import fs.FS
import io.pipeOutErrorTextTr

class Open : Command(CommandType.FS) {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		val openedFile = pArg.getStandalone()
		if(openedFile.isEmpty()) {
			pipeNeedArgs(tunnel, 1)
			return 1
		}
		
		val type = if(pArg.has("h") || pArg.has("html")) {
			"html"
		}
		else if(pArg.has("t") || pArg.has("text")) {
			"text"
		}
		else {
			""
		}
		
		val open = openedFile.last()
		if(!FS.hasFile(open, env["PWD"])) {
			tunnel.pipeOutErrorTextTr("command.open.not_found", "file" to open)
			return 1
		}
		val id = Application.startApp(FileViewer())
		Application.sendMessage(id, "open", mapOf("file" to FS.getAbsolutePath(FS.getFile(open, relativeFrom = env["PWD"])), "type" to type))
		return 0
	}

	override fun getHelp(): String = Translation["command.open.help"]

}