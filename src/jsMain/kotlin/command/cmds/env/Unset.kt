package command.cmds.env

import Translation
import command.Command
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutText

class Unset : Command() {

	override suspend fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			pipeNeedArgs(tunnel, 1)
			return 1
		}
		for(a in args) {
			env.baseEnv?.remove(a)
		}
		return 0
	}

	override fun getHelp(): String = Translation["command.unset.help"]

}