import app.App
import app.Editor
import app.HtmlViewer
import app.Terminal
import command.Env
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
			Application.startApp(Terminal(newRootEnv()))
		}.invokeOnCompletion {
			if(it != null) { // on terminal start with error
				it.printStackTrace()
				showInitError("Failed to start terminal.")
			}
		}
	}
}

private fun newRootEnv(): Env {
	return Env().also {
		it["OS"] = "LF OS"
		it["ALIASES"] = "cls=clear;?=help"
		it["VERSION"] = "beta-0.2.0"
		it["ENGINE"] = "Kotlin/JS"
		it["ENGINE_VERSION"] = "1.9.21"
		it["ENGINE_LIB"] = "kotlinx-html-js:0.8.0;stdlib-js:1.9.21"
		it["INPUT_BEGIN"] = "> "
		it["CREATOR"] = "LF"
		it["INPUT_BEGIN"] = it.defaultCommandInputPrefix()
		it["PWD"] = FS.getHomeDirectoryPath()
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

	private val appLayer = mutableListOf<Int>()
	private val openedApp = mutableMapOf<Int, App>()
	private var nextAppId = 0

	private fun cleanBody() {
		appElement.clear()
	}

	fun init() {
		document.body?.append(appElement)
	}

	fun startApp(app: App): Int {
		if(appLayer.isNotEmpty()) {
			val last = openedApp[appLayer.last()]
			last?.pause()
			last?.suspend()
		}
		cleanBody()
		appLayer.add(nextAppId)
		openedApp[nextAppId] = app
		app.init()
		appElement.append {
			rootFrame {
				app.buildGUI()()
			}
		}
		app.restore()
		app.resume()
		nextAppId++
		return nextAppId - 1
	}

	private fun backToApp(id: Int) {
		val idx = appLayer.indexOf(id)
		if(idx == -1) return
		if(idx == appLayer.lastIndex) return
		val last = openedApp[appLayer.last()]
		last?.pause()
		last?.suspend()
		cleanBody()
		appLayer.subList(idx + 1, appLayer.size).forEach {
			openedApp.remove(it)
		}
		appLayer.removeAll(appLayer.subList(idx + 1, appLayer.size))
		appElement.append {
			rootFrame {
				openedApp[id]?.buildGUI()?.invoke(this)
			}
		}
		openedApp[id]?.restore()
		openedApp[id]?.resume()
	}

	fun backToApp(app: JsClass<out App>) {
		backToApp(findApp(app) ?: return)
	}

	fun back() {
		if(appLayer.size <= 1) return
		backToApp(appLayer[appLayer.lastIndex - 1])
	}

	fun findApp(app: JsClass<out App>): Int? {
		return appLayer.findLast { openedApp[it]!!::class.js == app }
	}

	fun sendMessage(to: Int?, msg: String, extra: Map<String, String>? = null) {
		if(to == null) return
		openedApp[to]?.receiveMessage(msg, extra)
	}

	private fun TagConsumer<HTMLElement>.rootFrame(content: (DIV.() -> Unit) = {}) {
		div {
			id = "frame"
			content()
		}
	}

}


