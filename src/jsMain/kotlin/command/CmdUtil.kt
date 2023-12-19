package command

import Translation
import io.TerminalTunnel
import io.pipeOutText

/**
 * Get the argument of the command.
 */
fun  getRmInfo(args: Array<String>, tunnel: TerminalTunnel): Triple<Argument, Boolean, Boolean>? {
	val pArg = Command.parseArgs(args)
	if(pArg.getStandalone().isEmpty()) {
		tunnel.pipeOutText(Translation["command_arg.1"]) { style.color = "red" }
		return null
	}

	val isRecursive = pArg.has("r")
	val isSilent = pArg.has("s")
	return Triple(pArg, isRecursive, isSilent)
}