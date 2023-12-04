package command.cmds

import command.Command
import command.Commands
import kotlinx.browser.document
import org.w3c.dom.HTMLSpanElement

class LsCmd : Command() {

	override fun execute(args: Array<String>): Int {
		val space = "&nbsp;&nbsp;&nbsp; "
		val cmd = Commands.availableCommands()
		val span = document.createElement("span") as HTMLSpanElement
		span.innerHTML = cmd.joinToString(space)
		tunnel.outputToTerminal(span)
		return 0
	}

}