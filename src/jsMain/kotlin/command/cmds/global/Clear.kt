package command.cmds.global

import Application
import Translation
import app.Terminal
import command.Command

class Clear : Command() {

	override suspend fun execute(args: Array<String>): Int {
		Application.sendMessage(Application.findApp(Terminal::class.js), "clear")
		return 0
	}

	override fun getHelp(): String = Translation["command.clear.help"]

}