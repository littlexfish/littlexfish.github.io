package command.cmds.fs

import Translation
import command.Command
import command.CommandType
import command.getRmInfo
import fs.FS
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutText

class Rm : Command(CommandType.FS) {

	override suspend fun execute(args: Array<String>): Int {
		val (pArg, isRecursive, isSilent) = getRmInfo(args, tunnel) ?: return 1

		for(path in pArg.getStandalone()) {
			val suc = FS.remove(path, isRecursive, env["PWD"])
			if(suc == false) {
				if(isSilent) tunnel.pipeOutErrorTextTr("command.rm.not_found", "path" to path)
				continue
			}
		}
		return 0
	}

	override fun getHelp(): String = Translation["command.rm.help"]

}