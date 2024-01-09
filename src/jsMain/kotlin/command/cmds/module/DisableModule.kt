package command.cmds.module

import Translation
import command.Command
import command.CommandType
import io.pipeOutText
import module.ModuleRegistry

class DisableModule : Command(CommandType.DEBUG) {

	override suspend fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			pipeNeedArgs(tunnel, 1)
			return 1
		}

		for(arg in args) {
			ModuleRegistry.disable(arg)
		}
		tunnel.pipeOutText(Translation["command.disable_module.current", "mods" to ModuleRegistry.getLoadedModule().joinToString(", ")])
		return 0
	}

	override fun getHelp(): String = Translation["command.disable_module.help"]

}