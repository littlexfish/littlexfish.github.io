package command.cmds.fs

import Translation
import command.Command
import ext.*
import fs.FS
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutNewLine
import io.pipeOutTag
import io.pipeOutText
import kotlinx.coroutines.await
import org.w3c.files.File
import kotlin.js.Date
import kotlin.math.log

class Ls : Command() {

	/**
	 * Arguments:
	 * -a: list all files
	 * -l: list as list
	 * -h: list as human-readable
	 * -s: sort by size
	 * -S: sort by size descending
	 * -t: sort by time
	 * -T: sort by time descending
	 */
	override suspend fun execute(args: Array<String>): Int {
		val pwd = env["PWD"]!!

		val pArg = parseArgs(args)
		val listIncludeHide = pArg.has("a")
		val listAsList = pArg.has("l")
		val listShowHuman = pArg.has("h")
		val sortType = when {
			pArg.has("s") -> "s"
			pArg.has("S") -> "S"
			pArg.has("t") -> "t"
			pArg.has("T") -> "T"
			else -> ""
		}
		val argDir = pArg.getStandalone().lastOrNull()
		if(!FS.hasDirectory(argDir ?: pwd, pwd)) {
			tunnel.pipeOutErrorTextTr("command.ls.not_found", "path" to (argDir ?: pwd))
			return 1
		}
		val dirHandle = if(argDir == null) FS.getDirectory(pwd) else FS.getDirectory(argDir, false, pwd)

		val list = list(dirHandle, listIncludeHide)
		val sorted = sort(list, sortType)

		out(FS.getAbsolutePath(dirHandle), sorted, listAsList, listShowHuman, listIncludeHide)
		return 0
	}

	private suspend fun list(dir: FileSystemDirectoryHandle, includeHide: Boolean): List<Pair<FileSystemHandle, File?>> {
		val iterator = dir.getEntries()
		val ret = mutableListOf<Pair<FileSystemHandle, File?>>()

		for((k, v) in iterator) {
			if(!includeHide && k.startsWith(".")) continue
			ret.add(v to (if(v is FileSystemFileHandle) v.getFile().await() else null))
		}

		return ret
	}

	private fun sort(list: List<Pair<FileSystemHandle, File?>>, sortType: String): List<Pair<FileSystemHandle, File?>> {
		val sortedDir = list.filter { it.second == null }.sortedBy { it.first.name }
		val file = list.filter { it.second != null }
		return sortedDir.plus(when(sortType) {
			"s" -> file.sortedBy { it.second!!.size.toInt() }
			"S" -> file.sortedByDescending { it.second!!.size.toInt() }
			"t" -> file.sortedBy { it.second!!.lastModified }
			"T" -> file.sortedByDescending { it.second!!.lastModified }
			else -> file.toList()
		})
	}

	private fun humanize(size: Int): String {
		var s = size.toDouble()
		var i = 0
		while(s > 1024) {
			s /= 1024
			i++
		}
		val int = s.toInt()
		val space = log(int.toDouble(), 10.0).toInt() + 1
		return "${" ".repeat(4 - space)}${int}${listOf(" KMGPE")[i] + "B"}"
	}

	private fun getLastModifiedUTC(f: File): String {
		val lastModified = f.lastModified
		val date = Date(lastModified)
		return date.toLocaleString(Translation.getCurrentLocale(), dateLocaleOptions { hour12 = false })
	}

	private fun out(absPath: String, list: List<Pair<FileSystemHandle, File?>>, asList: Boolean, human: Boolean, includeHide: Boolean) {
		if(asList) {
			val fileFilter = list.filter { it.second != null }.map { it.second!! }
			var maxSize = 1
			for(file in fileFilter) {
				val size = file.size.toInt()
				if(size > maxSize) maxSize = size
			}
			val spaceSize = log(maxSize.toDouble(), 10.0).toInt() + 1
			val pipeOutName = { prefix: String, name: String, color: String? ->
				tunnel.pipeOutTag("span") {
					innerHTML = prefix
					style.color = Settings.getSettings(SettKeys.Theme.COLOR_2)
				}
				tunnel.pipeOutText(name) {
					color?.let { style.color = color }
				}
			}
			val dirTimeSpace = run {
				if(fileFilter.isEmpty()) 0
				else {
					val tmp = fileFilter[0]
					getLastModifiedUTC(tmp).length
				}
			}
			if(includeHide) {
				pipeOutName("d--&nbsp;${"&nbsp;".repeat(spaceSize)}&nbsp;${"&nbsp;".repeat(dirTimeSpace)}&nbsp;", ".", Settings.getSettings(SettKeys.Theme.COLOR_1_DARK))
				tunnel.pipeOutNewLine()
				if(absPath != "/") {
					pipeOutName("d--&nbsp;${"&nbsp;".repeat(spaceSize)}&nbsp;${"&nbsp;".repeat(dirTimeSpace)}&nbsp;", "..", Settings.getSettings(SettKeys.Theme.COLOR_1_DARK))
					tunnel.pipeOutNewLine()
				}
			}
			for(handle in list) {
				val name = handle.first.name
				if(handle.second != null) {
					val file = handle.second!!
					val fileSize = file.size.toInt()
					val sizeSpace = log(if(fileSize == 0) 1.0 else fileSize.toDouble(), 10.0).toInt() + 1
					val perm = FS.getPermission("$absPath/$name")
					val pre = if(human) "f${perm}&nbsp;${humanize(fileSize)}&nbsp;${getLastModifiedUTC(file)}&nbsp;"
					else "f${perm}&nbsp;${"&nbsp;".repeat(spaceSize - sizeSpace)}${file.size.toInt()}&nbsp;${getLastModifiedUTC(file)}&nbsp;"
					pipeOutName(pre, name, if(file.name.startsWith(".")) Settings.getSettings(SettKeys.Theme.COLOR_1_DARK) else null)
				}
				else {
					pipeOutName("d--&nbsp;${"&nbsp;".repeat(spaceSize)}&nbsp;${"&nbsp;".repeat(dirTimeSpace)}&nbsp;", name,
						if(name.startsWith(".")) Settings.getSettings(SettKeys.Theme.COLOR_1_DARK) else Settings.getSettings(SettKeys.Theme.COLOR_1))
				}
				tunnel.pipeOutNewLine()
			}
		}
		else {
			val pipeOutName = { name: String, color: String? ->
				tunnel.pipeOutText(name) {
					style.paddingRight = "40px"
					color?.let { style.color = color }
				}
			}
			if(includeHide) {
				pipeOutName(".", Settings.getSettings(SettKeys.Theme.COLOR_1_DARK))
				if(absPath != "/") {
					pipeOutName("..", Settings.getSettings(SettKeys.Theme.COLOR_1_DARK))
				}
			}
			var idx = 0
			for(handle in list) {
				pipeOutName(handle.first.name,
					if(handle.first.name.startsWith(".")) {
						if (handle.second == null) Settings.getSettings(SettKeys.Theme.COLOR_1_DARK)
						else Settings.getSettings(SettKeys.Theme.FOREGROUND_DARK)
					}
					else if(handle.second == null) Settings.getSettings(SettKeys.Theme.COLOR_1)
					else null)
				if(idx % 5 == 4) tunnel.pipeOutNewLine()
				idx++
			}
		}
	}

	override fun getHelp(): String = Translation["command.ls.help"]

}