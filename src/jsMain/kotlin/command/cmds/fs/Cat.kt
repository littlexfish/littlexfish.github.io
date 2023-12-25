package command.cmds.fs

import Translation
import command.Command
import fs.FS
import io.pipeOutNewLine
import io.pipeOutPre
import io.pipeOutText
import io.pipeOutTextLn

class Cat : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		val path = mutableListOf<String>()
		for(p in pArg.getStandalone()) {
			if(FS.hasFile(p, relativeFrom = env["PWD"])) {
				path.add(p)
			}
			else {
				tunnel.pipeOutText(Translation["command.cat.not_found", "path" to p]) {
					style.color = "red"
				}
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
					style.color = "cornflowerblue"
				}
			}
			if(it.isEmpty()) {
				tunnel.pipeOutText("<empty>") {
					style.color = "gray"
				}
			}
			else {
				tunnel.pipeOutPre(it)
			}
			tunnel.pipeOutNewLine()
			showNextContent(path, index + 1, withPath)
		}
	}

}