package command.cmds.env

import Translation
import command.Command
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutText

class UnAlias : Command() {

	override suspend fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			pipeNeedArgs(tunnel, 1)
			return 1
		}
		val aliases = ArrayList(env["ALIASES"]!!.split(";"))
		for(a in args) {
			for(alias in aliases) {
				if(alias.startsWith("$a=")) {
					aliases.remove(alias)
					break
				}
			}
		}
		env.baseEnv?.set("ALIASES", aliases.joinToString(";"))
		return 0
	}

	override fun getHelp(): String = Translation["command.unalias.help"]

}