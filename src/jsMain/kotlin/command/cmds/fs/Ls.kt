package command.cmds.fs

import Translation
import command.Command
import ext.*
import fs.FS
import io.pipeOutNewLine
import io.pipeOutTag
import io.pipeOutText
import kotlinx.coroutines.await
import org.w3c.files.File
import kotlin.js.Date
import kotlin.math.log

class Ls : Command() {

	companion object {
		private const val COLOR_DIR = "cornflowerblue"
		private const val COLOR_DIR_HIDE = "mediumblue"
		private const val COLOR_HIDE = "gray"
		private const val COLOR_LIST_PREFIX = "mediumslateblue"
	}

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
		val dir = "$pwd/${pArg.getStandalone().lastOrNull() ?: ""}"

		val list = list(dir, listIncludeHide)
		val sorted = sort(list, sortType)

		out(sorted, listAsList, listShowHuman)
		return 0
	}

	private suspend fun list(dir: String, includeHide: Boolean): List<Pair<FileSystemHandle, File?>> {
		val iterator = FS.getDirectory(dir).getEntries()
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
		return date.toLocaleTimeString(Translation.getCurrentLocale(), dateLocaleOptions { hour12 = false })
	}

	private fun out(list: List<Pair<FileSystemHandle, File?>>, asList: Boolean, human: Boolean) {
		if(asList) {
			val fileFilter = list.filter { it.second != null }.map { it.second!! }
			var maxSize = 1
			for(file in fileFilter) {
				val size = file.size.toInt()
				if(size > maxSize) maxSize = size
			}
			val spaceSize = log(maxSize.toDouble(), 10.0).toInt() + 1
			for(handle in list) {
				val name = handle.first.name
				if(handle.second != null) {
					val file = handle.second!!
					val sizeSpace = log(file.size.toDouble(), 10.0).toInt() + 1
					if(human) {
						tunnel.pipeOutTag("span") {
							innerHTML = "f&nbsp;${humanize(file.size.toInt())}&nbsp;${getLastModifiedUTC(file)}&nbsp;"
							style.color = COLOR_LIST_PREFIX
						}
					}
					else {
						tunnel.pipeOutTag("span") {
							innerHTML = "f&nbsp;${"&nbsp;".repeat(spaceSize - sizeSpace)}${file.size.toInt()}&nbsp;${getLastModifiedUTC(file)}&nbsp;"
							style.color = COLOR_LIST_PREFIX
						}
					}
					tunnel.pipeOutText(name) {
						if(file.name.startsWith(".")) style.color = COLOR_DIR_HIDE
					}
				}
				else {
					tunnel.pipeOutTag("span") {
						innerHTML = "d&nbsp;${" ".repeat(spaceSize)}&nbsp;${"&nbsp;".repeat(12)}&nbsp;"
						style.color = COLOR_LIST_PREFIX
					}
					tunnel.pipeOutText(name) {
						if(handle.first.name.startsWith(".")) style.color = COLOR_DIR_HIDE
						else style.color = COLOR_DIR
					}
				}
				tunnel.pipeOutNewLine()
			}
		}
		else {
			for(handle in list) {
				tunnel.pipeOutText(handle.first.name) {
					style.paddingRight = "40px"
					if(handle.first.name.startsWith(".")) {
						if (handle.second == null) style.color = COLOR_DIR_HIDE
						else style.color = COLOR_HIDE
					}
					else if(handle.second == null) style.color = COLOR_DIR
				}
			}
		}
	}

	override fun onExecuteError(it: Throwable) {
		if(it::class.js.name == "TypeError" || it.asDynamic().name == DOMExceptionName.NOT_FOUND_ERR) {
			tunnel.pipeOutText("ls: No such file or directory") { style.color = "red" }
		}
		else {
			console.error(it)
			tunnel.pipeOutText("error on execute ls: $it") { style.color = "red" }
		}
	}

}