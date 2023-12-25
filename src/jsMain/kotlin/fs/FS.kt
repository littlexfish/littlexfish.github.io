package fs

import ext.*
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.files.FileReader
import kotlin.js.json

object FS {

	private const val HOME_DIRECTORY = "/home"
	private var fs: StorageManager = window.navigator.storage
	private var root: FileSystemDirectoryHandle? = null
	private var home: FileSystemDirectoryHandle? = null

	suspend fun init() {
		val opfsRoot = fs.getDirectory().await()
		ensureSystemDir(opfsRoot)
		FSPermission.init(opfsRoot)
		root = opfsRoot.getDirectoryHandle("terminal", json("create" to true)).await()
		FSMapper.init(root!!)
		home = getDirectory(HOME_DIRECTORY, true)
	}

	private suspend fun ensureSystemDir(opfsRoot: FileSystemDirectoryHandle) {
		opfsRoot.getDirectoryHandle("system", json("create" to true)).await()
	}

	private fun getDirectoryRoot(): FileSystemDirectoryHandle {
		return root ?: throw IllegalStateException("FS not initialized")
	}

	suspend fun getDirectory(path: String, create: Boolean = false, relativeFrom: String? = null): FileSystemDirectoryHandle {
		val p = toGlobalPath(path, relativeFrom)
		return if(create) {
			FSMapper.addDirectory(getDirectoryRoot(), p)
		}
		else {
			FSMapper.getDirectory(getDirectoryRoot(), p)
		}
	}

	suspend fun getFile(path: String, create: Boolean = false, createDir: Boolean = false, relativeFrom: String? = null, defaultPermission: Permission? = null): FileSystemFileHandle {
		val p = toGlobalPath(path, relativeFrom)
		return if(create) {
			val spl = FSMapper.splitPath(p)
			FSMapper.addFile(getDirectoryRoot(), spl.first, spl.second, createDir, defaultPermission)
		}
		else {
			FSMapper.getFile(getDirectoryRoot(), p)
		}
	}

	private fun getEntry(path: String, relativeFrom: String? = null): Boolean? {
		val p = toGlobalPath(path, relativeFrom)
		return FSMapper.getEntry(p)
	}

	fun hasDirectory(path: String, relativeFrom: String? = null): Boolean {
		return getEntry(path, relativeFrom) == true
	}

	fun hasFile(path: String, relativeFrom: String? = null): Boolean {
		return getEntry(path, relativeFrom) == false
	}

	suspend fun remove(path: String, isRecursive: Boolean, relativeFrom: String? = null): Boolean? {
		val p = toGlobalPath(path, relativeFrom)
		return FSMapper.removeEntry(getDirectoryRoot(), isRecursive, p)
	}

	suspend fun move(file: String, target: String, relativeFrom: String?) {
		val from = toGlobalPath(file, relativeFrom)
		val to = toGlobalPath(target, relativeFrom)
		val (f, targetFile) = getFileRoute(file, target, relativeFrom)
		readContent(f) {
			val writer = targetFile.createWritable().await()
			writer.write(it)
			writer.close()
			f.remove()
			FSPermission.setPermission(to, FSPermission.getPermission(from))
			FSPermission.removePermission(from)
		}
	}

	suspend fun copy(file: String, target: String, relativeFrom: String?) {
		val from = toGlobalPath(file, relativeFrom)
		val to = toGlobalPath(target, relativeFrom)
		val (f, targetFile) = getFileRoute(file, target, relativeFrom)
		readContent(f) {
			val writer = targetFile.createWritable().await()
			writer.write(it)
			writer.close()
			FSPermission.setPermission(to, FSPermission.getPermission(from))
		}
	}

	private suspend fun getFileRoute(file: String, target: String, relativeFrom: String? = null): Pair<FileSystemFileHandle, FileSystemFileHandle> {
		val f = getFile(file, create = false, createDir = false, relativeFrom = relativeFrom)
		val targetFile = getFile(target, create = true, createDir = true, relativeFrom = relativeFrom)
		return f to targetFile
	}

	private suspend fun readContent(file: FileSystemFileHandle, onRead: suspend (ByteArray) -> Unit) {
		val reader = FileReader()
		reader.onload = {
			val res = reader.result.unsafeCast<ByteArray>()
			MainScope().launch { onRead(res) }
		}
		reader.readAsArrayBuffer(file.getFile().await())
	}

	suspend fun readContentAsText(file: FileSystemFileHandle, onRead: suspend (String) -> Unit) {
		val reader = FileReader()
		reader.onload = {
			val res = reader.result.unsafeCast<String>()
			MainScope().launch { onRead(res) }
		}
		reader.readAsText(file.getFile().await())
	}

	fun getHomeDirectoryPath(): String {
		return HOME_DIRECTORY
	}

	fun replaceHomeDirectory(path: String): String {
		return simplifyPath(path).replaceFirst(HOME_DIRECTORY, "~")
	}

	suspend fun getAbsolutePath(handle: FileSystemHandle): String {
		return "/" + getDirectoryRoot().resolve(handle).await()!!.joinToString("/")
	}

	private fun simplifyPath(path: String): String {
		val spl = FSMapper.splitPath(path)
		return "${spl.first}/${spl.second}"
	}

	fun canRead(path: String, relativeFrom: String? = null): Boolean {
		val p = toGlobalPath(path, relativeFrom)
		if(!hasFile(p)) return false
		return FSPermission.getPermission(p).read
	}

	fun canWrite(path: String, relativeFrom: String? = null): Boolean {
		val p = toGlobalPath(path, relativeFrom)
		if(!hasFile(p)) return false
		return FSPermission.getPermission(p).write
	}

	fun getPermission(path: String, relativeFrom: String? = null): Permission {
		val p = toGlobalPath(path, relativeFrom)
		if(!hasFile(p)) return Permission.DEFAULT
		return FSPermission.getPermission(p)
	}

	fun setPermission(path: String, permission: Permission, relativeFrom: String? = null) {
		val p = toGlobalPath(path, relativeFrom)
		if(!hasFile(p)) return
		FSPermission.setPermission(p, permission)
	}

	private fun toGlobalPath(path: String, relativeFrom: String? = null): String {
		return if(path.startsWith("/")) path else "${relativeFrom!!}/$path"
	}

}
