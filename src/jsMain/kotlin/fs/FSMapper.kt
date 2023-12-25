package fs

import ext.FileSystemDirectoryHandle
import ext.FileSystemFileHandle
import ext.getValues
import ext.isDirectory
import kotlinx.coroutines.await
import kotlin.js.json

internal object FSMapper {

	private val FSRoot = FSTreeNode("/", true)

	internal suspend fun init(root: FileSystemDirectoryHandle) {
		FSRoot.clear()
		buildTree(root, FSRoot)
	}

	private suspend fun buildTree(currentHandle: FileSystemDirectoryHandle, currentNode: FSTreeNode) {
		val values = currentHandle.getValues()
		for(value in values) {
			if(value.isDirectory) {
				val node = FSTreeNode(value.name, true)
				currentNode.add(node)
				buildTree(value as FileSystemDirectoryHandle, node)
			}
			else {
				currentNode.add(FSTreeNode(value.name, false))
			}
		}
	}

	internal suspend fun removeEntry(root: FileSystemDirectoryHandle, isRecursive: Boolean, path: String): Boolean? {
		return try {
			val isD = getEntry(path)
			if(isD == true) {
				getDirectory(root, path).remove(json("recursive" to isRecursive)).await()
			}
			else if(isD == false) {
				getFile(root, path).remove(json("recursive" to isRecursive)).await()
			}
			val spl = splitPath(path)
			val node = FSRoot.find(spl.first.split("/").iterator())
			node?.deleteChild(spl.second, isRecursive) == true
		}
		catch(e: Exception) {
			null
		}
	}

	internal suspend fun addFile(root: FileSystemDirectoryHandle, path: String, name: String, createDir: Boolean, permission: Permission? = null): FileSystemFileHandle {
		val parent = ensureDirectory(root, simplifyPath(path), createDir)
		if(!parent.second.hasChild(name)) {
			parent.second.add(FSTreeNode(name, false))
		}
		if(permission != null) FSPermission.setPermission("/$path/$name", permission)
		return parent.first.getFileHandle(name, json("create" to true)).await()
	}

	internal suspend fun addDirectory(root: FileSystemDirectoryHandle, path: String): FileSystemDirectoryHandle {
		return ensureDirectory(root, simplifyPath(path), true).first
	}

	private suspend fun ensureDirectory(root: FileSystemDirectoryHandle, path: String, createDir: Boolean): Pair<FileSystemDirectoryHandle, FSTreeNode> {
		val segments = simplifyPath(path).split("/").filter { it != "" }
		var curDir = root
		var curNode = FSRoot
		for(s in segments) {
			curDir = curDir.getDirectoryHandle(s, json("create" to createDir)).await()
			curNode = if(!curNode.hasChild(s)) {
				val n = FSTreeNode(s, true)
				curNode.add(n)
				n
			} else {
				curNode.getChild(s)!!
			}
		}
		return curDir to curNode
	}

	/**
	 * @param path the path to map, absolute path
	 */
	internal suspend fun getFile(root: FileSystemDirectoryHandle, path: String): FileSystemFileHandle {
		val segments = simplifyPath(path).split("/")
		val last = FSRoot.find(segments.iterator()) ?: throw NotFoundException()
		if(last.isDirectory) throw TypeNotMatchException(false)
		val parent = getDirectory(root, path.substringBeforeLast('/', ""))
		return parent.getFileHandle(path.substringAfterLast('/')).await()
	}

	internal suspend fun getDirectory(root: FileSystemDirectoryHandle, path: String): FileSystemDirectoryHandle {
		val segments = simplifyPath(path).split("/").filter { it != "" }
		if(segments.isEmpty()) return root
		val last = FSRoot.find(segments.iterator()) ?: throw NotFoundException()
		if(!last.isDirectory) throw TypeNotMatchException(true)
		var curDir = root
		for(s in segments) {
			curDir = curDir.getDirectoryHandle(s).await()
		}
		return curDir
	}

	private fun simplifyPath(path: String): String {
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

	internal fun splitPath(path: String): Pair<String, String> {
		val simple = simplifyPath(path)
		val left = simple.substringBeforeLast('/', "")
		val right = simple.substringAfterLast('/')
		return left to right
	}

	internal fun getEntry(path: String): Boolean? {
		val segments = simplifyPath(path).split("/")
		val last = FSRoot.find(segments.iterator()) ?: return null
		return last.isDirectory
	}

	data class FSTreeNode(val name: String, val isDirectory: Boolean) {
		private val children = mutableListOf<FSTreeNode>()
		fun getChild(n: String): FSTreeNode? {
			for(child in children) {
				if(child.name == n) return child
			}
			return null
		}
		fun hasChild(n: String): Boolean {
			return getChild(n) != null
		}
		fun find(path: Iterator<String>): FSTreeNode? {
			return pFind(path).second
		}
		private fun pFind(path: Iterator<String>): Pair<Boolean, FSTreeNode?> {
			if(!path.hasNext()) return true to this
			var next = path.next()
			if(next == "..") return false to null
			while(next == "." || next == "") {
				if(!path.hasNext()) return true to this
				next = path.next()
			}
			for(child in children) {
				if(child.name == next) {
					val f = child.pFind(path)
					if(f.first) return f
				}
			}
			return true to null
		}
		fun deleteChild(n: String, req: Boolean): Boolean {
			for(i in children.indices) {
				if(children[i].name == n) {
					if(children.isEmpty() || req && children[i].children.isNotEmpty()) {
						children[i].clear()
						children.removeAt(i)
						return true
					}
					return false
				}
			}
			return false
		}
		fun findPath(path: Iterator<String>): String? {
			return pFindPath(path).second
		}
		private fun pFindPath(path: Iterator<String>, begin: String = ""): Pair<Boolean, String?> {
			if(!path.hasNext()) return true to begin
			var next = path.next()
			if(next == "..") return false to null
			while(next == "." || next == "") {
				if(!path.hasNext()) return true to begin
				next = path.next()
			}
			for(child in children) {
				if(child.name == next) {
					val n = "$begin/${child.name}"
					val f = child.pFindPath(path, n)
					if(f.first) return f
				}
			}
			return true to null
		}
		internal fun add(child: FSTreeNode) {
			children.add(child)
		}
		internal fun clear() {
			children.forEach { it.clear() }
			children.clear()
		}
	}

}