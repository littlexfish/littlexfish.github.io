package fs

import ext.FileSystemFileHandle
import kotlinx.coroutines.await
import org.w3c.dom.*
import org.w3c.dom.parsing.DOMParser
import org.w3c.files.FileReader

object HTMLProcessor {

	suspend fun openHtml(handle: FileSystemFileHandle, onResult: (Document) -> Unit) {
		val reader = FileReader()
		reader.onload = {
			val content = reader.result.unsafeCast<String>()
			val doc = parseHtml(content)
			onResult(doc)
			null
		}
		reader.readAsText(handle.getFile().await())
	}

	/**
	 * @return document without scripts
	 */
	private fun parseHtml(content: String): Document {
		val parser = DOMParser()
		val doc = parser.parseFromString(content, "text/html")
		doc.scripts.asList().forEach(Element::remove)
		return doc
	}

}