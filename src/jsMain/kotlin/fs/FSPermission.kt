package fs

import ext.FileSystemDirectoryHandle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.files.FileReader
import kotlin.js.json

object FSPermission {

	private var ready = false
	private val allPermissions = HashMap<String, Permission>()
	private const val FILENAME = "fp.conf"
	private var systemDir: FileSystemDirectoryHandle? = null

	internal suspend fun init(opfsRoot: FileSystemDirectoryHandle) {
		ready = false
		systemDir = opfsRoot.getDirectoryHandle("system").await()
		val handle = systemDir?.getFileHandle(FILENAME, json("create" to true))?.await()
		handle?.let { f ->
			FS.readContentAsText(f) {
				val lines = it.split("\n")
				for(line in lines) {
					val parts = line.split(";")
					if(parts.size == 3) {
						val path = parts[0]
						val read = parts[1] == "r"
						val write = parts[2] == "w"
						allPermissions[path] = Permission(read, write)
					}
				}
				true.let { r -> ready = r }
			}
		}
	}

	private suspend fun save() {
		if(systemDir == null) return
		val handle = systemDir?.getFileHandle(FILENAME, json("create" to true))?.await()
		val writer = handle?.createWritable()?.await()
		val text = allPermissions.map { "${it.key};${if(it.value.read) "r" else "-"};${if(it.value.write) "w" else "-"}" }.joinToString("\n")
		writer?.write(text)
		writer?.close()
	}

	internal fun getPermission(path: String): Permission {
		return allPermissions[path] ?: Permission.DEFAULT
	}

	internal fun setPermission(path: String, permission: Permission) {
		allPermissions[path] = permission
		MainScope().launch {
			save()
		}
	}

	internal fun removePermission(path: String) {
		allPermissions.remove(path)
		MainScope().launch {
			save()
		}
	}

}