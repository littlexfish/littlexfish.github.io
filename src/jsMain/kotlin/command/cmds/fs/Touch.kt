package command.cmds.fs

import Translation
import command.Command
import fs.FS
import io.pipeOutText

class Touch : Command() {

	override suspend fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			tunnel.pipeOutText(Translation["command_arg.1"]) { style.color = "red" }
			return 1
		}
		val along = parseArgs(args).getStandalone()
		for(a in along) {
			FS.getFile(a, true, env["PWD"]!!)
		}
		return 0
	}

}