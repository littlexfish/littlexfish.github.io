package style

import fs.SettKeys
import fs.Settings

object TerminalStyle : StyleRegister("terminal") {
	override fun getStyleContent(): String = """
		$outputStyle
		$inputStyle
	"""

	private val outputStyle = """
		#terminal-output {
			overflow-y: auto;
			display: block;
			width: 100%;
			height: calc(100% - 30px);
		}
		#terminal-output > div {
			margin: 5px 0;
		}
		#terminal-output a {
			text-decoration: none;
			color: ${Settings[SettKeys.Theme.COLOR_1]};
		}
		#terminal-output a:hover {
			text-decoration: underline;
		}
	"""

	private val inputStyle = """
		#terminal-input-outline {
			display: flex;
			width: 100%;
			border-bottom: ${Settings[SettKeys.Theme.FRAME]} 2px solid;
		}
		#terminal-input-outline.focus {
			border-bottom-color: ${Settings[SettKeys.Theme.FRAME_LIGHT]};
		}
		#terminal-input-outline > span {
			display: block;
			width: fit-content;
			height: fit-content;
			margin: auto;
			white-space: pre;
			$NO_SELECTION
		}
		#terminal-input {
			display: block;
			background: none;
			border: none;
			color: ${Settings[SettKeys.Theme.FOREGROUND]};
			caret-color: ${Settings[SettKeys.Theme.FOREGROUND]};
			$FULL_WH
			font-size: 16px;
		}
		#terminal-input:focus {
			outline: none;
		}
	"""
}