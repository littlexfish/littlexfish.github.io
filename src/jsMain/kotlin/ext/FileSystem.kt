package ext

import kotlinx.coroutines.await
import org.w3c.dom.Navigator
import org.w3c.files.File
import kotlin.js.Promise

inline val Navigator.storage: StorageManager
	get() = asDynamic().storage.unsafeCast<StorageManager>()

@JsModule("StorageManager")
@JsNonModule
external object StorageManager {
	fun getDirectory(): Promise<FileSystemDirectoryHandle>
}

@JsModule("FileSystemHandle")
@JsNonModule
open external class FileSystemHandle {

	val kind: String
	val name: String
	fun isSameEntry(other: FileSystemHandle): Promise<Boolean>
	fun queryPermission(fileSystemHandlePermissionDescriptor: dynamic): Promise<PermissionStatus>
	fun remove(options: dynamic): Promise<Unit>
	fun requestPermission(fileSystemHandlePermissionDescriptor: dynamic): Promise<PermissionStatus>
}

val FileSystemHandle.isFile: Boolean
	get() = kind == "file"
val FileSystemHandle.isDirectory: Boolean
	get() = kind == "directory"

@JsModule("FileSystemFileHandle")
@JsNonModule
external class FileSystemFileHandle : FileSystemHandle {

	fun createWritable(option: dynamic = definedExternally): Promise<FileSystemWritableFileStream>
	fun getFile(): Promise<File>

	fun move(targetDir: String, newName: String): Promise<dynamic>

}

@JsModule("FileSystemDirectoryHandle")
@JsNonModule
external class FileSystemDirectoryHandle : FileSystemHandle {

	fun getDirectoryHandle(name: String, options: dynamic = definedExternally): Promise<FileSystemDirectoryHandle>
	fun getFileHandle(name: String, options: dynamic = definedExternally): Promise<FileSystemFileHandle>
	fun removeEntry(name: String, options: dynamic = definedExternally): Promise<Unit>
	fun resolve(possibleDescendant: FileSystemHandle): Promise<Array<String>?>
	fun entries(): FileSystemDirectoryIterator // TODO
	fun keys(): FileSystemDirectoryIterator // TODO
	fun values(): FileSystemDirectoryIterator // TODO

}

suspend fun FileSystemDirectoryHandle.getEntries(): Map<String, FileSystemHandle> {
	val ret = mutableMapOf<String, FileSystemHandle>()
	val entries = entries()
	while(true) {
		val next = entries.next().await()
		if(next.done.unsafeCast<Boolean>()) break
		val key = next.value[0].unsafeCast<String>()
		val value = next.value[1].unsafeCast<FileSystemHandle>()
		ret[key] = value
	}
	return ret
}

@JsModule("FileSystemWritableFileStream")
@JsNonModule
external class FileSystemWritableFileStream : WritableStream {

	/**
	 * @param data Can be one of the following:
	 * - The file data to write, in the form of an `ArrayBuffer`, `TypedArray`, `DataView`, `Blob`, or string.
	 * - An object containing the following properties:
	 *     - `type`
	 *         - A string that is one of "write", "seek", or "truncate".
	 *     - data
	 *         - The file data to write. Can be an ArrayBuffer, TypedArray, DataView, Blob, or string. This property is required if type is set to "write".
	 *
	 *     - position
	 *         - The byte position the current file cursor should move to if type "seek" is used. Can also be set if type is "write", in which case the write will start at the specified position.
	 *
	 *     - size
	 *         - A number representing the number of bytes the stream should contain. This property is required if type is set to "truncate".
	 */
	fun write(data: dynamic): Promise<Unit>
	fun seek(position: Int): Promise<Unit>
	fun truncate(size: Int): Promise<Unit>

}

@JsModule("FileSystemDirectoryIterator")
@JsNonModule
external class FileSystemDirectoryIterator {
	fun next(): Promise<dynamic>
}
