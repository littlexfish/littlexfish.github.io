package fs

import ext.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlin.js.json

// TODO: add file system file mapping, to make FS more quickly find a file or directory
object FS {

	private const val HOME_DIRECTORY = "/home"
	private var fs: StorageManager = window.navigator.storage
	private var root: FileSystemDirectoryHandle? = null
	private var home: FileSystemDirectoryHandle? = null

	suspend fun init() {
		val opfsRoot = fs.getDirectory().await()
		root = opfsRoot.getDirectoryHandle("terminal", json("create" to true)).await()
		home = getDirectory(HOME_DIRECTORY, true)
	}

	fun getDirectoryRoot(): FileSystemDirectoryHandle {
		return root ?: throw IllegalStateException("FS not initialized")
	}

	fun getHomeDirectory(): FileSystemDirectoryHandle {
		return home ?: throw IllegalStateException("FS not initialized")
	}

	suspend fun getDirectory(path: String, create: Boolean = false, relative: FileSystemDirectoryHandle? = null): FileSystemDirectoryHandle {
		val (segments, rootFirst) = splitPath(path)
		if(!rootFirst && relative == null) throw IllegalArgumentException("relative cannot be null if path is relative")
		var dir = if(rootFirst) getDirectoryRoot() else relative
		for(segment in segments) {
			dir = dir!!.getDirectoryHandle(segment, json("create" to create)).await()
		}
		return dir!!
	}

	suspend fun getFile(path: String, create: Boolean = false, relative: FileSystemDirectoryHandle? = null): FileSystemFileHandle {
		val (segments, rootFirst) = splitPath(path)
		if(!rootFirst && relative == null) throw IllegalArgumentException("relative cannot be null if path is relative")
		var dir = if(rootFirst) getDirectoryRoot() else relative
		for(segment in segments.subList(0, segments.lastIndex)) {
			dir = dir!!.getDirectoryHandle(segment, json("create" to create)).await()
		}
		return dir!!.getFileHandle(segments.last(), json("create" to create)).await()
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

}

fun simplifyPath(path: String): String {
	val segments = path.split("/")
	val pathList = mutableListOf<String>()
	for(segment in segments) {
		if(segment == "" || segment == ".") continue
		if(segment == ".." && pathList.isNotEmpty()) {
			pathList.removeLast()
			continue
		}
		pathList.add(segment)
	}
	return (if(path.startsWith("/")) "/" else "") + pathList.joinToString("/")
}

fun splitPath(path: String): Pair<List<String>, Boolean> {
	val simplify = simplifyPath(path)
	val segments = simplify.split("/")
	val rootFirst = segments[0] == ""
	return Pair((if(rootFirst) segments.subList(1, segments.size) else segments).filter { it.isNotEmpty() }, rootFirst)
}
