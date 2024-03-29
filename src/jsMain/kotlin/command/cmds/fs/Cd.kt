package command.cmds.fs

import Translation
import command.Command
import command.CommandType
import fs.FS
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutText

class Cd : Command(CommandType.FS) {

	override suspend fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			env.baseEnv?.set("PWD", FS.getHomeDirectoryPath())
			return 0
		}
		val dir = args[0]
		val pwd = env["PWD"]!!
		if(!FS.hasDirectory(dir, pwd)) {
			tunnel.pipeOutErrorTextTr("command.cd.not_found", "path" to dir)
			return 1
		}
		val handle = FS.getDirectory(dir, false, pwd)
		val path = FS.getAbsolutePath(handle)
		env.baseEnv?.set("PWD", path)
		return 0
	}

	override fun getHelp(): String = Translation["command.cd.help"]

}