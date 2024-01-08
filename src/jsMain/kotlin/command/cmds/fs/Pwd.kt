package command.cmds.fs

import Translation
import command.Command
import command.CommandType
import io.pipeOutText

class Pwd : Command(CommandType.FS) {

	override suspend fun execute(args: Array<String>): Int {
		tunnel.pipeOutText(env["PWD"] ?: "")
		return 0
	}

	override fun getHelp(): String = Translation["command.pwd.help"]

}