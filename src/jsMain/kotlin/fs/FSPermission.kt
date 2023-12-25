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
	private lateinit var systemDir: FileSystemDirectoryHandle

	internal suspend fun init(opfsRoot: FileSystemDirectoryHandle) {
		ready = false
		systemDir = opfsRoot.getDirectoryHandle("system").await()
		val handle = systemDir.getFileHandle(FILENAME, json("create" to true)).await()
		val file = handle.getFile().await()
		val reader = FileReader()
		reader.onload = {
			val text = reader.result.unsafeCast<String>()
			val lines = text.split("\n")
			for(line in lines) {
				val parts = line.split(";")
				if(parts.size == 3) {
					val path = parts[0]
					val read = parts[1] == "r"
					val write = parts[2] == "w"
					allPermissions[path] = Permission(read, write)
				}
			}
			true.let { ready = it }
		}
		reader.readAsText(file)
	}

	internal suspend fun save() {
		console.log(allPermissions.keys.map { "$it${allPermissions[it]}" }.toTypedArray())
		val handle = systemDir.getFileHandle(FILENAME, json("create" to true)).await()
		val writer = handle.createWritable().await()
		val text = allPermissions.map { "${it.key};${if(it.value.read) "r" else "-"};${if(it.value.write) "w" else "-"}" }.joinToString("\n")
		writer.write(text)
		writer.close()
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