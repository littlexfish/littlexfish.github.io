package command.cmds.global

import Application
import Translation
import command.Command
import app.Terminal
import command.CommandType
import io.pipeOutText
import kotlinx.browser.window

class Terminal : Command(CommandType.COMMON) {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		val stand = pArg.getStandalone()
		tunnel.pipeOutText(Translation["command.terminal.start", "id" to Application.nextAppId()])
		window.setTimeout({
			val id = Application.startApp(Terminal(env))
			Application.sendMessage(id, "run", mapOf("cmd" to stand.joinToString(" ; ")))
	    }, 500)
		return 0
	}

	override fun getHelp(): String = Translation["command.terminal.help"]

}