package command.cmds.global

import Translation
import command.Command
import fs.FS
import io.pipeOutNewLine
import io.pipeOutText

class Grep : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args, listOf("f"))

		val pattern = pArg.getStandalone().lastOrNull()
		if(pattern == null) {
			tunnel.pipeOutText(Translation["command_arg.1"]) {
				style.color = "red"
			}
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

		}
		else { // read from file
			val actFile = mutableListOf<String>()
			for(file in useFile) {
				if(FS.hasFile(file, env["PWD"])) {
					actFile.add(file)
				}
			}
			// TODO: highlight matched text
			grepFile(pattern.toRegex(), matchType, actFile, 0, mutableListOf()) {
				it.forEach { (line, matches) ->
					// TODO highlight
					var lastNormalEnd = 0
					for((start, end) in matches) {
						if(start > lastNormalEnd) {
							tunnel.pipeOutText(line.substring(lastNormalEnd, start))
						}
						tunnel.pipeOutText(line.substring(start, end + 1)) {
							style.color = "cornflowerblue"
						}
						lastNormalEnd = end + 1
					}
					if(lastNormalEnd < line.length) {
						tunnel.pipeOutText(line.substring(lastNormalEnd))
					}
					tunnel.pipeOutNewLine()
				}
			}

		}

		return 0
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