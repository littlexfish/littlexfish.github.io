package style

import fs.SettKeys
import fs.Settings

fun htmlViewerStyle(): String = """
	#html-viewer {
		display: block;
		${fullWH()}
	}
	${title()}
	${container()}
""".replace("[\\t\\n\\r]+".toRegex(), "")

private fun title() = """
	#title-bar {
		display: flex;
		font-size: 16px;
		border: ${Settings.getSettings(SettKeys.Theme.FRAME)} 1px solid;
		padding: 5px;
		border-radius: 5px;
		height: 20px;
		margin-bottom: 5px;
	}
	#title-bar > #title {
		display: block;
		width: 100%;
		height: fit-content;
		margin: auto;
		white-space: pre;
		${noSelection()}
	}
	${titleCloseButton()}
""".replace("[\\t\\n\\r]+".toRegex(), "")

private fun titleCloseButton() = """
	#title-bar > #close {
		color: ${Settings.getSettings(SettKeys.Theme.BUTTON_COLOR)};
	}
	#title-bar > #close:hover {
		color: ${Settings.getSettings(SettKeys.Theme.BUTTON_COLOR_HOVER)};
	}
	#title-bar > #close:active {
		color: ${Settings.getSettings(SettKeys.Theme.BUTTON_COLOR_ACTIVE)};
	}
""".replace("[\\t\\n\\r]+".toRegex(), "")

private fun container() = """
	#container {
		padding: 5px;
		border: ${Settings.getSettings(SettKeys.Theme.FRAME)} 1px solid;
		border-radius: 5px;
		height: calc(100% - 45px);
	}
	#container > iframe {
		${fullWH()}
		border: none;
		outline: none;
		overflow: auto;
	}
""".replace("[\\t\\n\\r]+".toRegex(), "")