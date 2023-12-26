package app

import Application
import fs.FS
import fs.HTMLProcessor
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.*
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLIFrameElement

class HtmlViewer : App("html_viewer") {

	private val containerElement: HTMLDivElement
		get() = document.getElementById("container") as HTMLDivElement

	override fun buildGUI(): DIV.() -> Unit = {
		div {
			id = "html-viewer"
			div {
				id = "title-bar"
				div {
					id = "title"
				}
				div {
					id = "close"
					i {
						classes = setOf("fa", "fa-regular", "fa-circle-xmark")
					}
					onClickFunction = {
						Application.back()
					}
				}
			}
			div {
				id = "container"
			}
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
			HTMLProcessor.openHtml(handle) { t, it ->
				containerElement.clear()
				val frame = document.createElement("iframe") as HTMLIFrameElement
				containerElement.append(frame)
				frame.srcdoc = it
				document.getElementById("title")?.textContent = t.ifEmpty { file.substringAfterLast("/") }
			}
		}
	}

}