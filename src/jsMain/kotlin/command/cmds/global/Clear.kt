package command.cmds.global

import Application
import Translation
import command.Command

class Clear : Command() {

	override suspend fun execute(args: Array<String>): Int {
		Application.sendMessage("terminal", "clear")
		return 0
	}

	override fun getHelp(): String = Translation["command.clear.help"]

}