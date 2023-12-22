package command.cmds.fs

import Translation
import command.Command
import command.parsePermissionChange
import fs.FS
import fs.Permission
import io.pipeOutText

class Chmod : Command() {

	/**
	 * Usage: chmod [-p <permission>] <file>...
	 * Arguments:
	 *     -p <permission>: The permission to set for the file(s)
	 *         #[(r|w)]: change read or write to true
	 *         ^[(r|w)]: change read or write to false
	 *         [(r|w)]: change read or write to true if specified, and false if not specified
	 */
	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args)
		if(pArg.getStandalone().size < 2) {
			tunnel.pipeOutText(Translation["command_arg.2"]) { style.color = "red" }
			return 1
		}

		val permissionChange = pArg.getStandalone().first()
		val perm = parsePermissionChange(permissionChange)
		if(perm == null) {
			tunnel.pipeOutText(Translation["command.chmod.perm_error", "perm" to permissionChange]) { style.color = "red" }
			return 1
		}

		for(a in pArg.getStandalone().subList(1, pArg.getStandalone().size)) {
			FS.setPermission(a, perm.toPermission(FS.getPermission(a, env["PWD"])), env["PWD"])
		}
		return 0
	}

}