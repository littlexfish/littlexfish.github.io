package command.cmds.fs

import Translation
import command.Command
import fs.FS
import fs.Permission
import io.pipeOutText

class Touch : Command() {

	/**
	 * Usage: touch [-p <permission>] <file>...
	 * Arguments:
	 *     -p <permission>: The permission to set for the file(s)
	 *         #[(r|w)]: change read or write to true
	 *         ^[(r|w)]: change read or write to false
	 *         [(r|w)]: change read or write to true if specified, and false if not specified
	 */
	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args, listOf("p"))
		if(pArg.getStandalone().isEmpty()) {
			tunnel.pipeOutText(Translation["command_arg.1"]) { style.color = "red" }
			return 1
		}

		val permissionChange = pArg.get("p")

		for(a in pArg.getStandalone()) {
			val perm = permissionChange?.let(::parsePermission)
			console.log(perm?.read ?: "null", perm?.write ?: "null")
			FS.getFile(a, create = true, createDir = false, relativeFrom = env["PWD"]!!, defaultPermission = perm)
		}
		return 0
	}

	private fun parsePermission(value: String): Permission {
		val perm = PermissionChange()
		if(value.startsWith("#")) {
			if(value.contains("r")) perm.read = true
			if(value.contains("w")) perm.write = true
		}
		else if(value.startsWith("^")) {
			if(value.contains("r")) perm.read = false
			if(value.contains("w")) perm.write = false
		}
		else {
			if(value.contains("r")) perm.read = true
			if(value.contains("w")) perm.write = true
			if(perm.read == null) perm.read = false
			if(perm.write == null) perm.write = false
		}
		// FIXME: check permission is set correctly
		return perm.toPermission()
	}

	private data class PermissionChange(var read: Boolean? = null, var write: Boolean? = null) {
		fun toPermission(default: Permission = Permission.DEFAULT): Permission {
			return toPermission(read ?: default.read, write ?: default.write)
		}
		private fun toPermission(read: Boolean, write: Boolean): Permission {
			return if(read && write) Permission.ALL
			else if(read) Permission.READ_ONLY
			else if(write) Permission.WRITE_ONLY
			else Permission.DEFAULT
		}
	}

}