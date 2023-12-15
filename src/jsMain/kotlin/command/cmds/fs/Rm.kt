package command.cmds.fs

import Translation
import command.Command
import ext.getEntries
import fs.FS
import io.pipeOutText
import kotlinx.coroutines.await
import kotlin.js.json

class Rm : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		if(pArg.getStandalone().isEmpty()) {
			tunnel.pipeOutText(Translation["command_arg.1"]) { style.color = "red" }
			return 1
		}

		val isRecursive = pArg.has("r")
		val isSilent = pArg.has("s")

		for(path in pArg.getStandalone()) {
			val parent = FS.getDirectory(path.substringBeforeLast('/', ""), false, env["PWD"]!!)
			val name = path.substringAfterLast('/')
			val contains = parent.getEntries().containsKey(name)
			if(!contains) {
				if(!isSilent) tunnel.pipeOutText(Translation["command.rm.not_found", "path" to path]) { style.color = "red" }
				continue
			}
			parent.removeEntry(name, json("recursive" to isRecursive)).await()
		}

		return 0
	}

}