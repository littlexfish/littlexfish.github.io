package style

import fs.SettKeys
import fs.Settings

object SettingsStyle : StyleRegister("settings") {

	override fun getStyleContent(): String = """
		$settingsBtnHover
		$settingsPanel
		$settingsCategory
		$SETTINGS_CONTENT
		$settingsItem
	"""

	private val settingsBtnHover = """
			#setting-btn-outline:hover {
			color: ${Settings[SettKeys.Theme.BUTTON_COLOR_HOVER]};
			border: ${Settings[SettKeys.Theme.BUTTON_COLOR_HOVER]} 1px solid;
		}
		#setting-btn-outline:active {
			color: ${Settings[SettKeys.Theme.BUTTON_COLOR_ACTIVE]};
			border: ${Settings[SettKeys.Theme.BUTTON_COLOR_ACTIVE]} 1px solid;
		}
		"""

	private val settingsPanel = """
		#setting-panel-outline {
			display: none;
			$CENTER_ELEMENT
			width: 80%;
			height: 80%;
			max-width: 720px;
			max-height: 480px;
			border: ${Settings[SettKeys.Theme.FRAME]} 1px solid;
			background: ${Settings[SettKeys.Theme.BACKGROUND]}dd;
			border-right-width: 3px;
			border-bottom-width: 3px;
			border-radius: 5px;
			overflow: visible;
			padding: 5px 7px 7px 5px;
		}
		#setting-panel-outline.open {
			display: block;
		}
		#setting-panel-close {
			position: absolute;
			top: -20px;
			right: -20px;
			color: ${Settings[SettKeys.Theme.BUTTON_COLOR]};
		}
		#setting-panel-close:hover {
			color: ${Settings[SettKeys.Theme.BUTTON_COLOR_HOVER]};
		}
		#setting-panel-close:active {
			color: ${Settings[SettKeys.Theme.BUTTON_COLOR_ACTIVE]}
		}
		#setting-panel {
			overflow: hidden;
			display: flex;
			gap: 5px;
			$FULL_WH
		}
	"""

	private val settingsCategory = """
		.setting-category {
			border: ${Settings[SettKeys.Theme.FRAME_LIGHT]} 1px dashed;
			border-radius: 5px;
			margin: 5px;
			padding: 5px;
			$NO_SELECTION
		}
		.setting-category.active {
			border-style: solid;
			margin: 5px;
		}
	"""

	private const val SETTINGS_CONTENT = """
		#setting-content {
			$FULL_WH
			overflow: clip;
		}
		.setting-content {
			display: none;
			overflow: auto;
			$FULL_WH
		}
		.setting-content.active {
			display: block;
		}
	"""

	private val settingsItem = """
		.setting-group {
			font-weight: bold;
			overflow: clip;
			margin: 5px 0;
			border-radius: 5px;
			background: ${Settings[SettKeys.Theme.FOREGROUND_DARK]};
			padding: 5px;
			$NO_SELECTION
		}
		.setting-item {
			display: flex;
			overflow: clip;
			margin: 5px 0;
			margin-left: 10px;
		}
		.setting-item > input {
			width: 20%;
			max-width: 100px;
			min-width: 20px;
			height: 25px;
			margin: auto;
		}
		.setting-item > span {
			width: 100%;
			height: fit-content;
			overflow: hidden;
			text-overflow: ellipsis;
		}
	"""

}


