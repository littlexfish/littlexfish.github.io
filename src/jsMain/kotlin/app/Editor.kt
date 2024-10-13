package app

import Application
import Translation
import fs.FS
import fs.SettKeys
import fs.Settings
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onMouseDownFunction
import kotlinx.html.js.onMouseUpFunction
import org.w3c.dom.*
import org.w3c.dom.events.MouseEvent
import style.EditorStyle
import style.StyleRegister
import util.GlobalState

class Editor : App("editor") {

	private val fileListElement: HTMLDivElement
		get() = document.getElementById("editor-files") as HTMLDivElement
	private val fileEditorElement: HTMLTextAreaElement
		get() = document.getElementById("editor-textarea") as HTMLTextAreaElement
	private val editorHighlightElement: HTMLElement
		get() = document.getElementById("editor-code") as HTMLElement
	private val notSavedFrameElement: HTMLDivElement
		get() = document.getElementById("editor-not-saved") as HTMLDivElement

	private var currentOpenFile: FileInfo? = null
	private val openedFiles = mutableListOf<FileInfo>()
	private val currentContent = mutableMapOf<FileInfo, Pair<String, String>?>()

	override fun buildGUI(): TagConsumer<HTMLElement>.() -> Unit = {
		div {
			id = "editor-frame"
			div {
				id = "editor-files"
			}
			div {
				id = "editor"
				div {
					id = "editor-code"
					classes = setOf("editor")
				}
				textArea {
					id = "editor-textarea"
					classes = setOf("editor")
					autoFocus = true
					attributes["autocorrect"] = "off"
					attributes["autocapitalize"] = "off"
					spellCheck = false
				}
			}
		}
		div {
			id = "editor-not-saved"
			div {
				+Translation["editor.not_saved"]
			}
			div {
				button {
					+Translation["editor.not_saved.save"]
					onClickFunction = {
						tryExit(true)
					}
				}
				button {
					+Translation["editor.not_saved.dont_save"]
					onClickFunction = {
						tryExit(false)
					}
				}
				button {
					+Translation["editor.not_saved.cancel"]
					onClickFunction = {
						setNotSavedFrameVisible(false)
					}
				}
			}
		}
	}

	override fun getStyleRegister(): StyleRegister = EditorStyle

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
					setContent(fileEditorElement.value)
				}
			}
		}
		document.onkeydown = { evt ->
			if(evt.key == "Escape") {
				if(Settings[SettKeys.Editor.AUTO_SAVE].toBoolean()) {
					tryExit(true)
				}
				else if(openedFiles.indices.any { !isSameAsSaved(it) }) {
					setNotSavedFrameVisible(true)
				}
				else {
					tryExit(false)
				}
			}
			else if(evt.ctrlKey && evt.key == "s") {
				evt.preventDefault()
				val idx = getOpenedFile()
				if(idx >= 0) onSave(idx)
			}
		}
		val consumeMouseEvent = { evt: MouseEvent ->
			if(notSavedFrameElement.classList.contains("open")) evt.preventDefault()
		}
		document.let {
			it.onmouseup = consumeMouseEvent
			it.onmousedown = consumeMouseEvent
			it.onmousemove = consumeMouseEvent
			it.onmouseout = consumeMouseEvent
			it.onmouseover = consumeMouseEvent
			it.onmouseenter = consumeMouseEvent
			it.onmouseleave = consumeMouseEvent
		}
		fileEditorElement.oninput = {
			setContent(fileEditorElement.value)
			null
		}
		window.asDynamic().printDebug = {
			console.log(openedFiles, currentContent)
		}
		setTabSize(4)
		setLigaturesEnabled(Settings[SettKeys.Editor.LIGATURES].toBoolean())
		val files = GlobalState.get<List<FileInfo>>("files")
		if(files != null) {
			files.forEach(::loadFile)
			if(files.isNotEmpty()) {
				selectFile(openedFiles[0])
			}
		}
		else {
			GlobalState.set("files", mutableListOf<FileInfo>())
		}
		refreshEditor()
	}

	override fun onSuspend() {
		document.onkeydown = null
		document.let {
			it.onmouseup = null
			it.onmousedown = null
			it.onmousemove = null
			it.onmouseout = null
			it.onmouseover = null
			it.onmouseenter = null
			it.onmouseleave = null
		}
	}

	private fun setLigaturesEnabled(enabled: Boolean) {
		if(enabled) {
			editorHighlightElement.classList.add("ligatures")
		}
		else {
			editorHighlightElement.classList.remove("ligatures")
		}
	}

	private fun refreshEditor() {
		fileTitleSimplify()
		softRefreshFileList()
		if(fileListElement.childElementCount == 0) {
			fileEditorElement.value = ""
			editorHighlightElement.clear()
			fileEditorElement.disabled = true
		}
		else {
			fileEditorElement.disabled = false
		}
	}

	private fun setTabSize(size: Int) {
		fileEditorElement.style.tabSize = size.toString()
		editorHighlightElement.style.tabSize = size.toString()
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

	private fun openFile(path: String): FileInfo {
		for(i in 0..<openedFiles.size) {
			if(openedFiles[i].path == path) {
				return openedFiles[i]
			}
		}
		return FileInfo(path)
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
		if(currentOpenFile == openedFiles[idx]) {
			classes = setOf("opened")
		}
		onMouseDownFunction = { it.preventDefault() }
		onMouseUpFunction = {
			if(it.asDynamic().button == 0) {
				it.preventDefault()
				if(window.asDynamic().event.ctrlKey.unsafeCast<Boolean>()) {
					onCloseFile(idx)
				}
				else {
					selectFile(openedFiles[idx])
				}
			}
			else if(it.asDynamic().button == 1) {
				it.preventDefault()
				onCloseFile(idx)
			}
		}
	}

	private fun selectFile(fi: FileInfo) {
		if(currentOpenFile == fi) {
			softRefreshFileList()
			return
		}
		val loadFileFunc = {
			loadFile(fi) {
				val content = currentContent[fi]?.second
				currentOpenFile = fi
				if(content == null) {
					fileEditorElement.classList.add("no-perm")
					fileEditorElement.value = Translation["editor.no_permission_read"]
					fileEditorElement.disabled = true
				}
				else {
					fileEditorElement.classList.remove("no-perm")
					fileEditorElement.disabled = false
					fileEditorElement.readOnly = !FS.canWrite(fi.path)
					fileEditorElement.value = content
					setContent(content)
				}
				softRefreshFileList()
			}
		}
		val idx = openedFiles.indexOf(currentOpenFile)
		if(idx >= 0 && Settings[SettKeys.Editor.AUTO_SAVE].toBoolean()) {
			onSave(idx, loadFileFunc)
		}
		else {
			loadFileFunc()
		}
	}

	private fun loadFile(fi: FileInfo, afterLoad: (() -> Unit)? = null) {
		if(fi !in openedFiles) {
			openedFiles.add(0, fi)
		}
		val globalState = GlobalState.get<MutableList<FileInfo>>("files")
		if(globalState?.contains(fi) != true) {
			globalState?.add(0, fi)
		}
		if(fi !in currentContent) {
			MainScope().launch {
				if(FS.canRead(fi.path)) {
					val file = FS.getFile(fi.path)
					FS.readContentAsText(file) {
						currentContent[fi] = it to it
						afterLoad?.invoke()
					}
				}
				else {
					currentContent[fi] = null
					afterLoad?.invoke()
				}
			}
		}
		else {
			afterLoad?.invoke()
		}
	}

	private fun onCloseFile(idx: Int, closeEmpty: Boolean = true) {
		val fi = openedFiles.removeAt(idx)
		currentContent.remove(fi)
		GlobalState.get<MutableList<FileInfo>>("files")?.remove(fi)
		refreshEditor()
		if(openedFiles.isEmpty() && closeEmpty) {
			Application.back()
			return
		}
		if(fi == currentOpenFile) {
			if (openedFiles.isNotEmpty()) {
				if (idx >= openedFiles.size) {
					selectFile(openedFiles.last())
				}
				else {
					selectFile(openedFiles[idx])
				}
			}
		}
		else if(currentOpenFile != null) {
			selectFile(currentOpenFile!!)
		}
	}

	private fun getOpenedFile(): Int {
		return openedFiles.indexOf(currentOpenFile)
	}

	private fun onSave(idx: Int, onSaveEnd: (() -> Unit)? = null) {
		val fi = openedFiles[idx]
		val path = fi.path
		if(!FS.canWrite(path)) return
		if(fileEditorElement.readOnly) return
		MainScope().launch {
			val file = FS.getFile(path)
			val writer = file.createWritable().await()
			writer.write(currentContent[fi]!!.second)
			writer.close()
			currentContent[fi] = currentContent[fi]!!.second to currentContent[fi]!!.second
			refreshSavedStatus()
			onSaveEnd?.invoke()
		}
	}

	override fun onReceiveMessage(msg: String, extra: Map<String, String>?) {
		if(extra != null) {
			val file = extra["file"]
			if(file != null) {
				val fi = openFile(file)
				selectFile(fi)
				refreshEditor()
			}
		}
	}

	private fun setCurrentLanguage(lang: String) {
		val classes = editorHighlightElement.classList
		val currentLang = classes.asList().filter { it.startsWith("language-") }
		currentLang.forEach { classes.remove(it) }
		classes.add("language-$lang")
	}

	private fun setContent(content: String?) {
		fileEditorElement.value = content ?: ""
		editorHighlightElement.clear()
		if(content != null) {
			currentOpenFile?.let {
				currentContent[it] = currentContent[it]!!.first to content
			}
			editorHighlightElement.innerHTML = content.replace("&", "&amp;").replace("<", "&lt;")
			refreshHighlight()
		}
		else {
			editorHighlightElement.innerHTML = ""
		}
		refreshSavedStatus()
	}

	private fun refreshHighlight() {
		setCurrentLanguage(currentOpenFile?.getExtension() ?: "plaintext")
		editorHighlightElement.removeAttribute("data-highlighted")
		val hljs = js("hljs")
		hljs.highlightElement(editorHighlightElement)
	}

	private fun refreshSavedStatus() {
		for(i in 0..<openedFiles.size) {
			if(isSameAsSaved(i)) {
				fileListElement.children[i]?.classList?.remove("not-saved")
			}
			else {
				fileListElement.children[i]?.classList?.add("not-saved")
			}
		}
	}

	private fun isSameAsSaved(idx: Int): Boolean {
		val fi = openedFiles[idx]
		if(currentContent[fi] == null) return true
		return currentContent[fi]!!.first == currentContent[fi]!!.second
	}

	private fun tryExit(withSave: Boolean) {
		if(withSave) {
			val needSave = openedFiles.indices.filter { !isSameAsSaved(it) }
			var currentIdx = 0
			var endOfSave: (() -> Unit)? = null
			endOfSave = {
				if(currentIdx < needSave.size) {
					onSave(needSave[currentIdx++], endOfSave)
				}
				else {
					Application.backToApp(Terminal::class.js)
				}
			}
			if(needSave.isEmpty()) {
				endOfSave()
			}
			else {
				onSave(needSave[currentIdx++], endOfSave)
			}
		}
		else {
			Application.backToApp(Terminal::class.js)
		}
	}

	private fun setNotSavedFrameVisible(vis: Boolean) {
		if(vis) notSavedFrameElement.classList.add("open")
		else notSavedFrameElement.classList.remove("open")
	}

	private data class FileInfo(val path: String) {
		private val spl = path.split("/")
		private var displayLength = 1
		private var forceExt: String? = null
		fun addDisplayLength() {
			if(displayLength < spl.size) displayLength++
		}

		fun getDisplay(): String {
			return spl.takeLast(displayLength).joinToString("/")
		}
		fun resetDisplay() {
			displayLength = 1
		}
		fun setForceExt(ext: String?) {
			forceExt = ext
		}
		fun getExtension(): String {
			if(forceExt != null) return forceExt!!
			val ext = path.substringAfterLast(".", "")
			return if(ext.contains("/")) "" else ext
		}
	}

}