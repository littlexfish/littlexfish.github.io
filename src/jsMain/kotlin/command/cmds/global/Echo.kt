package command.cmds.global

import command.Command
import io.pipeOutText

class Echo : Command() {
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
}