package command.cmds.fs

import Application
import Translation
import app.FileViewer
import command.Command
import command.CommandType
import fs.FS
import io.pipeOutErrorTextTr
import io.pipeOutTextLn
import kotlinx.browser.window

class Open : Command(CommandType.FS) {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		val openedFile = pArg.getStandalone()
		if(openedFile.isEmpty()) {
			pipeNeedArgs(tunnel, 1)
			return 1
		}
		
		val open = openedFile.last()
		val type = if(pArg.has("h") || pArg.has("html")) {
			"html"
		}
		else if(pArg.has("t") || pArg.has("text")) {
			"text"
		}
		else if(pArg.has("u") || pArg.has("url")) {
			tunnel.pipeOutTextLn(Translation["command.open.new"])
			window.open(open, "_blank")
			return 0
		}
		else if(open.matches("https?://([\\w.]+)(:\\d{1,5})?(/.*)?".toRegex())) {
			"url"
		}
		else {
			""
		}
		
		if(type != "url" && !FS.hasFile(open, env["PWD"])) {
			tunnel.pipeOutErrorTextTr("command.open.not_found", "file" to open)
			return 1
		}
		val id = Application.startApp(FileViewer())
		if(type == "url") {
			Application.sendMessage(id, "url", mapOf("url" to open))
		}
		else {
			Application.sendMessage(id, "open", mapOf("file" to FS.getAbsolutePath(FS.getFile(open, relativeFrom = env["PWD"])), "type" to type))
		}
		return 0
	}

	override fun getHelp(): String = Translation["command.open.help"]

}