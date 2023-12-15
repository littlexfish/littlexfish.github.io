package command.cmds.global

import Translation
import clearTerminal
import command.Command

class Clear : Command() {

	override suspend fun execute(args: Array<String>): Int {
		clearTerminal()
		return 0
	}

	override fun getHelp(): String = Translation["command.clear.help"]

}