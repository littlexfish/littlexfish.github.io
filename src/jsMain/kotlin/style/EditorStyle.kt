package style

import fs.SettKeys
import fs.Settings

fun editorStyle(): String = """
	#editor-frame {
		display: flex;
		${fullWH()}
		gap: 10px;
	}
	${editorFileList()}
	${editor()}
	${codeHighlight()}
""".replace("[\\t\\n\\r]+".toRegex(), "")

private fun editorFileList() = """
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
		border: ${Settings[SettKeys.Theme.FRAME]} 1px dashed;
		cursor: pointer;
		text-overflow: ellipsis;
		margin: 5px;
		border-radius: 5px;
		${noSelection()}
	}
	#editor-files > div.opened {
		border: ${Settings[SettKeys.Theme.FRAME_LIGHT]} 1px solid;
	}
""".replace("[\\t\\n\\r]+".toRegex(), "")

private fun editor() = """
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
		${noSelection()}
	}
	#editor-textarea {
		color: transparent;
		padding: 0;
	}
	/*#editor-textarea::-moz-selection, #editor-textarea::selection {
		color: transparent;
		background: transparent;
	}*/
	::-moz-selection, ::selection {
		color: transparent;
	}
	#editor-textarea:focus {
		outline: none;
	}
	#editor:focus {
		border: ${Settings[SettKeys.Theme.FRAME_LIGHT]} 1px solid;
	}
	#editor.nowrap {
		white-space: pre;
	}
	#editor.no-perm {
		color: ${Settings[SettKeys.Theme.COLOR_ERROR]};
	}
""".replace("[\\t\\n\\r]+".toRegex(), "")

// FIXME: in editor, the first line always has a margin at left
// official style: https://github.com/highlightjs/highlight.js/tree/main/src/styles
private fun codeHighlight() = """
	.hljs > span {
		white-space: pre;
	}
	#editor {
		color: #a9b7c6;
		background: #282b2e;
	}
	.hljs-number,
	.hljs-literal,
	.hljs-symbol,
	.hljs-bullet {
		color: #6897BB;
	}
	.hljs-keyword,
	.hljs-selector-tag,
	.hljs-deletion {
		color: #cc7832;
	}
	.hljs-variable,
	.hljs-template-variable,
	.hljs-link {
		color: #629755;
	}
	.hljs-comment,
	.hljs-quote {
		color: #808080;
	}
	.hljs-meta {
		color: #bbb529;
	}
	.hljs-string,
	.hljs-attribute,
	.hljs-addition {
		color: #6A8759;
	}
	.hljs-section,
	.hljs-title,
	.hljs-type {
		color: #ffc66d;
	}
	.hljs-name,
	.hljs-selector-id,
	.hljs-selector-class {
		color: #e8bf6a;
	}
	.hljs-emphasis {
		font-style: italic;
	}
	.hljs-strong {
		font-weight: bold;
	}
""".replace("[\\t\\n\\r]+".toRegex(), "")