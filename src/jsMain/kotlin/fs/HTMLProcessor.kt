package fs

import ext.FileSystemFileHandle
import kotlinx.coroutines.await
import kotlinx.dom.clear
import org.w3c.dom.*
import org.w3c.dom.parsing.DOMParser
import org.w3c.files.FileReader

object HTMLProcessor {

	suspend fun openHtml(handle: FileSystemFileHandle, onResult: (String, String) -> Unit) {
		val reader = FileReader()
		reader.onload = {
			val content = reader.result.unsafeCast<String>()
			val doc = parseHtml(content)
			onResult(doc.title, doc.documentElement?.outerHTML ?: "")
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
		val css = doc.createElement("link") as HTMLLinkElement
		css.rel = "stylesheet"
		css.href = "app.css"

		doc.head?.append(css)
		return doc
	}

}