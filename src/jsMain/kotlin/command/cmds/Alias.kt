package command.cmds

import command.Command
import io.pipeOutText

class Alias : Command() {

	override fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			tunnel.pipeOutText("need at least one argument")
			return 1
		}
		for(a in args) {
			val split = a.split("=", limit = 2)
			if(split.size != 2) {
				tunnel.pipeOutText("invalid alias setter: $a")
				return 2
			}
			val name = split[0]
			val value = split[1]
			if(!"[a-zA-Z0-9_\\-?,.:~&^]+".toRegex().matches(name)) {
				tunnel.pipeOutText("invalid alias name: $name")
				return 3
			}
			env.baseEnv?.set("ALIASES", env["ALIASES"]!! + ";$name=$value")
		}
		return 0
	}

}