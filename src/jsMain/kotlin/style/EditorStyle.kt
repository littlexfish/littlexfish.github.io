package style

import fs.SettKeys
import fs.Settings

object EditorStyle : StyleRegister("editor") {
	override fun getStyleContent(): String = """
		#editor-frame {
			display: flex;
			$FULL_WH
			gap: 10px;
		}
		$editorFileList
		$editor
		$codeHighlight
		$notSavedFrame
	"""

	private val editorFileList = """
		#editor-files {
			display: block table;
			height: 100%;
			width: auto;
			min-width: 100px;
			border: ${Settings[SettKeys.Theme.FRAME]} 1px solid;
			border-radius: 5px;
			overflow: hidden auto;
		}
		#editor-files > div {
			display: block;
			overflow: hidden;
			height: fit-content;
			min-height: 20px;
			width: auto;
			max-width: 300px;
			padding: 5px;
			border: ${Settings[SettKeys.Editor.FILE_FRAME]} 1px dashed;
			cursor: pointer;
			text-overflow: ellipsis;
			margin: 5px;
			border-radius: 5px;
			$NO_SELECTION
		}
		#editor-files > div.opened {
			border-style: solid;
		}
		#editor-files > div.not-saved {
			border-color: ${Settings[SettKeys.Editor.FILE_NOT_SAVE]};
		}
	"""

	private val editor = """
		#editor {
			position: relative;
			display: grid;
			padding: 5px;
			border: ${Settings[SettKeys.Theme.FRAME]} 1px solid;
			border-radius: 5px;
			height: calc(100% - 10px);
			width: 100%;
			overflow: auto;
		}
		#editor > * {
			grid-column: 1;
			grid-row: 1;
			overflow: visible;
		}
		.editor {
			padding: 2px;
			background: transparent;
		}
		#editor-code {
			white-space: pre;
			font-family: 'Iosevka Term SS14 Web', monospace;
			$NO_SELECTION
		}
		#editor-textarea {
			color: transparent;
			padding: 0;
		}
		::-moz-selection, ::selection {
			color: transparent;
		}
		#editor-textarea:focus {
			outline: none;
		}
		#editor-textarea.nowrap {
			white-space: pre;
		}
		#editor-textarea.no-perm {
			color: ${Settings[SettKeys.Theme.COLOR_ERROR]};
		}
	"""

	private val notSavedFrame = """
		#editor-not-saved {
			display: none;
			$CENTER_ELEMENT
			padding: 20px;
			border: ${Settings[SettKeys.Theme.FRAME]} 1px solid;
			border-right-width: 3px;
			border-bottom-width: 3px;
			border-radius: 5px;
			background: ${Settings[SettKeys.Theme.BACKGROUND]};
		}
		#editor-not-saved.open {
			display: block;
		}
		#editor-not-saved > div:nth-child(2) {
			display: flex;
			margin-top: 10px;
			justify-content: flex-end;
			gap: 5px;
		}
	"""

	// FIXME: in editor, the first line always has a margin at left
	// official style: https://github.com/highlightjs/highlight.js/tree/main/src/styles
	private val codeHighlight = """
		.hljs > span {
			white-space: pre;
		}
		#editor {
			color: #a9b7c6;
			background: #282b2e;
		}
		.hljs-number {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_NUMBER]}
		}
		.hljs-literal {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_LITERAL]}
		}
		.hljs-symbol {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_SYMBOL]}
		}
		.hljs-bullet {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_BULLET]}
		}
		.hljs-keyword {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_KEYWORD]}
		}
		.hljs-selector-tag {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_SELECTOR_TAG]}
		}
		.hljs-deletion {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_DELETION]}
		}
		.hljs-variable {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_VARIABLE]}
		}
		.hljs-template-variable {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_TEMPLATE_VARIABLE]}
		}
		.hljs-link {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_LINK]}
		}
		.hljs-comment {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_COMMENT]}
		}
		.hljs-quote {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_QUOTE]}
		}
		.hljs-meta {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_META]}
		}
		.hljs-string {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_STRING]}
		}
		.hljs-attribute {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_ATTRIBUTE]}
		}
		.hljs-addition {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_ADDITION]}
		}
		.hljs-section {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_SECTION]}
		}
		.hljs-title {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_TITLE]}
		}
		.hljs-type {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_TYPE]}
		}
		.hljs-name {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_NAME]}
		}
		.hljs-selector-id {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_SELECTOR_ID]}
		}
		.hljs-selector-class {
			color: ${Settings[SettKeys.Editor.HIGHLIGHT_SELECTOR_CLASS]}
		}
		.hljs-emphasis {
			font-style: italic;
		}
		.hljs-strong {
			font-weight: bold;
		}
	"""

}


