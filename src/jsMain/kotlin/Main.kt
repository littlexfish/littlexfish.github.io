import app.App
import app.Editor
import app.HtmlViewer
import app.Terminal
import fs.FS
import kotlinx.browser.document
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.div
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.collections.Map
import kotlin.collections.mapOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set

private val appElement = document.create.div {}

/**
 * The main function
 */
suspend fun main() {
	document.body?.append(appElement)
	coroutineScope {
		launch {
			// start the translation
			try {
				Translation.init()
			}
			catch(e: Throwable) {
				console.error(e)
				showInitError("Failed to load translation file.")
				return@launch
			}
			// start file system
			FS.init()
			// init applications
			Application.init()
			// start terminal
			Application.startApp("terminal")
		}.invokeOnCompletion {
			if(it != null) { // on terminal start with error
				it.printStackTrace()
				showInitError("Failed to start terminal.")
			}
		}
	}
}

/**
 * Show error
 */
private fun showInitError(msg: String, title: String? = null) {
	appElement.clear()
	document.head?.title = "Error" + if(title != null) ": $title" else ""
	appElement.append {
		div {
			id = "error-frame"
			+msg
			br
			+"If this is a bug, please open an "
			a("https://github.com/littlexfish/littlexfish.github.io/issues/new") {
				+"issue"
			}
			+"."
		}
	}
}

/**
 * scroll element into view
 */
fun Element.scrollToView() {
	scrollIntoView(mapOf("behavior" to "smooth", "block" to "end", "inline" to "start"))
}

object Application {

	private val apps = mutableMapOf<String, App>()

	private var currentApp: App? = null

	private fun cleanBody() {
		appElement.clear()
	}

	fun init() {
		document.body?.append(appElement)
		buildApp(Terminal())
		buildApp(Editor())
		buildApp(HtmlViewer())
	}

	private fun buildApp(app: App) {
		apps[app.name] = app
	}

	fun startApp(name: String) {
		currentApp?.let {
			it.pause()
			it.suspend()
		}
		currentApp = apps[name] ?: throw IllegalArgumentException("App $name not found")
		currentApp?.let {
			cleanBody()
			it.init()
			// build app ui
			appElement.append {
				rootFrame {
					it.buildGUI()()
				}
			}
			it.restore()
			it.resume()
		}
	}

	fun sendMessage(to: String, msg: String, extra: Map<String, String>? = null) {
		currentApp?.receiveMessage(to, msg, extra)
	}

	private fun TagConsumer<HTMLElement>.rootFrame(content: (DIV.() -> Unit) = {}) {
		div {
			id = "frame"
			content()
		}
	}

}


