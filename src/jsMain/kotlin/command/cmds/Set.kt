package command.cmds

import command.Command
import io.pipeOutText

class Set : Command() {

	override fun execute(args: Array<String>): Int {
		if(args.size < 2) {
			tunnel.pipeOutText("need at least two arguments") { style.color = "red" }
			return 1
		}
		val name = args[0]
		val value = args[1]
		if(!"[a-zA-Z0-9_]+".toRegex().matches(name)) {
			tunnel.pipeOutText("invalid variable name: $name") { style.color = "red" }
			return 2
		}
		env.baseEnv?.set(name, value)
		return 0
	}

}