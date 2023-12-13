package ext

import kotlin.js.Promise

@JsModule("WritableStream")
@JsNonModule
open external class WritableStream {

	val locked: Boolean

	fun abort(reason: String): Promise<String>
	fun close(): Promise<Unit>
	fun getWriter(): WritableStreamDefaultWriter
}

@JsModule("WritableStreamDefaultWriter")
@JsNonModule
external class WritableStreamDefaultWriter {

	val closed: Promise<dynamic>
	val desiredSize: Int
	val ready: Promise<dynamic>

	fun abort(reason: String = definedExternally): Promise<String>
	fun close(): Promise<Unit>
	fun releaseLock()
	fun write(chunk: ByteArray): Promise<Unit>
}