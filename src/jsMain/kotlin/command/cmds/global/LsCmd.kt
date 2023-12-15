package command.cmds.global

import command.Command
import command.Commands
import io.pipeOutText

class LsCmd : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val cmd = Commands.availableCommands()
		for(c in cmd) {
			tunnel.pipeOutText(c) {
				style.paddingRight = "40px"
			}
		}
		return 0
	}

}