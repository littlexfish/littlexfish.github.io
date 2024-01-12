import command.CommandType
import fs.SettKeys
import fs.Settings
import kotlinx.browser.document
import kotlinx.html.*
import kotlinx.html.js.div
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import module.ModuleRegistry
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

internal fun TagConsumer<HTMLElement>.rootFrame() {
	div {
		id = "frame"
		// content
	}
	val canSettings = ModuleRegistry.getLoadedModule().find { it == CommandType.FS } != null
	div {
		id = "setting-btn-outline"
		div {
			id = "setting-btn"
			i {
				classes = setOf("fa", "fa-xl", "fa-cog")
				if(!canSettings) {
					style = "transform: scale(0.8, 0.8);"
				}
			}
			if(!canSettings) {
				i {
					classes = setOf("fa", "fa-xl", "fa-ban")
					style = "color: #FF0000;z-index: 1"
				}
			}
			if(canSettings) {
				onClickFunction = {
					document.getElementById("setting-panel-outline")?.classList?.add("open")
				}
			}
		}
	}
	if(canSettings) {
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

			subGroup(Translation["settings.editor_group.editor_normal"])
			booleanSelector(Translation["settings.editor.ligatures"], SettKeys.Editor.LIGATURES)
			booleanSelector(Translation["settings.editor.auto_save"], SettKeys.Editor.AUTO_SAVE)
			colorSelector(Translation["settings.editor.file_frame"], SettKeys.Editor.FILE_FRAME)
			colorSelector(Translation["settings.editor.file_not_save"], SettKeys.Editor.FILE_NOT_SAVE)

			subGroup(Translation["settings.editor_group.editor_highlight"])
			colorSelector(Translation["settings.editor.highlight_background"], SettKeys.Editor.HIGHLIGHT_BACKGROUND)
			colorSelector(Translation["settings.editor.highlight_foreground"], SettKeys.Editor.HIGHLIGHT_FOREGROUND)
			colorSelector(Translation["settings.editor.highlight_keyword"], SettKeys.Editor.HIGHLIGHT_KEYWORD)
			colorSelector(Translation["settings.editor.highlight_variable"], SettKeys.Editor.HIGHLIGHT_VARIABLE)
			colorSelector(Translation["settings.editor.highlight_string"], SettKeys.Editor.HIGHLIGHT_STRING)
			colorSelector(Translation["settings.editor.highlight_number"], SettKeys.Editor.HIGHLIGHT_NUMBER)
			colorSelector(Translation["settings.editor.highlight_literal"], SettKeys.Editor.HIGHLIGHT_LITERAL)
			colorSelector(Translation["settings.editor.highlight_symbol"], SettKeys.Editor.HIGHLIGHT_SYMBOL)
			colorSelector(Translation["settings.editor.highlight_bullet"], SettKeys.Editor.HIGHLIGHT_BULLET)
			colorSelector(Translation["settings.editor.highlight_comment"], SettKeys.Editor.HIGHLIGHT_COMMENT)
			colorSelector(Translation["settings.editor.highlight_quote"], SettKeys.Editor.HIGHLIGHT_QUOTE)
			colorSelector(Translation["settings.editor.highlight_attribute"], SettKeys.Editor.HIGHLIGHT_ATTRIBUTE)
			colorSelector(Translation["settings.editor.highlight_section"], SettKeys.Editor.HIGHLIGHT_SECTION)
			colorSelector(Translation["settings.editor.highlight_title"], SettKeys.Editor.HIGHLIGHT_TITLE)
			colorSelector(Translation["settings.editor.highlight_type"], SettKeys.Editor.HIGHLIGHT_TYPE)
			colorSelector(Translation["settings.editor.highlight_name"], SettKeys.Editor.HIGHLIGHT_NAME)
			colorSelector(Translation["settings.editor.highlight_template_variable"], SettKeys.Editor.HIGHLIGHT_TEMPLATE_VARIABLE)
			colorSelector(Translation["settings.editor.highlight_selector_tag"], SettKeys.Editor.HIGHLIGHT_SELECTOR_TAG)
			colorSelector(Translation["settings.editor.highlight_selector_id"], SettKeys.Editor.HIGHLIGHT_SELECTOR_ID)
			colorSelector(Translation["settings.editor.highlight_selector_class"], SettKeys.Editor.HIGHLIGHT_SELECTOR_CLASS)
			colorSelector(Translation["settings.editor.highlight_deletion"], SettKeys.Editor.HIGHLIGHT_DELETION)
			colorSelector(Translation["settings.editor.highlight_link"], SettKeys.Editor.HIGHLIGHT_LINK)
			colorSelector(Translation["settings.editor.highlight_meta"], SettKeys.Editor.HIGHLIGHT_META)
			colorSelector(Translation["settings.editor.highlight_addition"], SettKeys.Editor.HIGHLIGHT_ADDITION)

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