package command.cmds

import command.Command
import command.Commands
import io.pipeOutPre
import io.pipeOutTag
import io.pipeOutText

class Help : Command() {

	override fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			tunnel.pipeOutPre("Hi, Welcome to the HELP page\n\nUsing `help [command]` to get help for a specific command\nUsing `lscmd` to list all available commands\n")
		}
		else {
			val cmd = args[0]
			val command = Commands.getCommand(cmd)
			if(command == null) {
				tunnel.pipeOutText("Command '$cmd' not found") {
					style.color = "red"
				}
			}
			else {
				tunnel.pipeOutPre(command.getHelp() ?: "No help page for command '$cmd'")
			}
		}
		return 0
	}

	override fun getHelp(): String = "Prints this help page\nUsing `help [command]` to get help for a specific command\nUsing `lscmd` to list all available commands"

}