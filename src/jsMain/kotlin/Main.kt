import app.App
import app.Terminal
import command.Env
import fs.FS
import fs.SettKeys
import fs.Settings
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.*
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

	const val DEBUG = true
	private val appLayer = mutableListOf<Int>()
	private val openedApp = mutableMapOf<Int, App>()
	private var nextAppId = 0

	private fun cleanBody() {
		appElement.clear()
	}

	fun init() {
		document.head?.append { appStyle() }
		document.body?.let {
			it.append(appElement)
			it.style.background = Settings[SettKeys.Theme.BACKGROUND]
			it.style.color = Settings[SettKeys.Theme.FOREGROUND]
		}
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
		appElement.append {
			rootFrame {
				openedApp[id]?.buildGUI()?.invoke(this)
			}
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

	private fun TagConsumer<HTMLElement>.appStyle() {
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

	private fun TagConsumer<HTMLElement>.rootFrame(content: (DIV.() -> Unit) = {}) {
		div {
			id = "frame"
			content()
		}
		div {
			id = "setting-btn-outline"
			style = "background-color: ${Settings[SettKeys.Theme.BACKGROUND]}"
			div {
				id = "setting-btn"
				i {
					classes = setOf("fa", "fa-xl", "fa-cog")
				}
				onClickFunction = {
					document.getElementById("setting-panel-outline")?.classList?.add("open")
				}
			}
		}
		div {
			id = "setting-panel-outline"
			div {
				id = "setting-panel"
				settingPanel()
			}
			div {
				id = "setting-panel-close"
				i {
					classes = setOf("fa", "fa-regular", "fa-circle-xmark")
				}
				onClickFunction = {
					document.getElementById("setting-panel-outline")?.classList?.remove("open")
				}
			}
		}
	}

	private fun DIV.settingPanel() {
		val cate = listOf("theme", "editor", "locale")
		div {
			id = "setting-category-list"
			for(c in cate) {
				div {
					id = "setting-category-$c"
					classes = if(c == "theme") setOf("setting-category", "active") else setOf("setting-category")
					+Translation["settings.category.$c"]
					onClickFunction = {
						onSettingCategoryClick(c)
					}
				}
			}
		}
		div {
			id = "setting-content"
			div {
				id = "setting-content-theme"
				classes = setOf("setting-content", "active")
				subGroup(Translation["settings.theme_group.frame"])
				colorSelector(Translation["settings.theme.frame"], SettKeys.Theme.FRAME)
				colorSelector(Translation["settings.theme.frame_light"], SettKeys.Theme.FRAME_LIGHT)

				subGroup(Translation["settings.theme_group.color"])
				colorSelector(Translation["settings.theme.background"], SettKeys.Theme.BACKGROUND)
				colorSelector(Translation["settings.theme.foreground"], SettKeys.Theme.FOREGROUND)
				colorSelector(Translation["settings.theme.foreground_dark"], SettKeys.Theme.FOREGROUND_DARK)
				colorSelector(Translation["settings.theme.color_error"], SettKeys.Theme.COLOR_ERROR)
				colorSelector(Translation["settings.theme.color_cmd_input"], SettKeys.Theme.COLOR_CMD_INPUT)
				colorSelector(Translation["settings.theme.color_1"], SettKeys.Theme.COLOR_1)
				colorSelector(Translation["settings.theme.color_1_dark"], SettKeys.Theme.COLOR_1_DARK)
				colorSelector(Translation["settings.theme.color_2"], SettKeys.Theme.COLOR_2)
				colorSelector(Translation["settings.theme.color_2_dark"], SettKeys.Theme.COLOR_2_DARK)
				colorSelector(Translation["settings.theme.color_3"], SettKeys.Theme.COLOR_3)
				colorSelector(Translation["settings.theme.color_3_dark"], SettKeys.Theme.COLOR_3_DARK)

				subGroup(Translation["settings.theme_group.button"])
				colorSelector(Translation["settings.theme.button.color"], SettKeys.Theme.BUTTON_COLOR)
				colorSelector(Translation["settings.theme.button.hover"], SettKeys.Theme.BUTTON_COLOR_HOVER)
				colorSelector(Translation["settings.theme.button.active"], SettKeys.Theme.BUTTON_COLOR_ACTIVE)
			}
			div {
				id = "setting-content-editor"
				classes = setOf("setting-content")
				booleanSelector(Translation["settings.editor.ligatures"], SettKeys.Editor.LIGATURES)
				booleanSelector(Translation["settings.editor.auto_save"], SettKeys.Editor.AUTO_SAVE)
			}
			div {
				id = "setting-content-locale"
				classes = setOf("setting-content")
				optionSelector(Translation["settings.locale.language"], SettKeys.Locale.LANGUAGE, Translation.allLanguage)
			}
		}
	}

	private fun DIV.settingItemName(name: String) {
		classes = setOf("setting-item")
		span {
			+name
		}
	}

	private fun DIV.subGroup(name: String) {
		div {
			classes = setOf("setting-group")
			span {
				+name
			}
		}
	}

	private fun DIV.colorSelector(name: String, settingsId: String) {
		div {
			settingItemName(name)
			input {
				type = InputType.color
				value = Settings[settingsId]
				onChangeFunction = {
					Settings[settingsId] = it.target.asDynamic().value.unsafeCast<String>()
				}
			}
		}
	}

	private fun DIV.booleanSelector(name: String, settingsId: String) {
		div {
			settingItemName(name)
			input {
				type = InputType.checkBox
				checked = Settings.getAsBoolean(settingsId)
				onChangeFunction = {
					Settings[settingsId] = it.target.asDynamic().checked.unsafeCast<Boolean>()
				}
			}
		}
	}

	private fun DIV.optionSelector(name: String, settingsId: String, options: List<String>) {
		div {
			settingItemName(name)
			select {
				options.forEach {
					option {
						value = it
						if(Settings[settingsId] == it) {
							selected = true
						}
						+it
					}
				}
				onChangeFunction = {
					Settings[settingsId] = it.target.asDynamic().value.unsafeCast<String>()
				}
			}
		}
	}

	private fun onSettingCategoryClick(cate: String) {
		document.getElementById("setting-category-list")?.children?.asList()?.forEach {
			it.classList.remove("active")
		}
		document.getElementById("setting-category-$cate")?.classList?.add("active")

		document.getElementById("setting-content")?.children?.asList()?.forEach {
			it.classList.remove("active")
		}
		document.getElementById("setting-content-$cate")?.classList?.add("active")
	}

}


