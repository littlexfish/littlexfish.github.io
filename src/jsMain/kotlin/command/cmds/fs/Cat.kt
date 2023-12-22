package command.cmds.fs

import Translation
import command.Command
import fs.FS
import io.pipeOutNewLine
import io.pipeOutText
import io.pipeOutTextLn
import kotlinx.coroutines.await
import kotlinx.coroutines.withTimeout
import org.w3c.files.FileReader

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

	@Suppress("ControlFlowWithEmptyBody")
	private suspend fun showContent(path: List<String>, withPath: Boolean) {
		val contents = mutableMapOf<String, List<String>>()
		for(p in path) {
			val handler = FS.getFile(p, relativeFrom = env["PWD"])
			if(withPath) {
				val absPath = FS.getAbsolutePath(handler)
				tunnel.pipeOutTextLn("$absPath:") {
					style.color = "cornflowerblue"
				}
			}
			val reader = FileReader()
			reader.onload = {
				val text = reader.result.unsafeCast<String>()
				contents[p] = text.split("\n")
				null
			}
			reader.readAsText(handler.getFile().await())
		}
		for(p in path) {
			val content = contents[p]!!
			for(line in content) {
				tunnel.pipeOutTextLn(line)
			}
			tunnel.pipeOutNewLine()
		}
	}

}