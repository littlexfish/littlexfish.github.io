package command.cmds.global

import Application
import Translation
import command.Command
import io.pipeOutText

class Exit : Command() {

	override suspend fun execute(args: Array<String>): Int {
		Application.backWithTimeout(1000, {
			tunnel.pipeOutText(Translation["command.exit.bye"])
		}, {
			tunnel.pipeOutText(Translation["command.exit.back_error"]) {
				style.color = "red"
			}
		})
		return 0
	}

	override fun getHelp(): String = Translation["command.exit.help"]

}