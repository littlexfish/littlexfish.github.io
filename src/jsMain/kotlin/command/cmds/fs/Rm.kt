package command.cmds.fs

import Translation
import command.Command
import command.getRmInfo
import ext.getEntries
import fs.FS
import io.pipeOutText
import kotlinx.coroutines.await
import kotlin.js.json

class Rm : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val (pArg, isRecursive, isSilent) = getRmInfo(args, tunnel) ?: return 1

		for(path in pArg.getStandalone()) {
			val suc = FS.remove(path, isRecursive, env["PWD"])
			if(suc == false) {
				if(!isSilent) tunnel.pipeOutText(Translation["command.rm.not_found", "path" to path]) { style.color = "red" }
				continue
			}
		}
		return 0
	}

}