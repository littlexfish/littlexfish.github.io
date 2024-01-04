import fs.SettKeys
import fs.Settings
import kotlinx.browser.document
import kotlinx.html.*
import kotlinx.html.js.div
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

internal fun TagConsumer<HTMLElement>.rootFrame() {
	div {
		id = "frame"
		// content
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