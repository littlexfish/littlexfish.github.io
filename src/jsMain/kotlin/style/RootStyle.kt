package style

import fs.SettKeys
import fs.Settings

fun rootStyle(): String = """
	${globalStyle()}
	${defaultInput()}
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
	${errorFrame()}
	${textArea()}
	${scrollBarStyle()}
""".replace("[\\t\\n\\r]+".toRegex(), "")

private fun globalStyle() = """
	* {
		${monoFont()}
		font-feature-settings: "calt" 0;
		white-space: nowrap;
	}
	.ligatures {
		font-feature-settings: "calt" 1;
	}
	.ligatures-all {
		font-feature-settings: "calt" 0, "dlig" 1;
	}
""".replace("[\\t\\n\\r]+".toRegex(), "")

private fun defaultInput() = """
	input[type="color"] {
		-webkit-appearance: none;
		-moz-appearance: none;
		appearance: none;
		width: 100px;
		height: 100px;
		background-color: transparent;
		border: none;
	}
	input::-webkit-color-swatch, input::-moz-color-swatch {
		border: none;
	}
""".replace("[\\t\\n\\r]+".toRegex(), "")

private fun errorFrame(): String {
	var error = Settings[SettKeys.Theme.COLOR_ERROR]
	if(error.isBlank()) error = "#ff0000"
	var color1 = Settings[SettKeys.Theme.COLOR_1]
	if(color1.isBlank()) color1 = "#6495ed"
	return """
	#error-frame {
		display: block;
		border: $error 1px solid;
		border-radius: 5px;
		${centerElement()}
		padding: 20px 50px;
		font-size: 30px;
	}
	#error-frame a {
		text-decoration: none;
		color: $color1;
	}
	#error-frame a:hover {
		text-decoration: underline;
	}
	""".replace("[\\t\\n\\r]+".toRegex(), "")
}

private fun textArea() = """
	textArea {
		background: none;
		border: none;
		color: ${Settings[SettKeys.Theme.FOREGROUND]};
		caret-color: white;
		${fullWH()}
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
""".replace("[\\t\\n\\r]+".toRegex(), "")

private fun scrollBarStyle() = """
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
""".replace("[\\t\\n\\r]+".toRegex(), "")