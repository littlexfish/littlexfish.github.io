package command.cmds.fs

import Translation
import command.Command
import fs.FS
import io.pipeOutText

class Mkdir : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		if(pArg.getStandalone().isEmpty()) {
			tunnel.pipeOutText(Translation["command_arg.1"]) { style.color = "red" }
			return 1
		}

		for(a in pArg.getStandalone()) {
			FS.getDirectory(a, create = true, relativeFrom = env["PWD"]!!)
		}

		return 0
	}

	override fun getHelp(): String = Translation["command.mkdir.help"]

}