package command.cmds.env

import Translation
import command.Command
import io.pipeOutText

class Alias : Command() {

	override suspend fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			tunnel.pipeOutText(Translation["command_arg.1"]) { style.color = "red" }
			return 1
		}
		for(a in args) {
			val split = a.split("=", limit = 2)
			if(split.size != 2) {
				tunnel.pipeOutText(Translation["command.alias.invalid_setter", "setter" to a]) { style.color = "red" }
				return 2
			}
			val name = split[0]
			val value = split[1]
			if(!"[a-zA-Z0-9_\\-?,.:~&^]+".toRegex().matches(name)) {
				tunnel.pipeOutText(Translation["command.alias.invalid_name", "name" to name]) { style.color = "red" }
				return 3
			}
			env.baseEnv?.set("ALIASES", env["ALIASES"]!! + ";$name=$value")
		}
		return 0
	}

}