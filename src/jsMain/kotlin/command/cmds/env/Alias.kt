package command.cmds.env

import Translation
import command.Command
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutText

class Alias : Command() {

	override suspend fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			pipeNeedArgs(tunnel, 1)
			return 1
		}
		for(a in args) {
			val split = a.split("=", limit = 2)
			if(split.size != 2) {
				tunnel.pipeOutErrorTextTr("command.alias.invalid_setter", "setter" to a)
				return 2
			}
			val name = split[0]
			val value = split[1]
			if(!"[a-zA-Z0-9_\\-?,.:~&^]+".toRegex().matches(name)) {
				tunnel.pipeOutErrorTextTr("command.alias.invalid_name", "name" to name)
				return 3
			}
			env.baseEnv?.set("ALIASES", env["ALIASES"]!! + ";$name=$value")
		}
		return 0
	}

	override fun getHelp(): String = Translation["command.alias.help"]

}