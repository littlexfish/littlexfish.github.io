import command.*
import io.TerminalTunnel
import kotlinx.browser.document
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.dom.append
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement

private lateinit var terminalOutput: HTMLDivElement
private lateinit var terminalInput: HTMLInputElement

val rootEnv = Env()
var currentEnv = Env(rootEnv)
private val commandHistory = ArrayList<String>()
private var inputTemp = ""
private var currentHistoryIndex = -1

private var outputId = -1
	get() {
		field++
		return field
	}
private var inputId = -1
	get() {
		field++
		return field
	}

suspend fun main() {
	coroutineScope {
		launch {
			try {
				Translation.init()
			}
			catch(e: Throwable) {
				console.error(e)
				document.body!!.append {
					indexError("Failed to load translation file.")
				}
			}
			init()
			document.body!!.append {
				indexBuild()
			}

			terminalOutput = document.getElementById("terminal-output") as HTMLDivElement
			terminalInput = document.getElementById("terminal-input") as HTMLInputElement
			terminalInput.onkeydown = { event ->
				if(event.keyCode == 13) {
					onCommand()
				}
				else if(event.keyCode == 9) {
					event.preventDefault()
					val auto = findAutoComplete(splitCommand(terminalInput.value))
					if(auto.size == 1) {
						terminalInput.value = auto[0]
					}
					else if(auto.size > 1) {
						// TODO: show auto complete
						console.log("auto complete for multiple possible is not supported yet")
					}
				}
				else if(event.keyCode == 38) { // up
					event.preventDefault()
					if(currentHistoryIndex == -1) inputTemp = terminalInput.value
					val history = historyChange(1)
					if(history != null) {
						terminalInput.value = history
					}
				}
				else if(event.keyCode == 40) { // down
					event.preventDefault()
					if(currentHistoryIndex == -1) inputTemp = terminalInput.value
					val history = historyChange(-1)
					if(history != null) {
						terminalInput.value = history
					}
				}
			}
			terminalInput.onfocus = { _ ->
				document.getElementById("terminal-input-outline")?.classList?.add("focus")
			}
			terminalInput.onblur = { _ ->
				document.getElementById("terminal-input-outline")?.classList?.remove("focus")
			}
			document.onkeyup = { event ->
				if(event.keyCode == 191) { // /(slash)
					terminalInput.focus()
				}
			}
			postInit()
		}
	}
}

private fun init() {
	rootEnv["OS"] = "LF OS"
	rootEnv["ALIASES"] = "cls=clear;?=help"
	rootEnv["VERSION"] = "beta-0.1.0"
	rootEnv["ENGINE"] = "Kotlin/JS"
	rootEnv["ENGINE_VERSION"] = "1.9.21"
	rootEnv["ENGINE_LIB"] = "kotlinx-html-js:0.8.0;stdlib-js:1.9.21"
	rootEnv["INPUT_BEGIN"] = "> "
	currentEnv["CREATOR"] = "LF"
	currentEnv["PWD"] = "/"
	currentEnv["INPUT_BEGIN"] = currentEnv.defaultCommandInputPrefix()
}

private fun postInit() {
	runCommand(CommandStore("welcome", arrayOf()))
}

private fun onCommand() {
	val input = terminalInput.value
	terminalInput.value = ""
	if(input.isBlank()) return
	addInput(input)
	addToHistory(input)

	val pipe = defineCommandBatch(input)
	val errorCmd = checkPipeCommand(pipe)
	if(errorCmd.isNotEmpty()) {
		addOutput(createElement("span") {
			innerText = Translation["command_not_found", "cmd" to errorCmd.joinToString(", ")]
			style.color = "red"
		})
		return
	}
	terminalInput.disabled = true
	var currentPipe: Pipe? = pipe
	var currentTunnel: TerminalTunnel = newTerminalTunnel()
	while(currentPipe != null) {
		val nextTunnel = newTerminalTunnel()
		if(currentPipe.type == PipeType.PIPE_NEXT) {
			setTunnelToNextTunnel(currentTunnel, nextTunnel)
		}
		else {
			setTunnelToTerminal(currentTunnel)
		}
		val env = Env(currentEnv)
		val suc = runCommand(currentPipe.current, currentTunnel, env)
		if(currentPipe.type == PipeType.SUC_NEXT && !suc) {
			break
		}
		else if(currentPipe.type == PipeType.FAIL_NEXT && suc) {
			break
		}
		currentPipe = currentPipe.getNext()
		currentTunnel = nextTunnel
	}
	terminalInput.disabled = false
	terminalInput.focus()
}

private fun toAliasCommand(cmdLine: String): CommandStore {
	val split = splitCommand(cmdLine).let(::commandArrayToSplit)
	val alias = getAlias(split.command)
	return if(alias != null) {
		val newSplit = splitCommand(cmdLine.replaceFirst(split.command, alias)).let(::commandArrayToSplit)
		CommandStore(newSplit.command, newSplit.args)
	}
	else CommandStore(split.command, split.args)
}

private fun runCommand(split: CommandStore, tunnel: TerminalTunnel = newTerminalTunnel().apply { setTunnelToTerminal(this) }, env: Env = Env(currentEnv)): Boolean {
	val cmd = split.command
	val args = split.args
	val command = Commands.getCommand(cmd)
	if(command == null) { // this should not happen
		console.error("command '$cmd' not found after check")
		return false
	}
	command.init(tunnel, env)
	env["CMD"] = cmd
	val ec = command.execute(args)
	currentEnv["?"] = ec.toString()
	return ec == 0
}

private fun defineCommandBatch(input: String): Pipe {
	var remain = input
	val pipeLine = mutableListOf<Pair<CommandStore, PipeType>>()
	while(remain.isNotBlank()) {
		val nextPipePos = "\\s*;|&&|\\|\\||\\|\\s*".toRegex().find(remain)
		if(nextPipePos == null) {
			val split = toAliasCommand(remain)
			pipeLine.add(Pair(CommandStore(split.command, split.args), PipeType.NONE))
			remain = ""
		}
		else {
			val pipeType = when(nextPipePos.value.trim()) {
				";" -> PipeType.EXECUTE
				"|" -> PipeType.PIPE_NEXT
				"&&" -> PipeType.SUC_NEXT
				"||" -> PipeType.FAIL_NEXT
				else -> PipeType.NONE
			}
			val split = toAliasCommand(remain.substring(0, nextPipePos.range.first))
			pipeLine.add(Pair(CommandStore(split.command, split.args), pipeType))
			remain = remain.substring(nextPipePos.range.last + 1)
		}
	}
	val rootPipe = Pipe(pipeLine[0].first, pipeLine[0].second, null)
	var currentPipe = rootPipe
	for(i in 1..<pipeLine.size) {
		currentPipe.addPipe(pipeLine[i].first, pipeLine[i].second)
		currentPipe = currentPipe.getNext()!!
	}
	return rootPipe
}

private fun checkPipeCommand(pipe: Pipe): List<String> {
	val noCmd = mutableListOf<String>()
	var currentPipe: Pipe? = pipe
	while(currentPipe != null) {
		val cmd = currentPipe.current.command
		if(Commands.getCommand(cmd) == null) {
			noCmd.add(cmd)
		}
		currentPipe = currentPipe.getNext()
	}
	return noCmd
}

private fun splitCommand(line: String): Array<String> {
	val out = ArrayList<String>()
	val spSplit = line.split(" ")
	var quoteType: String? = null
	for(s in spSplit) {
		if(quoteType == null) {
			if(s.startsWith("$")) {
				val env = currentEnv[s.substring(1)] ?: ""
				out.add(env)
			}
			else if(s.startsWith("\"")) {
				if(s.endsWith("\"")) {
					out.add(s.substring(1, s.length - 1))
				}
				else {
					out.add(s.substring(1))
					quoteType = "\""
				}
			}
			else if(s.startsWith("'")) {
				if(s.endsWith("'")) {
					out.add(s.substring(1, s.length - 1))
				}
				else {
					out.add(s.substring(1))
					quoteType = "'"
				}
			}
			else {
				if(s.isNotBlank()){
					if(s.length > 1 && s.startsWith("-") && !s.startsWith("--")) {
						for(c in s.substring(1)) {
							out.add("-$c")
						}
					}
					else {
						out.add(s)
					}
				}
			}
		}
		else {
			if(s.endsWith(quoteType)) {
				out[out.size - 1] += " " + s.substring(0, s.length - quoteType.length)
				quoteType = null
			}
			else {
				out[out.size - 1] += " $s"
			}
		}
	}
	return out.toTypedArray()
}

private fun commandArrayToSplit(commandSplit: Array<String>): CommandStore {
	return CommandStore(commandSplit[0], commandSplit.sliceArray(1..< commandSplit.size))
}

/**
 * add output to terminal
 * @return the id of the output
 */
private fun addOutput(element: Element): String {
	val div = createOutput()
	div.append(element)
	terminalOutput.append(div)
	div.scrollIntoView()
	return div.id
}

fun createOutput(): Element {
	val out = document.createElement("div") as HTMLDivElement
	out.id = "to-$outputId"
	terminalOutput.append(out)
	return out
}

private fun addInput(cmd: String) {
	val out = document.createElement("div") as HTMLDivElement
	out.append(createElement("span") { classList.add("ti");innerText = currentEnv.getCommandInputPrefix() + cmd })
	out.id = "ti-$inputId"
	out.classList.add("ti")
	terminalOutput.append(out)
	out.scrollToView()
}

private fun newTerminalTunnel(): TerminalTunnel {
	return TerminalTunnel()
}

private fun setTunnelToNextTunnel(tunnel: TerminalTunnel, next: TerminalTunnel) {
	tunnel.registerPipeOut {
		next.pipeIn(it)
	}
}

private fun setTunnelToTerminal(tunnel: TerminalTunnel) {
	var currentOutput: Element? = null
	tunnel.registerPipeOut {
		if(currentOutput == null) {
			currentOutput = createOutput()
		}
		currentOutput!!.append(it)
		it.scrollToView()
	}
}

private fun setTunnelFromTerminal(tunnel: TerminalTunnel) {
	// TODO: now input from terminal not supported
}

fun clearTerminal() {
	terminalOutput.clear()
}

private fun findAutoComplete(array: Array<String>): Array<String> {
	return if(array.isEmpty()) return emptyArray()
	else if(array.size == 1) {
		val cmd = array[0]
		val cmds = Commands.availableCommands()
		val out = ArrayList<String>()
		for(c in cmds) {
			if(c.startsWith(cmd)) {
				out.add(c)
			}
		}
		out.toTypedArray()
	}
	else {
		console.log("auto complete for arguments is not supported yet")
		emptyArray()
	}
}

private fun addToHistory(line: String) {
	if(commandHistory.isEmpty()) commandHistory.add(line)
	else {
		commandHistory.remove(line)
		commandHistory.add(0, line)
	}
	if(commandHistory.size > 100) commandHistory.removeAt(100)
	currentHistoryIndex = -1
}

private fun historyChange(delta: Int): String? {
	val new = currentHistoryIndex + delta
	if(new < 0) {
		currentHistoryIndex = -1
		return inputTemp
	}
	if(new >= commandHistory.size) return null
	currentHistoryIndex = new
	return commandHistory[new]
}

private fun getAlias(t: String): String? {
	val aliases = currentEnv["ALIASES"]!!.split(";")
	for(a in aliases) {
		val split = a.split("=", limit = 2)
		if(split[0] == t) return split[1]
	}
	return null
}

private fun Element.scrollToView() {
	scrollIntoView(mapOf("behavior" to "smooth", "block" to "end", "inline" to "start"))
}
