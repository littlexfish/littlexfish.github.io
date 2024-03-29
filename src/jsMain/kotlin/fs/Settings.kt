package fs

import ext.FileSystemDirectoryHandle
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import style.StyleRegistry
import kotlin.js.json

object Settings {

	private var ready = false
	private val allSettings = HashMap<String, String>().apply { putAll(getDefaultSettings()) }
	private const val FILENAME = ".config"
	private var systemDir: FileSystemDirectoryHandle? = null

	internal suspend fun init(opfsRoot: FileSystemDirectoryHandle) {
		ready = false
		systemDir = opfsRoot.getDirectoryHandle("system").await()
		val handle = systemDir?.getFileHandle(FILENAME, json("create" to true))?.await()
		handle?.let { f ->
			FS.readContentAsText(f) {
				if(it.isBlank()) {
					allSettings.putAll(getDefaultSettings())
					save()
				}
				else {
					val lines = it.split("\n")
					for(line in lines) {
						val kv = line.split("=", limit = 2)
						if(kv.size == 2) {
							val key = kv[0]
							val value = kv[1]
							allSettings[key] = value
						}
					}
				}
				StyleRegistry.loadStyle("settings")
				true.let { r -> ready = r }
			}
		}
	}

	private suspend fun save() {
		if(systemDir == null) return
		val handle = systemDir?.getFileHandle(FILENAME, json("create" to true))?.await()
		val writer = handle?.createWritable()?.await()
		val text = allSettings.map { "${it.key}=${it.value}" }.joinToString("\n")
		writer?.write(text)
		writer?.close()
	}

	private fun getSettings(key: String): String? {
		return allSettings[key]
	}

	operator fun get(key: String) = getSettings(key) ?: ""

	fun getAsBoolean(key: String) = get(key).toBoolean()

	private fun setSettings(key: String, sett: Any) {
		allSettings[key] = sett.toString()
		MainScope().launch {
			save()
		}
	}

	operator fun set(key: String, sett: Any) = setSettings(key, sett)

	private fun getDefaultSettings(): Map<String, String> {
		return mapOf(
			SettKeys.Theme.FRAME to "#808080", // gray
			SettKeys.Theme.FRAME_LIGHT to "#ffffff", // white
			SettKeys.Theme.BACKGROUND to "#000000", // black
			SettKeys.Theme.FOREGROUND to "#ffffff", // white
			SettKeys.Theme.FOREGROUND_DARK to "#808080", // gray
			SettKeys.Theme.COLOR_ERROR to "#ff0000", // red
			SettKeys.Theme.COLOR_CMD_INPUT to "#4682b4", // steelblue
			SettKeys.Theme.COLOR_1 to "#6495ed", // cornflowerblue
			SettKeys.Theme.COLOR_1_DARK to "#4682b4", // steelblue
			SettKeys.Theme.COLOR_2 to "#7b68ee", // mediumslateblue
			SettKeys.Theme.COLOR_2_DARK to "#8a2be2", // blueviolet
			SettKeys.Theme.COLOR_3 to "#90ee90", // lightgreen
			SettKeys.Theme.COLOR_3_DARK to "#008000", // green
//			SettKeys.Theme.COLOR_4 to "#ffff00",
//			SettKeys.Theme.COLOR_4_DARK to "#800000",
//			SettKeys.Theme.COLOR_5 to "#00ffff",
//			SettKeys.Theme.COLOR_5_DARK to "#800000",
			SettKeys.Theme.BUTTON_COLOR to "#808080", // gray 128
			SettKeys.Theme.BUTTON_COLOR_HOVER to "#c8c8c8", // gray 200
			SettKeys.Theme.BUTTON_COLOR_ACTIVE to "#ffffff", // white

			SettKeys.Editor.LIGATURES to "false",
			SettKeys.Editor.AUTO_SAVE to "false",
			SettKeys.Editor.FILE_FRAME to "#ffffff", // white
			SettKeys.Editor.FILE_NOT_SAVE to "#ff7f50", // coral
			SettKeys.Editor.HIGHLIGHT_BACKGROUND to "#282b2e",
			SettKeys.Editor.HIGHLIGHT_FOREGROUND to "#a9b7c6",
			SettKeys.Editor.HIGHLIGHT_KEYWORD to "#cc7832",
			SettKeys.Editor.HIGHLIGHT_VARIABLE to "#629755",
			SettKeys.Editor.HIGHLIGHT_STRING to "#6A8759",
			SettKeys.Editor.HIGHLIGHT_NUMBER to "#6897BB",
			SettKeys.Editor.HIGHLIGHT_LITERAL to "#6897BB",
			SettKeys.Editor.HIGHLIGHT_SYMBOL to "#6897BB",
			SettKeys.Editor.HIGHLIGHT_BULLET to "#6897BB",
			SettKeys.Editor.HIGHLIGHT_COMMENT to "#808080",
			SettKeys.Editor.HIGHLIGHT_QUOTE to "#808080",
			SettKeys.Editor.HIGHLIGHT_ATTRIBUTE to "#6A8759",
			SettKeys.Editor.HIGHLIGHT_SECTION to "#ffc66d",
			SettKeys.Editor.HIGHLIGHT_TITLE to "#ffc66d",
			SettKeys.Editor.HIGHLIGHT_TYPE to "#ffc66d",
			SettKeys.Editor.HIGHLIGHT_NAME to "#e8bf6a",
			SettKeys.Editor.HIGHLIGHT_TEMPLATE_VARIABLE to "#629755",
			SettKeys.Editor.HIGHLIGHT_SELECTOR_TAG to "#cc7832",
			SettKeys.Editor.HIGHLIGHT_SELECTOR_ID to "#e8bf6a",
			SettKeys.Editor.HIGHLIGHT_SELECTOR_CLASS to "#e8bf6a",
			SettKeys.Editor.HIGHLIGHT_DELETION to "#cc7832",
			SettKeys.Editor.HIGHLIGHT_LINK to "#629755",
			SettKeys.Editor.HIGHLIGHT_META to "#bbb529",
			SettKeys.Editor.HIGHLIGHT_ADDITION to "#6A8759",

			SettKeys.Locale.LANGUAGE to if(window.navigator.language in Translation.allLanguage) window.navigator.language else Translation.DEFAULT_LANG
		)
	}

}

object SettKeys {

	object Theme {
		const val FRAME = "theme.frame"
		const val FRAME_LIGHT = "theme.frame_light"
		const val BACKGROUND = "theme.background"
		const val FOREGROUND = "theme.foreground"
		const val FOREGROUND_DARK = "theme.foreground_dark"
		const val COLOR_ERROR = "theme.color_error"
		const val COLOR_CMD_INPUT = "theme.color_cmd_input"
		const val COLOR_1 = "theme.color_1"
		const val COLOR_1_DARK = "theme.color_1_dark"
		const val COLOR_2 = "theme.color_2"
		const val COLOR_2_DARK = "theme.color_2_dark"
		const val COLOR_3 = "theme.color_3"
		const val COLOR_3_DARK = "theme.color_3_dark"
//		const val COLOR_4 = "theme.color_4"
//		const val COLOR_4_DARK = "theme.color_4_dark"
//		const val COLOR_5 = "theme.color_5"
//		const val COLOR_5_DARK = "theme.color_5_dark"

		const val BUTTON_COLOR = "theme.button.color"
		const val BUTTON_COLOR_HOVER = "theme.button.hover"
		const val BUTTON_COLOR_ACTIVE = "theme.button.active"
	}

	object Editor {
		const val LIGATURES = "editor.ligatures"
		const val AUTO_SAVE = "editor.auto_save"
		const val FILE_NOT_SAVE = "editor.file_not_save"
		const val FILE_FRAME = "editor.file_frame"

		const val HIGHLIGHT_BACKGROUND = "editor.highlight.background"
		const val HIGHLIGHT_FOREGROUND = "editor.highlight.foreground"
		const val HIGHLIGHT_KEYWORD = "editor.highlight.keyword"
		const val HIGHLIGHT_VARIABLE = "editor.highlight.variable"
		const val HIGHLIGHT_STRING = "editor.highlight.string"
		const val HIGHLIGHT_NUMBER = "editor.highlight.number"
		const val HIGHLIGHT_LITERAL = "editor.highlight.literal"
		const val HIGHLIGHT_SYMBOL = "editor.highlight.symbol"
		const val HIGHLIGHT_BULLET = "editor.highlight.bullet"
		const val HIGHLIGHT_COMMENT = "editor.highlight.comment"
		const val HIGHLIGHT_QUOTE = "editor.highlight.quote"
		const val HIGHLIGHT_ATTRIBUTE = "editor.highlight.attribute"
		const val HIGHLIGHT_SECTION = "editor.highlight.section"
		const val HIGHLIGHT_TITLE = "editor.highlight.title"
		const val HIGHLIGHT_TYPE = "editor.highlight.type"
		const val HIGHLIGHT_NAME = "editor.highlight.name"
		const val HIGHLIGHT_TEMPLATE_VARIABLE = "editor.highlight.template_variable"
		const val HIGHLIGHT_SELECTOR_TAG = "editor.highlight.selector_tag"
		const val HIGHLIGHT_SELECTOR_ID = "editor.highlight.selector_id"
		const val HIGHLIGHT_SELECTOR_CLASS = "editor.highlight.selector_class"
		const val HIGHLIGHT_DELETION = "editor.highlight.deletion"
		const val HIGHLIGHT_LINK = "editor.highlight.link"
		const val HIGHLIGHT_META = "editor.highlight.meta"
		const val HIGHLIGHT_ADDITION = "editor.highlight.addition"
	}

	object Locale {
		const val LANGUAGE = "locale.language"
	}

}