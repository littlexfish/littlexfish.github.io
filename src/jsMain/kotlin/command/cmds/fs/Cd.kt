package command.cmds.fs

import command.Command
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
		if(!FS.hasDirectory(dir, pwd)) {
			tunnel.pipeOutText("No such directory: $dir") { style.color = "red" }
			return 1
		}
		val handle = FS.getDirectory(dir, false, pwd)
		val path = FS.getAbsolutePath(handle)
		env.baseEnv?.set("PWD", path)
		return 0
	}

}