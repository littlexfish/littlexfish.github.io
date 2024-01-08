package command.cmds.global

import Translation
import command.Command
import command.CommandType
import io.pipeOutText

class Echo : Command(CommandType.COMMON) {
	override suspend fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			tunnel.pipeOutText("\n")
			return 0
		}
		if(args.contains("-i")) {
			while(tunnel.hasNextRead()) {
				tunnel.pipeOut(tunnel.readFromPipeIn()!!)
			}
		}
		else {
			tunnel.pipeOutText(args.joinToString(" "))
		}
		return 0
	}

	override fun getHelp(): String = Translation["command.echo.help"]

}