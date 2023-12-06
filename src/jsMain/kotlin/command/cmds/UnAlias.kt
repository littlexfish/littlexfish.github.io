package command.cmds

import command.Command
import io.pipeOutText

class UnAlias : Command() {

	override fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			tunnel.pipeOutText("need at least one argument") { style.color = "red" }
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

}