package command.cmds.debug

import Application
import command.Command
import ext.storage
import kotlinx.browser.window
import kotlinx.coroutines.await

class ResetSettings : Command() {

	override suspend fun execute(args: Array<String>): Int {
		if(!Application.DEBUG) return 1
		val file = ".config"
		val root = window.navigator.storage.getDirectory().await()
		val system = root.getDirectoryHandle("system").await()
		system.removeEntry(file)
		window.location.reload()
		return 0
	}

	override fun getHelp() = "Reset settings file and reload the page."

}