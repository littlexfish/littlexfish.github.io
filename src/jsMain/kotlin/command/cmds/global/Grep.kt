package command.cmds.global

import Translation
import command.Command
import fs.FS
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import io.pipeOutNewLine
import io.pipeOutText

class Grep : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args, listOf("f"))

		val pattern = pArg.getStandalone().lastOrNull()
		if(pattern == null) {
			pipeNeedArgs(tunnel, 1)
			return 1
		}
		val useFile = pArg.getAll("f")
		val lineMatch = pArg.has("l")
		val allMatch = pArg.has("a")
		val matchType = when {
			allMatch -> 2
			lineMatch -> 1
			else -> 0
		}

		if(useFile == null) { // read from pipe input
			if(!tunnel.hasNextRead()) {
				// TODO: read from stdin by user
				return 0
			}
			val contents = mutableListOf<String>()
			while(tunnel.hasNextRead()) {
				val ele = tunnel.readFromPipeIn()!!
				val html = ele.innerHTML
				val brk = html.replace("</?br>|</?p>|</?div>|</?tr>".toRegex(), "\n")
				val noTag = brk.replace("<[^>]+?>".toRegex(), " ")
				val noEmpty = noTag.split("\n").filter { it.isNotBlank() }.joinToString("\n")
				contents.add(noEmpty)
			}
			val allResult = mutableListOf<Pair<String, List<Pair<Int, Int>>>>()
			for(content in contents) {
				val result = grep(pattern.toRegex(), content, matchType)
				allResult.addAll(result)
			}
			pipeOutGrepResult(allResult)
		}
		else { // read from file
			val actFile = mutableListOf<String>()
			for(file in useFile) {
				if(FS.hasFile(file, env["PWD"])) {
					actFile.add(file)
				}
			}
			grepFile(pattern.toRegex(), matchType, actFile, 0, mutableListOf()) {
				pipeOutGrepResult(it)
			}
		}

		return 0
	}

	private fun pipeOutGrepResult(results: List<Pair<String, List<Pair<Int, Int>>>>) {
		results.forEach { (line, matches) ->
			var lastNormalEnd = 0
			for((start, end) in matches) {
				if(start > lastNormalEnd) {
					tunnel.pipeOutText(line.substring(lastNormalEnd, start))
				}
				tunnel.pipeOutText(line.substring(start, end + 1)) {
					style.color = Settings[SettKeys.Theme.COLOR_1]
				}
				lastNormalEnd = end + 1
			}
			if(lastNormalEnd < line.length) {
				tunnel.pipeOutText(line.substring(lastNormalEnd))
			}
			tunnel.pipeOutNewLine()
		}
	}

	private suspend fun grepFile(pattern: Regex, matchType: Int, files: List<String>, index: Int, out: MutableList<Pair<String, List<Pair<Int, Int>>>>, func: (List<Pair<String, List<Pair<Int, Int>>>>) -> Unit) {
		if(index >= files.size) {
			func(out)
			return
		}
		val handle = FS.getFile(files[index], relativeFrom = env["PWD"])
		FS.readContentAsText(handle) {
			val result = grep(pattern, it, matchType)
			out.addAll(result)
			grepFile(pattern, matchType, files, index + 1, out, func)
		}
	}

	/**
	 * @param matchType 0: normal match, 1: line match, 2: all match
	 */
	private fun grep(pattern: Regex, content: String, matchType: Int): List<Pair<String, List<Pair<Int, Int>>>> {
		return when(matchType) {
			1 -> {
				val lines = content.split("\n")
				val out = mutableListOf<Pair<String, List<Pair<Int, Int>>>>()
				for(line in lines) {
					if(pattern.matches(line)) {
						out.add(line to listOf(0 to line.length))
					}
				}
				out
			}
			2 -> {
				if(pattern.matches(content)) {
					content.split("\n").map { it to listOf(0 to it.length) }
				}
				else {
					emptyList()
				}
			}
			else -> {
				val lines = content.split("\n")
				val out = mutableListOf<Pair<String, List<Pair<Int, Int>>>>()
				for(line in lines) {
					val results = pattern.findAll(line)
					val lst = mutableListOf<Pair<Int, Int>>()
					for(r in results) {
						lst.add(r.range.first to r.range.last)
					}
					if(lst.isNotEmpty()) {
						out.add(line to lst)
					}
				}
				out
			}
		}
	}

	override fun getHelp(): String = Translation["command.grep.help"]

}