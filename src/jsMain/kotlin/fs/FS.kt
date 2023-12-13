package fs

import ext.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlin.js.json

object FS {

	var fs: StorageManager = window.navigator.storage
	var root: FileSystemDirectoryHandle? = null

	suspend fun init() {
		val opfsRoot = fs.getDirectory().await()
		root = opfsRoot.getDirectoryHandle("terminal", json("create" to true)).await()
	}

	fun getDirectoryRoot(): FileSystemDirectoryHandle {
		return root ?: throw IllegalStateException("FS not initialized")
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
	return Pair(if(rootFirst) segments.subList(1, segments.size) else segments, rootFirst)
}
