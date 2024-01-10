package style

import fs.SettKeys
import fs.Settings

object RootStyle : StyleRegister("root") {

	override fun getStyleContent(): String = """
		$GLOBAL_STYLE
		$DEFAULT_INPUT
		body {
			background-color: ${Settings[SettKeys.Theme.BACKGROUND]};
			color: ${Settings[SettKeys.Theme.FOREGROUND]};
			margin: 0;
			padding: 0;
		}
		pre {
			margin: 0;
			white-space: pre;
		}
		#frame {
			display: block;
			position: absolute;
			top: 20px;
			left: 20px;
			bottom: 20px;
			right: 20px;
			border: ${Settings[SettKeys.Theme.FRAME]} 1px solid;
			border-radius: 5px;
			padding: 10px;
		}
		$textArea
		$settingsBtn
		$SCROLL_BAR
	"""

	private const val GLOBAL_STYLE = """
		* {
			$MONO_FONT
			font-feature-settings: "calt" 0;
			white-space: nowrap;
		}
		.ligatures {
			font-feature-settings: "calt" 1;
		}
		.ligatures-all {
			font-feature-settings: "calt" 0, "dlig" 1;
		}
	"""

	private const val DEFAULT_INPUT = """
		input[type="color"] {
			-webkit-appearance: none;
			-moz-appearance: none;
			appearance: none;
			width: 100px;
			background-color: transparent;
			border: none;
		}
		input::-webkit-color-swatch, input::-moz-color-swatch {
			border: none;
		}
	"""

	private val textArea = """
		textArea {
			background: none;
			border: none;
			color: ${Settings[SettKeys.Theme.FOREGROUND]};
			caret-color: white;
			$FULL_WH
			font-size: 16px;
			resize: none;
			overflow: auto;
		}
		textarea:disabled:not(.nowrap) {
			white-space: pre-wrap;
		}
		textarea:disabled.nowrap {
			white-space: pre;
		}
	"""

	private val settingsBtn = """
		#setting-btn-outline {
			position: absolute;
			bottom: 5px;
			right: 5px;
			background: ${Settings[SettKeys.Theme.BACKGROUND]};
			border-radius: 30px;
			width: 30px;
			height: 30px;
			border: ${Settings[SettKeys.Theme.BUTTON_COLOR]} 1px solid;
			color: ${Settings[SettKeys.Theme.BUTTON_COLOR]};
		}
		#setting-btn {
			display: grid;
			$CENTER_ELEMENT
		}
		#setting-btn > i {
			grid-column: 1;
			grid-row: 1;
		}
	""".trimIndent()

	private const val SCROLL_BAR = """
		::-webkit-scrollbar {
			height: 5px;
			width: 5px;
		}
		::-webkit-scrollbar-track {
			background: none;
			border: rgb(20, 20, 20) 1px dashed;
			border-radius: 5px;
		}
		::-webkit-scrollbar-track:hover {
			border-color: rgb(50, 50, 50);
			border-style: solid;
		}
		::-webkit-scrollbar-thumb {
			background: rgb(50, 50, 50);
			border-radius: 5px;
		}
		::-webkit-scrollbar-thumb:hover {
			background: rgb(100, 100, 100);
		}
		::-webkit-scrollbar-thumb:active {
			background: rgb(150, 150, 150);
		}
		::-webkit-scrollbar-corner {
			background: none;
		}
		::-webkit-scrollbar-button {
			display: none;
		}
		::-webkit-scrollbar-track-piece {
			background: none;
		}
		::-webkit-resizer {
			background: rgb(50, 50, 50);
		}
		::-webkit-resizer:hover {
			background: rgb(100, 100, 100);
		}
		::-webkit-resizer:active {
			background: rgb(150, 150, 150);
		}
	"""

}