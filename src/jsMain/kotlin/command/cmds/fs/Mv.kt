package command.cmds.fs

import Translation
import command.Command
import command.getStandaloneWithSize
import fs.FS
import io.pipeOutText

class Mv : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		if(pArg.getStandalone().size < 2) {
			tunnel.pipeOutText(Translation["command_arg.2"]) { style.color = "red" }
			return 1
		}
		val files = getStandaloneWithSize(pArg, 2)
		val from = files[0]
		val to = files[1]
		FS.move(from, to, env["PWD"])
		return 0
	}

}