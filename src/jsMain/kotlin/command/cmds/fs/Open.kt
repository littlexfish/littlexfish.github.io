package command.cmds.fs

import Application
import Translation
import app.HtmlViewer
import command.Command
import fs.FS
import io.pipeOutText

class Open : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		val openedFile = pArg.getStandalone()
		if(openedFile.isEmpty()) {
			tunnel.pipeOutText(Translation["command_arg.1"]) { style.color = "red" }
			return 1
		}
		val open = openedFile.last()
		val id = Application.startApp(HtmlViewer())
		Application.sendMessage(id, "open", mapOf("file" to FS.getAbsolutePath(FS.getFile(open, relativeFrom = env["PWD"]))))
		return 0
	}

	override fun getHelp(): String = Translation["command.open.help"]

}