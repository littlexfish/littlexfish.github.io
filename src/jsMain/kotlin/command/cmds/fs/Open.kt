package command.cmds.fs

import Application
import Translation
import command.Command
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
		Application.startApp("html_viewer")
		Application.sendMessage("html_viewer", "open", mapOf("file" to open))
		return 0
	}

}