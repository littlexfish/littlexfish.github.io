package command.cmds.global

import Translation
import command.Command
import command.Commands
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutPre
import io.pipeOutText

class Help : Command() {

	override suspend fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			tunnel.pipeOutPre(Translation["command.help.text"])
		}
		else {
			val cmd = args[0]
			val command = Commands.getCommand(cmd)
			if(command == null) {
				tunnel.pipeOutErrorTextTr("command.help.not_found", "cmd" to cmd)
			}
			else {
				tunnel.pipeOutPre(command.getHelp() ?: Translation["command.help.no_page", "cmd" to cmd])
			}
		}
		return 0
	}

	override fun getHelp(): String = Translation["command.help.help"]

}