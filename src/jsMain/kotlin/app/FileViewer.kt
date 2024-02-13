package app

import Application
import Translation
import fs.FS
import fs.HTMLProcessor
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.*
import kotlinx.html.js.onClickFunction
import org.w3c.dom.*
import style.FileViewerStyle
import style.StyleRegister

class FileViewer : App("file_viewer") {

	private val containerElement: HTMLDivElement
		get() = document.getElementById("container") as HTMLDivElement

	override fun buildGUI(): TagConsumer<HTMLElement>.() -> Unit = {
		div {
			id = "html-viewer"
			div {
				id = "title-bar"
				div {
					id = "title"
					div {
						id = "title-content"
					}
					div {
						id = "open-type"
					}
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

	override fun getStyleRegister(): StyleRegister = FileViewerStyle

	override fun onInit() {}

	override fun onRestore() {}

	override fun onSuspend() {}

	override fun onReceiveMessage(msg: String, extra: Map<String, String>?) {
		if(extra == null) return
		if(msg == "url") {
			val url = extra["url"] ?: return
			containerElement.clear()
			val frame = document.createElement("iframe") as HTMLIFrameElement
			frame.onload = {
				val titleList = frame.contentDocument?.getElementsByTagName("title")
				val title = if(titleList?.asList()?.isNotEmpty() == true) titleList[0]!!.textContent ?: url else url
				document.getElementById("title-content")?.textContent = title//frame.contentDocument?.title ?: url
				undefined
			}
			frame.src = url
			containerElement.append(frame)
			document.getElementById("open-type")?.textContent = "(${Translation["file_viewer.mode.url"]})"
		}
		val file = extra["file"] ?: return
		val fileType = if(file.endsWith(".html") || file.endsWith(".htm")) "html" else "text"
		val forceType = if(extra["type"]?.isBlank() != true) extra["type"]!! else fileType
		MainScope().launch {
			val handle = FS.getFile(file)
			if(forceType == "html") {
				HTMLProcessor.openHtml(handle) { t, it ->
					containerElement.clear()
					val frame = document.createElement("iframe") as HTMLIFrameElement
					containerElement.append(frame)
					frame.srcdoc = it
					document.getElementById("title-content")?.textContent = t.ifEmpty { file.substringAfterLast("/") }
					document.getElementById("open-type")?.textContent = "(${Translation["file_viewer.mode.html"]})"
				}
			}
			else {
				FS.readContentAsText(handle) {
					containerElement.clear()
					val pre = document.createElement("pre") as HTMLPreElement
					containerElement.append(pre)
					pre.innerText = it
					document.getElementById("title-content")?.textContent = file.substringAfterLast("/")
					document.getElementById("open-type")?.textContent = "(${Translation["file_viewer.mode.text"]})"
				}
			}
		}
	}

}