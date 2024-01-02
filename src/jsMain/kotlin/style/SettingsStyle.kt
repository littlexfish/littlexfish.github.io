package style

import fs.SettKeys
import fs.Settings

fun settingsStyle(): String = """
	${settingsButton()}
	${settingsButton()}
	${settingsPanel()}
	${settingsCategory()}
	${settingsContent()}
	${settingsItem()}
""".replace("[\\t\\n\\r]+".toRegex(), "")

private fun settingsButton() = """
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
		${centerElement()}
	}
	#setting-btn-outline:hover {
		color: ${Settings[SettKeys.Theme.BUTTON_COLOR_HOVER]};
		border: ${Settings[SettKeys.Theme.BUTTON_COLOR_HOVER]} 1px solid;
	}
	#setting-btn-outline:active {
		color: ${Settings[SettKeys.Theme.BUTTON_COLOR_ACTIVE]};
		border: ${Settings[SettKeys.Theme.BUTTON_COLOR_ACTIVE]} 1px solid;
	}
""".replace("[\\t\\n\\r]+".toRegex(), "")

private fun settingsPanel() = """
	#setting-panel-outline {
		display: none;
		${centerElement()}
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
		${fullWH()}
	}
""".replace("[\\t\\n\\r]+".toRegex(), "")

private fun settingsCategory() = """
	.setting-category {
		border: ${Settings[SettKeys.Theme.FRAME_LIGHT]} 1px dashed;
		border-radius: 5px;
		margin: 5px;
		padding: 5px;
		${noSelection()}
	}
	.setting-category.active {
		border-style: solid;
		margin: 5px;
	}
""".replace("[\\t\\n\\r]+".toRegex(), "")

private fun settingsContent() = """
	#setting-content {
		${fullWH()}
		overflow: clip;
	}
	.setting-content {
		display: none;
		overflow: auto;
		${fullWH()}
	}
	.setting-content.active {
		display: block;
	}
""".replace("[\\t\\n\\r]+".toRegex(), "")

private fun settingsItem() = """
	.setting-group {
		font-weight: bold;
		overflow: clip;
		margin: 5px 0;
		border-radius: 5px;
		background: ${Settings[SettKeys.Theme.FOREGROUND_DARK]};
		padding: 5px;
		${noSelection()}
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
""".replace("[\\t\\n\\r]+".toRegex(), "")