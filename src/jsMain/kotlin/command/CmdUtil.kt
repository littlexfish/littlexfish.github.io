package command

import Translation
import command.cmds.fs.Chmod
import fs.Permission
import io.TerminalTunnel
import io.pipeOutText

/**
 * Get the argument of the command.
 */
fun getRmInfo(args: Array<String>, tunnel: TerminalTunnel): Triple<Argument, Boolean, Boolean>? {
	val pArg = Command.parseArgs(args)
	if(pArg.getStandalone().isEmpty()) {
		tunnel.pipeOutText(Translation["command_arg.1"]) { style.color = "red" }
		return null
	}

	val isRecursive = pArg.has("r")
	val isSilent = pArg.has("s")
	return Triple(pArg, isRecursive, isSilent)
}

fun parsePermissionChange(value: String): PermissionChange? {
	if(!checkPermissionParam(value, true)) {
		return null
	}
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
	return perm
}

fun parsePermission(value: String): Permission? {
	if(!checkPermissionParam(value, false)) {
		return null
	}
	val read = value.contains("r")
	val write = value.contains("w")
	return if(read && write) Permission.ALL
	else if(read) Permission.READ_ONLY
	else if(write) Permission.WRITE_ONLY
	else Permission.DEFAULT
}

private fun checkPermissionParam(value: String, containChange: Boolean): Boolean {
	return ((if(containChange) "[#^]?" else "") + "(r|w|rw|wr)").toRegex().matches(value)
}

data class PermissionChange(var read: Boolean? = null, var write: Boolean? = null) {
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