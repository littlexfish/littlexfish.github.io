package style

import fs.SettKeys
import fs.Settings

object FileViewerStyle : StyleRegister("file-viewer") {
	override fun getStyleContent(): String = """
		#html-viewer {
			display: block;
			$FULL_WH
		}
		$title
		$container
	"""

	private val title = """
		#title-bar {
			display: flex;
			font-size: 16px;
			border: ${Settings[SettKeys.Theme.FRAME]} 1px solid;
			padding: 5px;
			border-radius: 5px;
			height: 20px;
			margin-bottom: 5px;
		}
		#title-bar > #title {
			display: flex;
			width: 100%;
			height: fit-content;
			margin: auto;
		}
		#title-bar > #title > #title-content {
			display: block;
			flex: none;
			white-space: pre;
			$NO_SELECTION
		}
		#title-bar > #title > #open-type {
			display: block;
			flex: none;
			margin-left: 0.5em;
			white-space: pre;
			color: ${Settings[SettKeys.Theme.FOREGROUND_DARK]};
			$NO_SELECTION
		}
		#title-bar > #close {
			color: ${Settings[SettKeys.Theme.BUTTON_COLOR]};
		}
		#title-bar > #close:hover {
			color: ${Settings[SettKeys.Theme.BUTTON_COLOR_HOVER]};
		}
		#title-bar > #close:active {
			color: ${Settings[SettKeys.Theme.BUTTON_COLOR_ACTIVE]};
		}
	"""

	private val container = """
		#container {
			padding: 5px;
			border: ${Settings[SettKeys.Theme.FRAME]} 1px solid;
			border-radius: 5px;
			height: calc(100% - 45px);
		}
		#container > iframe {
			$FULL_WH
			border: none;
			outline: none;
			overflow: auto;
			background: white;
		}
	"""

}