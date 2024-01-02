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
		display: block;
		padding: 5px;
		border: ${Settings[SettKeys.Theme.FRAME]} 1px solid;
		border-radius: 5px;
		outline: none;
		height: calc(100% - 10px);
		width: 100%;
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