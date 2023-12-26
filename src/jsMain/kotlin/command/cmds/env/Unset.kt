package command.cmds.env

import Translation
import command.Command
import io.pipeOutText

class Unset : Command() {

	override suspend fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			tunnel.pipeOutText(Translation["command_arg.1"]) { style.color = "red" }
			return 1
		}
		for(a in args) {
			env.baseEnv?.remove(a)
		}
		return 0
	}

	override fun getHelp(): String = Translation["command.unset.help"]

}