package command.cmds.global

import Application
import Translation
import command.Command
import command.CommandType
import fs.SettKeys
import fs.Settings
import io.pipeOutText

class Exit : Command(CommandType.COMMON) {

	override suspend fun execute(args: Array<String>): Int {
		Application.backWithTimeout(1000, {
			tunnel.pipeOutText(Translation["command.exit.bye"])
		}, {
			tunnel.pipeOutText(Translation["command.exit.back_error"]) {
				style.color = Settings[SettKeys.Theme.COLOR_ERROR]
			}
		})
		return 0
	}

	override fun getHelp(): String = Translation["command.exit.help"]

}