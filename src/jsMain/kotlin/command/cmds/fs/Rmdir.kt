package command.cmds.fs

import Translation
import command.Command
import command.getRmInfo
import fs.FS
import io.pipeOutText
import kotlinx.coroutines.await
import kotlin.js.json

class Rmdir : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val (pArg, isRecursive, isSilent) = getRmInfo(args, tunnel) ?: return 1

		for(path in pArg.getStandalone()) {
			val hasDir = FS.hasDirectory(path, env["PWD"]!!)
			if(!hasDir) {
				if(!isSilent) tunnel.pipeOutText(Translation["command.rmdir.not_found", "path" to path]) { style.color = "red" }
				continue
			}
			val parent = FS.getDirectory(path.substringBeforeLast('/', ""), false, env["PWD"]!!)
			val name = path.substringAfterLast('/')
			parent.removeEntry(name, json("recursive" to isRecursive)).await()
		}

		return 0
	}

	override fun getHelp(): String = Translation["command.rmdir.help"]

}