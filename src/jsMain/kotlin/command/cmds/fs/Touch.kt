package command.cmds.fs

import Translation
import command.Command
import command.CommandType
import command.parsePermission
import fs.FS
import fs.Permission
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutText

class Touch : Command(CommandType.FS) {

	/**
	 * Usage: touch [-p <permission>] <file>...
	 * Arguments:
	 *     -p <permission>: The permission to set for the file(s)
	 *         [(r|w)]: change read or write to true if specified, and false if not specified
	 */
	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args, listOf("p"))
		if(pArg.getStandalone().isEmpty()) {
			pipeNeedArgs(tunnel, 1)
			return 1
		}

		val permissionChange = pArg.get("p")

		val perm = permissionChange?.let(::parsePermission) ?: Permission.DEFAULT

		for(a in pArg.getStandalone()) {
			FS.getFile(a, create = true, createDir = false, relativeFrom = env["PWD"]!!, defaultPermission = perm)
		}
		return 0
	}

	override fun getHelp(): String = Translation["command.touch.help"]

}