package ext

import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.await
import org.w3c.dom.Navigator
import org.w3c.files.File
import kotlin.js.Promise

inline val Navigator.storage: StorageManager
	get() = asDynamic().storage.unsafeCast<StorageManager>()

@JsModule("StorageManager")
@JsNonModule
external object StorageManager {
	fun estimate(): Promise<StorageEstimate>
	fun getDirectory(): Promise<FileSystemDirectoryHandle>
	fun persist(): Promise<Boolean>
	fun persisted(): Promise<Boolean>
}

@JsModule("StorageEstimate")
@JsNonModule
external class StorageEstimate {
	val quota: Int?
	val usage: Int?
}

@JsModule("FileSystemHandle")
@JsNonModule
open external class FileSystemHandle {

	val kind: String
	val name: String
	fun isSameEntry(other: FileSystemHandle): Promise<Boolean>
	fun queryPermission(fileSystemHandlePermissionDescriptor: dynamic): Promise<PermissionStatus>
	fun remove(options: dynamic = definedExternally): Promise<Unit>
	fun requestPermission(fileSystemHandlePermissionDescriptor: dynamic): Promise<PermissionStatus>
}

val FileSystemHandle.isFile: Boolean
	get() = kind == "file"
val FileSystemHandle.isDirectory: Boolean
	get() = kind == "directory"

@JsNonModule
external class FileSystemFileHandle : FileSystemHandle {

	fun createWritable(option: dynamic = definedExternally): Promise<FileSystemWritableFileStream>
	fun getFile(): Promise<File>
//	fun createSyncAccessHandle(): Promise<FileSystemSyncAccessHandle> // may not support
	fun move(targetDir: String, newName: String): Promise<dynamic>

}

@JsNonModule
external class FileSystemDirectoryHandle : FileSystemHandle {

	fun getDirectoryHandle(name: String, options: dynamic = definedExternally): Promise<FileSystemDirectoryHandle>
	fun getFileHandle(name: String, options: dynamic = definedExternally): Promise<FileSystemFileHandle>
	fun removeEntry(name: String, options: dynamic = definedExternally): Promise<Unit>
	fun resolve(possibleDescendant: FileSystemHandle): Promise<Array<String>?>
	fun entries(): FileSystemDirectoryIterator
	fun keys(): FileSystemDirectoryIterator
	fun values(): FileSystemDirectoryIterator

}

suspend fun FileSystemDirectoryHandle.getEntries(): Map<String, FileSystemHandle> {
	val ret = mutableMapOf<String, FileSystemHandle>()
	val entries = entries()
	var next = entries.next().await()
	while(!next.done.unsafeCast<Boolean>()) {
		val key = next.value[0].unsafeCast<String>()
		val value = next.value[1].unsafeCast<FileSystemHandle>()
		ret[key] = value
		next = entries.next().asDeferred().await()
	}
	return ret
}

suspend fun FileSystemDirectoryHandle.getKeys(): List<String> {
	val ret = mutableListOf<String>()
	val keys = keys()
	while(true) {
		val next = keys.next().await()
		if(next.done.unsafeCast<Boolean>()) break
		val key = next.value.unsafeCast<String>()
		ret.add(key)
	}
	return ret
}

suspend fun FileSystemDirectoryHandle.getValues(): List<FileSystemHandle> {
	val ret = mutableListOf<FileSystemHandle>()
	val values = values()
	while(true) {
		val next = values.next().await()
		if(next.done.unsafeCast<Boolean>()) break
		val value = next.value.unsafeCast<FileSystemHandle>()
		ret.add(value)
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
	 *         - The byte position the current file cursor should move to if type "seek" is used. Can also be set if type is "write", in which case to write will start at the specified position.
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

@JsModule("FileSystemSyncAccessHandle")
@JsNonModule
external class FileSystemSyncAccessHandle {
	fun close()
	fun flush()
	fun getSize(): Int
	fun read(buffer: dynamic, option: dynamic = definedExternally): Int
	fun truncate(size: Int)
	fun write(buffer: dynamic, option: dynamic = definedExternally): Int
}
