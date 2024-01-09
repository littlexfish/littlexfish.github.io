import app.App
import app.Terminal
import command.Env
import command.CommandType
import fs.FS
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.*
import module.ModuleRegister
import module.ModuleRegistry
import org.w3c.dom.*
import style.*
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
			ModuleRegistry.register(CommonModule())
			// start file system
			FS.init()
			// init applications
			Application.init()
			// start terminal
			Application.startApp(Terminal(newRootEnv()))
		}.invokeOnCompletion {
			if(it != null) { // on terminal start with error
				it.printStackTrace()
				showInitError("Failed to start terminal.", "Failed", it)
			}
		}
	}
}

private fun newRootEnv(): Env {
	return Env().also {
		it["ALIASES"] = "cls=clear;?=help;shell=terminal;cmd=terminal"
		it["INPUT_BEGIN"] = it.defaultCommandInputPrefix()
		it["PWD"] = FS.getHomeDirectoryPath()
	}
}

/**
 * Show error
 */
private fun showInitError(msg: String, title: String? = null, thr: Throwable? = null) {
	appElement.clear()
	document.head?.let {
		it.title = "Error" + if(title != null) ": $title" else ""
		it.append {
			style {
				unsafe {
					+rootStyle()
				}
			}
		}
	}
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
			if(thr != null) {
				div {
					pre {
						+thr.stackTraceToString()
					}
				}
			}
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

	private lateinit var appFrameElement: HTMLElement
	const val DEBUG = true
	private val appLayer = mutableListOf<Int>()
	private val openedApp = mutableMapOf<Int, App>()
	private var nextAppId = 0

	private fun cleanBody() {
		appFrameElement.clear()
	}

	suspend fun init() {
		ModuleRegistry.register(DebugModule())
		document.head?.append { Style.style()() }
		document.body?.append(appElement)
		appElement.append {
			rootFrame()
		}
		appFrameElement = document.getElementById("frame") as HTMLElement
	}

	fun nextAppId(): Int {
		return nextAppId
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
		appFrameElement.append {
			app.buildGUI()()
		}
		app.restore()
		app.resume()
		nextAppId++
		return nextAppId - 1
	}

	private fun backToApp(id: Int): Boolean {
		val idx = appLayer.indexOf(id)
		if(idx == -1) return false
		if(idx == appLayer.lastIndex) return false
		val last = openedApp[appLayer.last()]
		last?.pause()
		last?.suspend()
		cleanBody()
		appLayer.subList(idx + 1, appLayer.size).forEach {
			openedApp.remove(it)
		}
		appLayer.removeAll(appLayer.subList(idx + 1, appLayer.size))
		appFrameElement.append {
			openedApp[id]?.buildGUI()?.invoke(this)
		}
		openedApp[id]?.restore()
		openedApp[id]?.resume()
		return true
	}

	fun backToApp(app: JsClass<out App>): Boolean {
		return backToApp(findApp(app) ?: return false)
	}

	private fun backToClose(): Boolean {
		return appLayer.size <= 1
	}

	fun back(): Boolean {
		if(backToClose()) {
			window.close()
			return true
		}
		return backToApp(appLayer[appLayer.lastIndex - 1])
	}

	fun backWithTimeout(timeout: Int, beforeBack: () -> Unit, afterBack: (Boolean) -> Unit) {
		beforeBack()
		window.setTimeout({
			afterBack(back())
		}, timeout)
	}

	fun findApp(app: JsClass<out App>): Int? {
		return appLayer.findLast { openedApp[it]!!::class.js == app }
	}

	fun sendMessage(to: Int?, msg: String, extra: Map<String, String>? = null) {
		if(to == null) return
		openedApp[to]?.receiveMessage(msg, extra)
	}

	fun getCurrentApp(): App? {
		return openedApp[appLayer.last()]
	}

}

class DebugModule : ModuleRegister(CommandType.DEBUG) {

	override suspend fun loadModule(): Boolean {
		return Application.DEBUG
	}

}

class CommonModule : ModuleRegister(CommandType.COMMON) {

	override suspend fun loadModule(): Boolean {
		return true
	}

}
