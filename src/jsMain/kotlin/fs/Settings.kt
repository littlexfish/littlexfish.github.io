package fs

import ext.FileSystemDirectoryHandle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlin.js.json

object Settings {

	private var ready = false
	private val allSettings = HashMap<String, String>()
	private const val FILENAME = ".config"
	private lateinit var systemDir: FileSystemDirectoryHandle

	internal suspend fun init(opfsRoot: FileSystemDirectoryHandle) {
		ready = false
		systemDir = opfsRoot.getDirectoryHandle("system").await()
		val handle = systemDir.getFileHandle(FILENAME, json("create" to true)).await()
		FS.readContentAsText(handle) {
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
			true.let { r -> ready = r }
		}
	}

	private suspend fun save() {
		val handle = systemDir.getFileHandle(FILENAME, json("create" to true)).await()
		val writer = handle.createWritable().await()
		val text = allSettings.map { "${it.key}=${it.value}" }.joinToString("\n")
		writer.write(text)
		writer.close()
	}

	fun getSettings(key: String): String {
		return allSettings[key] ?: ""
	}

	fun setSettings(key: String, sett: Any) {
		allSettings[key] = sett.toString()
		MainScope().launch {
			save()
		}
	}

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
			SettKeys.Editor.AUTO_SAVE to "false"

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
	}



}