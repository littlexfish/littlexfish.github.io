@file:JsFileName("style.js")
package style

import kotlinx.html.TagConsumer
import kotlinx.html.js.style
import kotlinx.html.unsafe
import org.w3c.dom.HTMLElement

object Style {
	fun style(): TagConsumer<HTMLElement>.() -> Unit = {
		style {
			unsafe {
				+rootStyle()
				+settingsStyle()
				+terminalStyle()
				+editorStyle()
				+htmlViewerStyle()
			}
		}
	}
}