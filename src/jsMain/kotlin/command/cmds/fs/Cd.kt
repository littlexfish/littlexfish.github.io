package command.cmds.fs

import command.Command
import ext.DOMExceptionName
import fs.FS
import io.pipeOutText

class Cd : Command() {

	override suspend fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			env.baseEnv?.set("PWD", FS.getHomeDirectoryPath())
			return 0
		}
		val dir = args[0]
		val pwd = env["PWD"]!!
		val handle = FS.getDirectory(dir, false, pwd)
		val path = FS.getAbsolutePath(handle)
		env.baseEnv?.set("PWD", path)
		return 0
	}

	override fun onExecuteError(it: Throwable) {
		if(it::class.js.name == "TypeError" || it.asDynamic().name == DOMExceptionName.NOT_FOUND_ERR) {
			tunnel.pipeOutText("cd: No such file or directory") { style.color = "red" }
		}
		else {
			super.onExecuteError(it)
			tunnel.pipeOutText("error on execute cd: $it") { style.color = "red" }
		}
	}

}