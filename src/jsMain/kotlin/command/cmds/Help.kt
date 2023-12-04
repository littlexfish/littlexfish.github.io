package command.cmds

import command.Command
import command.Commands

class Help : Command() {

	override fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			tunnel.outputToTerminal(getHelp())
		}
		else {
			val cmd = args[0]
			val command = Commands.getCommandWithoutInit(cmd)
			if(command == null) {
				tunnel.outputToTerminal("Command '$cmd' not found")
			}
			else {
				tunnel.outputToTerminal(command.getHelp() ?: "No help page for command '$cmd'")
			}
		}
		return 0
	}

	override fun getHelp(): String = "Prints this help page\nUsing `help [command]` to get help for a specific command\nUsing `lscmd` to list all available commands"

}