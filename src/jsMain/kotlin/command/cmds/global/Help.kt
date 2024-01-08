package command.cmds.global

import Translation
import command.Command
import command.CommandType
import command.Commands
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutPre
import io.pipeOutText

class Help : Command(CommandType.COMMON) {

	override suspend fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			tunnel.pipeOutPre(Translation["command.help.text"])
		}
		else {
			val cmd = args[0]
			val command = Commands.getCommand(cmd)
			if(command.isEmpty()) {
				tunnel.pipeOutErrorTextTr("command.help.not_found", "cmd" to cmd)
			}
			else {
				if(command.isPresent()) tunnel.pipeOutPre(command.get()!!.getHelp() ?: Translation["command.help.no_page", "cmd" to cmd])
				else {
					val mods = Commands.findModuleExcept(cmd)
					tunnel.pipeOutErrorTextTr("command.help.no_module", "mods" to mods.joinToString(", "), "cmd" to cmd)
				}
			}
		}
		return 0
	}

	override fun getHelp(): String = Translation["command.help.help"]

}