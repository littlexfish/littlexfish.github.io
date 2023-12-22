package app

import Application
import Translation
import fs.FS
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onMouseDownFunction
import kotlinx.html.js.onMouseUpFunction
import org.w3c.dom.*
import org.w3c.files.FileReader

class Editor : App("editor") {

	private val fileListElement: HTMLDivElement
		get() = document.getElementById("editor-files") as HTMLDivElement
	private val fileEditorElement: HTMLTextAreaElement
		get() = document.getElementById("editor") as HTMLTextAreaElement

	private val openedFiles = mutableListOf<FileInfo>()

	override fun buildGUI(): DIV.() -> Unit = {
		div {
			id = "editor-frame"
			div {
				id = "editor-files"
			}
			textArea {
				id = "editor"
				wrap = TextAreaWrap.soft
			}
		}
	}

	override fun onInit() {
	}

	override fun onRestore() {
		fileEditorElement.onkeydown = {
			if(it.key == "Tab") {
				it.preventDefault()
				val start = fileEditorElement.selectionStart
				val end = fileEditorElement.selectionEnd
				if(start != null && end != null) {
					fileEditorElement.value = fileEditorElement.value.substring(0, start) + "\t" + fileEditorElement.value.substring(end)
					fileEditorElement.selectionStart = start + 1
					fileEditorElement.selectionEnd = start + 1
				}
			}
			else if(it.ctrlKey && it.key == "s") {
				it.preventDefault()
				val idx = getOpenedFile()
				if(idx >= 0) onSave(idx)
			}
		}
		document.onkeydown = {
			if(it.key == "Escape") {
				Application.startApp("terminal")
			}
		}
		setEditorSoftWrap(false)
		setTabSize(4)
		refreshEditor()
	}

	override fun onSuspend() {
		document.onkeydown = null
	}

	private fun refreshEditor() {
		fileTitleSimplify()
		softRefreshFileList()
		if(fileListElement.childElementCount == 0) {
			fileEditorElement.value = ""
			fileEditorElement.disabled = true
		}
		else {
			fileEditorElement.disabled = false
		}
	}

	private fun setEditorSoftWrap(wrap: Boolean) {
		if(wrap) fileEditorElement.classList.remove("nowrap")
		else fileEditorElement.classList.add("nowrap")
	}

	private fun setTabSize(size: Int) {
		fileEditorElement.style.tabSize = size.toString()
	}

	private fun fileTitleSimplify() {
		// reset display
		openedFiles.forEach(FileInfo::resetDisplay)
		var idx = 0
		while(idx < openedFiles.size) {
			val dup = findDuplicateDisplay(idx)
			if(dup.isNotEmpty()) {
				openedFiles[idx].addDisplayLength()
				dup.forEach(FileInfo::addDisplayLength)
			}
			else idx++
		}
	}

	private fun findDuplicateDisplay(idx: Int): List<FileInfo> {
		val out = mutableListOf<FileInfo>()
		for(i in 0..<openedFiles.size) {
			if(i == idx) continue
			if(openedFiles[i].getDisplay() == openedFiles[idx].getDisplay()) {
				out.add(openedFiles[i])
			}
		}
		return out
	}

	private fun openFile(path: String): Int {
		for(i in 0..<openedFiles.size) {
			if(openedFiles[i].path == path) {
				return i
			}
		}
		openedFiles.add(0, FileInfo(path))
		return 0
	}

	private fun softRefreshFileList() {
		fileListElement.clear()
		fileListElement.append {
			openedFiles.forEachIndexed { idx, it ->
				div {
					displayElement(idx, it.getDisplay())
				}
			}
		}
	}

	private fun DIV.displayElement(idx: Int, name: String) {
		+name
		title = openedFiles[idx].path
		onMouseDownFunction = { it.preventDefault() }
		onMouseUpFunction = {
			if(it.asDynamic().button == 0) {
				it.preventDefault()
				if(window.asDynamic().event.ctrlKey.unsafeCast<Boolean>()) {
					onCloseFile(idx)
				}
				else {
					onFileOpen(idx)
				}
			}
			else if(it.asDynamic().button == 1) {
				it.preventDefault()
				onCloseFile(idx)
			}
		}
	}

	private fun onFileOpen(idx: Int) {
		val open = getOpenedFile()
		if(idx == open) return
		for(i in 0..<fileListElement.childElementCount) {
			if(i == idx) fileListElement.children[i]?.classList?.add("opened")
			else fileListElement.children[i]?.classList?.remove("opened")
		}
		val path = openedFiles[idx].path
		loadFile(path)
	}

	private fun onCloseFile(idx: Int, closeEmpty: Boolean = true) {
		openedFiles.removeAt(idx)
		refreshEditor()
		if(openedFiles.isEmpty() && closeEmpty) {
			Application.startApp("terminal")
			return
		}
		if(openedFiles.isNotEmpty()) {
			val open = getOpenedFile()
			if(idx >= openedFiles.size) {
				onFileOpen(openedFiles.size - 1)
			}
			else if(idx == getOpenedFile()) {
				onFileOpen(idx)
			}
			else {
				onFileOpen(open + (if(idx < open) 0 else -1))
			}
		}
	}

	private fun getOpenedFile(): Int {
		for(i in 0..<fileListElement.childElementCount) {
			if(fileListElement.children[i]?.classList?.contains("opened") == true) return i
		}
		return -1
	}

	private fun loadFile(path: String) {
		MainScope().launch {
			if(FS.canRead(path)) {
				fileEditorElement.classList.remove("no-perm")
				fileEditorElement.disabled = false
				fileEditorElement.readOnly = !FS.canWrite(path)
				val file = FS.getFile(path)
				val reader = FileReader()
				reader.onload = {
					val content = reader.result.unsafeCast<String>()
					content.also { fileEditorElement.value = it }
				}
				reader.readAsText(file.getFile().await())
			}
			else {
				fileEditorElement.classList.add("no-perm")
				fileEditorElement.value = Translation["editor.no_permission_read"]
				fileEditorElement.disabled = true
			}
		}
	}

	private fun onSave(idx: Int) {
		val path = openedFiles[idx].path
		if(!FS.canWrite(path)) return
		if(fileEditorElement.readOnly) return
		MainScope().launch {
			val file = FS.getFile(path)
			val writer = file.createWritable().await()
			writer.write(fileEditorElement.value)
			writer.close()
		}
	}

	override fun onReceiveMessage(msg: String, extra: Map<String, String>?) {
		if(extra != null) {
			val file = extra["file"]
			if(file != null) {
				val idx = openFile(file)
				refreshEditor()
				onFileOpen(idx)
			}
		}
	}

	private data class FileInfo(val path: String) {
		private val spl = path.split("/")
		private var displayLength = 1
		fun addDisplayLength() {
			if(displayLength < spl.size) displayLength++
		}

		fun getDisplay(): String {
			return spl.takeLast(displayLength).joinToString("/")
		}
		fun resetDisplay() {
			displayLength = 1
		}
	}

}