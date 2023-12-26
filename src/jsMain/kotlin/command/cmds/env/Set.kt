package command.cmds.env

import Translation
import command.Command
import io.pipeOutText

class Set : Command() {

	override suspend fun execute(args: Array<String>): Int {
		if(args.size < 2) {
			tunnel.pipeOutText(Translation["command_arg.2"]) { style.color = "red" }
			return 1
		}
		val name = args[0]
		val value = args[1]
		if(!"[a-zA-Z0-9_]+".toRegex().matches(name)) {
			tunnel.pipeOutText(Translation["command.set.invalid_name", "name" to name]) { style.color = "red" }
			return 2
		}
		env.baseEnv?.set(name, value)
		return 0
	}

	override fun getHelp(): String = Translation["command.set.help"]

}