package command.cmds.fs

import Translation
import command.Command
import command.CommandType
import command.getStandaloneWithSize
import fs.FS
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutText

class Cp : Command(CommandType.FS) {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		if(pArg.getStandalone().size < 2) {
			pipeNeedArgs(tunnel, 2)
			return 1
		}
		val files = getStandaloneWithSize(pArg, 2)
		val from = files[0]
		val to = files[1]
		FS.copy(from, to, env["PWD"])
		return 0
	}

	override fun getHelp(): String = Translation["command.cp.help"]

}