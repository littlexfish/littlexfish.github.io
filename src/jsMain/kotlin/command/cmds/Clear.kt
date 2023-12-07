package command.cmds

import Translation
import clearTerminal
import command.Command

class Clear : Command() {

	override fun execute(args: Array<String>): Int {
		clearTerminal()
		return 0
	}

	override fun getHelp(): String = Translation["command.clear.help"]

}