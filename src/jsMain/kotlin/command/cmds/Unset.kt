package command.cmds

import command.Command
import io.pipeOutText

class Unset : Command() {

	override fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			tunnel.pipeOutText("need at least one argument") { style.color = "red" }
			return 1
		}
		for(a in args) {
			env.baseEnv?.remove(a)
		}
		return 0
	}
}