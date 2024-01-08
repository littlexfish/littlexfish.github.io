package command.cmds.fs

import Application
import Translation
import app.HtmlViewer
import command.Command
import command.CommandType
import fs.FS
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutText

class Open : Command(CommandType.FS) {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		val openedFile = pArg.getStandalone()
		if(openedFile.isEmpty()) {
			pipeNeedArgs(tunnel, 1)
			return 1
		}
		val open = openedFile.last()
		val id = Application.startApp(HtmlViewer())
		Application.sendMessage(id, "open", mapOf("file" to FS.getAbsolutePath(FS.getFile(open, relativeFrom = env["PWD"]))))
		return 0
	}

	override fun getHelp(): String = Translation["command.open.help"]

}