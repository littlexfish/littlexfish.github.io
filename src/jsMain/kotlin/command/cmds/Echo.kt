package command.cmds

import command.Command
import io.pipeOutText

class Echo : Command() {
	override fun execute(args: Array<String>): Int {
		tunnel.pipeOutText(args.joinToString(" "))
		return 0
	}
}