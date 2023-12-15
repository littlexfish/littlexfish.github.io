package command.cmds.fs

import command.Command
import ext.DOMException
import ext.DOMExceptionName
import fs.FS
import fs.simplifyPath
import io.pipeOutText

class Cd : Command() {

	override suspend fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			env.baseEnv?.set("PWD", FS.getHomeDirectoryPath())
			return 0
		}
		val dir = args[0]
		val pwd = env["PWD"]!!
		if(dir.startsWith("/")) {
			FS.getDirectory(dir, false)
			env.baseEnv?.set("PWD", simplifyPath(dir))
		}
		else {
			val p = simplifyPath("$pwd/$dir")
			FS.getDirectory(p, false)
			env.baseEnv?.set("PWD", p)
		}
		return 0
	}

	override fun onExecuteError(it: Throwable) {
		if(it::class.js.name == "TypeError" || it.asDynamic().name == DOMExceptionName.NOT_FOUND_ERR) {
			tunnel.pipeOutText("cd: No such file or directory") { style.color = "red" }
		}
		else {
			console.error(it)
			tunnel.pipeOutText("error on execute cd: $it") { style.color = "red" }
		}
	}

}