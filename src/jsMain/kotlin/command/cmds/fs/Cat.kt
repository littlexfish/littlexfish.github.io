package command.cmds.fs

import Translation
import command.Command
import command.CommandType
import fs.FS
import fs.SettKeys
import fs.Settings
import io.*

class Cat : Command(CommandType.FS) {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		val path = mutableListOf<String>()
		for(p in pArg.getStandalone()) {
			if(FS.hasFile(p, relativeFrom = env["PWD"])) {
				if(FS.canRead(p, relativeFrom = env["PWD"])) {
					path.add(p)
				}
				else {
					tunnel.pipeOutErrorTextTr("command.cat.no_permission", "path" to p)
				}
			}
			else {
				tunnel.pipeOutErrorTextTr("command.cat.not_found", "path" to p)
			}
		}
		if(path.isEmpty()) return 1
		showContent(path, path.size > 1)
		return 0
	}

	private suspend fun showContent(path: List<String>, withPath: Boolean) {
		val absPaths = path.map { FS.getAbsolutePath(FS.getFile(it, relativeFrom = env["PWD"])) }
		if(absPaths.isNotEmpty()) {
			showNextContent(absPaths, 0, withPath)
		}
	}

	private suspend fun showNextContent(path: List<String>, index: Int, withPath: Boolean) {
		if(index >= path.size) return
		val p = path[index]
		val handler = FS.getFile(p)
		FS.readContentAsText(handler) {
			if(withPath) {
				tunnel.pipeOutTextLn("$p:") {
					style.color = Settings[SettKeys.Theme.COLOR_1]
				}
			}
			if(it.isEmpty()) {
				tunnel.pipeOutText("<empty>") {
					style.color = Settings[SettKeys.Theme.FOREGROUND_DARK]
				}
			}
			else {
				tunnel.pipeOutPre(it)
			}
			tunnel.pipeOutNewLine()
			showNextContent(path, index + 1, withPath)
		}
	}

	override fun getHelp(): String = Translation["command.cat.help"]

}