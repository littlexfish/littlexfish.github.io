package command.cmds.module

import Translation
import command.Command
import command.CommandType
import io.pipeOutText
import module.ModuleRegistry

class ListModule : Command(CommandType.DEBUG) {
	override suspend fun execute(args: Array<String>): Int {
		tunnel.pipeOutText(Translation["command.lsmod.current", "mods" to ModuleRegistry.getLoadedModule().joinToString(", ")])
		return 0
	}

	override fun getHelp(): String = Translation["command.lsmod.help"]
}