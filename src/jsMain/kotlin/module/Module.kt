package module

import command.CommandType
import command.Commands

abstract class ModuleRegister(internal val bind: CommandType) {

	abstract suspend fun loadModule(): Boolean

}

object ModuleRegistry {

	private val modules = mutableListOf<ModuleRegister>()
	private val loadedModules = mutableSetOf<ModuleRegister>()

	suspend fun register(module: ModuleRegister) {
		modules.add(module)
		if(module.loadModule()) {
			loadedModules.add(module)
			Commands.registerType(module.bind)
		}
	}

	suspend fun reloadAll() {
		loadedModules.clear()
		Commands.clearType()
		modules.forEach {
			if(it.loadModule()) {
				loadedModules.add(it)
				Commands.registerType(it.bind)
			}
		}
	}

	fun getLoadedModule(): Set<CommandType> {
		return loadedModules.map { it.bind }.toSet()
	}

}