package app

import fs.FS
import fs.HTMLProcessor
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.dom.clear
import kotlinx.html.*
import org.w3c.dom.HTMLDivElement

class HtmlViewer : App("html_viewer") {

	private val containerElement: HTMLDivElement
		get() = document.getElementById("container") as HTMLDivElement

	override fun buildGUI(): DIV.() -> Unit = {
		div {
			id = "container"
		}
	}

	override fun onInit() {}

	override fun onRestore() {}

	override fun onSuspend() {}

	override fun onReceiveMessage(msg: String, extra: Map<String, String>?) {
		if(extra == null) return
		val file = extra["file"] ?: return
		MainScope().launch {
			val handle = FS.getFile(file)
			HTMLProcessor.openHtml(handle) { doc ->
				containerElement.clear()
				containerElement.append(doc)
			}
		}
	}

}