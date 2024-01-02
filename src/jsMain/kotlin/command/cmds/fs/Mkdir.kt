package command.cmds.fs

import Translation
import command.Command
import fs.FS
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutText

class Mkdir : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		if(pArg.getStandalone().isEmpty()) {
			pipeNeedArgs(tunnel, 1)
			return 1
		}

		for(a in pArg.getStandalone()) {
			FS.getDirectory(a, create = true, relativeFrom = env["PWD"]!!)
		}

		return 0
	}

	override fun getHelp(): String = Translation["command.mkdir.help"]

}