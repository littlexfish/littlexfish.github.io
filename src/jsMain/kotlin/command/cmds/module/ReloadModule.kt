package command.cmds.module

import Translation
import command.Command
import command.CommandType
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutNewLine
import io.pipeOutText
import module.ModuleRegistry

class ReloadModule : Command(CommandType.COMMON) {
	override suspend fun execute(args: Array<String>): Int {
		ModuleRegistry.reloadAll()
		val loadedModule = ModuleRegistry.getLoadedModule()
		var isBreak = false
		// show module message
		if(Application.DEBUG) {
			tunnel.pipeOutText(Translation["command.reload_module.debug_on"]) {
				style.color = Settings[SettKeys.Theme.FOREGROUND_DARK]
			}
			isBreak = true
		}
		val noModule = CommandType.ALL_NO_DEBUG.filter { it !in loadedModule }
		if(noModule.isNotEmpty()) {
			if(isBreak) tunnel.pipeOutNewLine()
			tunnel.pipeOutErrorTextTr("command.reload_module.no_module", "mods" to noModule.joinToString(", "))
		}
		return 0
	}

	override fun getHelp(): String = Translation["command.reload_module.help"]
}