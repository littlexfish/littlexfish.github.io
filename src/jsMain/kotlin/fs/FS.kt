package fs

import ext.*
import kotlinx.browser.window
import kotlinx.coroutines.await
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

	fun getDirectoryRoot(): FileSystemDirectoryHandle {
		return root ?: throw IllegalStateException("FS not initialized")
	}

	fun getHomeDirectory(): FileSystemDirectoryHandle {
		return home ?: throw IllegalStateException("FS not initialized")
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

	fun hasEntry(path: String, relativeFrom: String? = null): Boolean {
		return getEntry(path, relativeFrom) != null
	}

	fun hasDirectory(path: String, relativeFrom: String? = null): Boolean {
		return getEntry(path, relativeFrom) == true
	}

	fun hasFile(path: String, relativeFrom: String? = null): Boolean {
		return getEntry(path, relativeFrom) == false
	}

	suspend fun move(handle: FileSystemHandle, target: String, newName: String? = null) {
		// TODO: move file or directory
	}

	suspend fun copy(handle: FileSystemHandle, target: String, newName: String? = null) {
		// TODO: copy file or directory
	}

	fun getHomeDirectoryPath(): String {
		return HOME_DIRECTORY
	}

	fun isHomeDirectory(path: String): Boolean {
		return simplifyPath(path) == HOME_DIRECTORY
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

	suspend fun savePermission() {
		FSPermission.save()
	}

	private fun toGlobalPath(path: String, relativeFrom: String? = null): String {
		return if(path.startsWith("/")) path else "${relativeFrom!!}/$path"
	}

}
