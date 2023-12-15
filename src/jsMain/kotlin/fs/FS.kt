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
		root = opfsRoot.getDirectoryHandle("terminal", json("create" to true)).await()
		FSMapper.init(root!!)
		home = getDirectory(HOME_DIRECTORY, true)
	}

	fun getDirectoryRoot(): FileSystemDirectoryHandle {
		return root ?: throw IllegalStateException("FS not initialized")
	}

	fun getHomeDirectory(): FileSystemDirectoryHandle {
		return home ?: throw IllegalStateException("FS not initialized")
	}

	suspend fun getDirectory(path: String, create: Boolean = false, relativeFrom: String? = null): FileSystemDirectoryHandle {
		val p = if(path.startsWith("/")) path else "${relativeFrom!!}/$path"
		return if(create) {
			val spl = FSMapper.splitPath(p)
			FSMapper.addDirectory(getDirectoryRoot(), spl.first, spl.second)
		}
		else {
			FSMapper.getDirectory(getDirectoryRoot(), p)
		}
	}

	suspend fun getFile(path: String, create: Boolean = false, relativeFrom: String? = null): FileSystemFileHandle {
		val p = if(path.startsWith("/")) path else "${relativeFrom!!}/$path"
		return if(create) {
			val spl = FSMapper.splitPath(p)
			console.log(spl)
			FSMapper.addFile(getDirectoryRoot(), spl.first, spl.second)
		}
		else {
			FSMapper.getFile(getDirectoryRoot(), p)
		}
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

	fun simplifyPath(path: String): String {
		val spl = FSMapper.splitPath(path)
		return "${spl.first}/${spl.second}"
	}

}
