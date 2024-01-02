package command.cmds.fs

import Translation
import command.Command
import command.getStandaloneWithSize
import fs.FS
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutText

class Mv : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		if(pArg.getStandalone().size < 2) {
			pipeNeedArgs(tunnel, 2)
			return 1
		}
		val files = getStandaloneWithSize(pArg, 2)
		val from = files[0]
		val to = files[1]
		FS.move(from, to, env["PWD"])
		return 0
	}

	override fun getHelp(): String = Translation["command.mv.help"]

}