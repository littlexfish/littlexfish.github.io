package app

import Translation
import command.*
import createElement
import fs.SettKeys
import fs.Settings
import io.TerminalTunnel
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.*
import module.ModuleRegistry
import org.w3c.dom.*
import scrollToView
import style.*

class Terminal(rootEnv: Env? = null) : App("terminal") {

	/**
	 * The element contains terminal output
	 */
	private lateinit var terminalOutput: HTMLDivElement

	/**
	 * The element user can input command
	 */
	private lateinit var terminalInput: HTMLInputElement

	/**
	 * The max number of output element
	 */
	private val maxOutputElement = 100

	/**
	 * The root environment
	 */
	private val rootEnv = rootEnv ?: Env()

	/**
	 * The current environment
	 */
	private var currentEnv = this.rootEnv

	/**
	 * The command history
	 */
	private val commandHistory = ArrayList<String>()

	/**
	 * The temp input for history
	 */
	private var inputTemp = ""

	/**
	 * The current history index
	 */
	private var currentHistoryIndex = -1

	/**
	 * The id of the output
	 */
	private var outputId = -1
		get() {
			field++
			return field
		}

	/**
	 * The id of the input
	 */
	private var inputId = -1
		get() {
			field++
			return field
		}

	private val terminalStorage = mutableListOf<Element>()

	override fun buildGUI(): TagConsumer<HTMLElement>.() -> Unit = {
		div {
			id = "terminal-output"
		}
		div {
			id = "terminal-input-outline"
			span {
				id = "terminal-input-prefix"
				+currentEnv.getCommandInputPrefix()
			}
			input {
				id = "terminal-input"
				autoFocus = true
				autoComplete = false
			}
		}
	}

	override fun getStyleRegister(): StyleRegister = TerminalStyle

	override fun onInit() {}

	/**
	 * Post init the terminal
	 */
	private fun postInit() {
		MainScope().launch {
			// show module message
			runCommand(CommandStore("reload-modules", arrayOf()))
			// show the welcome message
			runCommand(CommandStore("welcome", arrayOf()))
		}
	}

	/**
	 * On command input
	 */
	private fun onCommand(input: String) {
		// input init
		if(input.isBlank()) return
		addInput(input)
		addToHistory(input)

		// process pipe
		val pipe = defineCommandBatch(input)
		val errorCmd = checkPipeCommand(pipe)
		if(errorCmd.isNotEmpty()) {
			val noModule = errorCmd.filter { !it.second }.map { it.first }.toMutableList()
			val noCommand = errorCmd.filter { it.second }.map { it.first }
			var needBreak = false
			if(noModule.isNotEmpty()) {
				val mods = noModule.flatMap { Commands.commandNeededModules(it) }.toMutableSet()
				ModuleRegistry.getLoadedModule().forEach { mods.remove(it) }
				addOutput(createElement("span") {
					innerText = Translation["command_no_module", "mods" to mods.joinToString(", ")]
					style.color = Settings[SettKeys.Theme.COLOR_ERROR]
				})
				needBreak = true
			}
			if(noCommand.isNotEmpty()) {
				if(needBreak) addOutput(createElement("br") {})
				addOutput(createElement("span") {
					innerText = Translation["command_not_found", "cmd" to noCommand.joinToString(", ")]
					style.color = Settings[SettKeys.Theme.COLOR_ERROR]
				})
			}
			currentEnv["?"] = "-1"
			return
		}
		// start pipe
		terminalInput.disabled = true
		MainScope().launch {
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
		}.invokeOnCompletion {
			// on pipe end
			terminalInput.disabled = false
			terminalInput.focus()
		}
	}

	/**
	 * Replace alias to the correct command
	 */
	private fun toAliasCommand(cmdLine: String): String {
		val aliases = getAliases()
		val aliasesRegex = aliases.map {
			val reg = it.first
				.replace("\\", "\\\\")
				.replace(".", "\\.")
				.replace("*", "\\*")
				.replace("+", "\\+")
				.replace("?", "\\?")
				.replace("(", "\\(")
				.replace(")", "\\)")
				.replace("[", "\\[")
				.replace("]", "\\]")
				.replace("{", "\\{")
				.replace("}", "\\}")
				.replace("|", "\\|")
				.replace("^", "\\^")
				.replace("$", "\\$")
				.replace("-", "\\-")
				.replace(":", "\\:")
			val notWords = "\\W"
			"($notWords)($reg)($notWords)".toRegex() to it.second
		}
		var out = cmdLine
		aliasesRegex.forEach {
			val matches = it.first.findAll(out)
			for(m in matches) {
				val prefix = m.groupValues[1]
				val alias = m.groupValues[2]
				val suffix = m.groupValues[3]
				out = out.replace("$prefix$alias$suffix", "$prefix${it.second}$suffix")
			}
		} // TODO: to prevent alias loop
		return out
	}

	/**
	 * Run a command
	 */
	private suspend fun runCommand(split: CommandStore, tunnel: TerminalTunnel = newTerminalTunnel().apply { setTunnelToTerminal(this) }, env: Env = Env(currentEnv)): Boolean {
		val cmd = split.command
		val args = split.args
		val command = Commands.getCommand(cmd).get()
		if(command == null) { // this should not happen
			console.error("command '$cmd' not found after check")
			return false
		}
		// set tunnel and env
		command.init(tunnel, env)
		env["CMD"] = cmd
		// execute command
		return try {
			val ec = command.execute(args)
			if(ec < 0) {
				throw IllegalStateException("exit code cannot be negative")
			}
			currentEnv["?"] = ec.toString()
			ec == 0
		}
		catch(e: Throwable) { // on command error
			command.onExecuteError(e)
			currentEnv["?"] = "-1"
			false
		}
	}

	/**
	 * Define the command batch with pipe
	 */
	private fun defineCommandBatch(input: String): Pipe {
		val pipeLine = mutableListOf<Pair<CommandStore, PipeType>>()
		var split = splitCommand(toAliasCommand(input))
		while(split.isNotEmpty()) {
			val idx = split.indexOfFirst { it in listOf(";", "&&", "||", "|") }
			if(idx == -1) {
				pipeLine.add(Pair(commandArrayToSplit(split), PipeType.NONE))
				split = emptyArray()
			}
			else {
				val pipeType = PipeType.entries.find { it.str == split[idx] } ?: PipeType.NONE
				val sub = split.sliceArray(0..<idx)
				pipeLine.add(Pair(commandArrayToSplit(sub), pipeType))
				split = split.sliceArray(idx + 1..<split.size)
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

	/**
	 * Check all command can execute
	 */
	private fun checkPipeCommand(pipe: Pipe): List<Pair<String, Boolean>> { // true for no command
		val noCmd = mutableListOf<Pair<String, Boolean>>()
		var currentPipe: Pipe? = pipe
		while(currentPipe != null) {
			val cmd = currentPipe.current.command
			val c = Commands.getCommand(cmd)
			if(c.isEmpty()) {
				noCmd.add(cmd to true)
			}
			else if(c.getOrNull() == null) {
				noCmd.add(cmd to false)
			}
			currentPipe = currentPipe.getNext()
		}
		return noCmd
	}

	/**
	 * Split command line to command and arguments, and replace with environment variables
	 */
	private fun splitCommand(line: String): Array<String> {
		val out = ArrayList<String>()
		val spSplit = line.split(" ")
		var quoteType: String? = null
		for(s in spSplit) {
			if(quoteType == null) { // not in quote
				if(s.startsWith("$")) { // find environment variable
					val env = currentEnv[s.substring(1)] ?: ""
					out.add(env)
				}
				else if(s.contains(";|&&|\\|".toRegex())) {
					val seg = splitPipeInSegment(s)
					out.addAll(seg)
				}
				else if(s.startsWith("\"")) { // find quote
					if(s.endsWith("\"")) {
						out.add(s.substring(1, s.length - 1))
					}
					else {
						out.add(s.substring(1))
						quoteType = "\""
					}
				}
				else if(s.startsWith("'")) { // find quote
					if(s.endsWith("'")) {
						out.add(s.substring(1, s.length - 1))
					}
					else {
						out.add(s.substring(1))
						quoteType = "'"
					}
				}
				else {
					if(s.isNotBlank()) {
						if(s.length > 1 && s.startsWith("-") && !s.startsWith("--")) { // find short option
							for(c in s.substring(1)) { // split short option
								out.add("-$c")
							}
						}
						else {
							out.add(s)
						}
					}
				}
			}
			else { // in quote
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

	private fun splitPipeInSegment(seg: String): List<String> {
		val out = mutableListOf<String>()
		var idx = 0
		while(idx < seg.length) {
			when(val curChar = seg[idx]) {
				';' -> {
					out.add(";")
				}
				'&' -> {
					if(idx + 1 < seg.length && seg[idx + 1] == '&') {
						out.add("&&")
						idx++
					}
					else {
						if(out.isEmpty()) out.add("&")
						else out[out.lastIndex] += "&"
					}
				}
				'|' -> {
					if(idx + 1 < seg.length && seg[idx + 1] == '|') {
						out.add("||")
						idx++
					}
					else {
						out.add("|")
					}
				}
				else -> {
					if(out.isEmpty()) out.add(curChar.toString())
					else out[out.lastIndex] += curChar.toString()
				}
			}
			idx++
		}
		return out.filter { it.isNotBlank() }
	}

	/**
	 * Convert command array to command store
	 */
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

	/**
	 * create output element
	 */
	private fun createOutput(): Element {
		val out = document.createElement("div") as HTMLDivElement
		out.id = "to-$outputId"
		terminalOutput.append(out)
		if(terminalOutput.childElementCount > maxOutputElement) {
			terminalOutput.removeChild(terminalOutput.firstChild!!)
		}
		out.scrollToView()
		return out
	}

	/**
	 * add input to terminal
	 */
	private fun addInput(cmd: String) {
		val out = document.createElement("div") as HTMLDivElement
		out.append(createElement("span") { classList.add("ti");innerText = currentEnv.getCommandInputPrefix() + cmd })
		out.id = "ti-$inputId"
		out.style.color = Settings[SettKeys.Theme.COLOR_CMD_INPUT]
		terminalOutput.append(out)
		if(terminalOutput.childElementCount > maxOutputElement) {
			terminalOutput.removeChild(terminalOutput.firstChild!!)
		}
		out.scrollToView()
	}

	/**
	 * new terminal tunnel
	 */
	private fun newTerminalTunnel(): TerminalTunnel {
		return TerminalTunnel()
	}

	/**
	 * set tunnel output to next tunnel
	 */
	private fun setTunnelToNextTunnel(tunnel: TerminalTunnel, next: TerminalTunnel) {
		tunnel.registerPipeOut {
			next.pipeIn(it)
		}
	}

	/**
	 * set tunnel output to terminal
	 */
	private fun setTunnelToTerminal(tunnel: TerminalTunnel) {
		var currentOutput: Element? = null
		tunnel.registerPipeOut {
			if(currentOutput == null) {
				currentOutput = createOutput()
			}
			currentOutput!!.append(it)
			currentOutput!!.scrollToView()
		}
	}

	/**
	 * set tunnel input from terminal
	 */
	@Suppress("unused", "unused_parameter")
	private fun setTunnelFromTerminal(tunnel: TerminalTunnel) {
		// TODO: now input from terminal not supported
	}

	/**
	 * clear terminal
	 */
	private fun clearTerminal() {
		terminalOutput.clear()
	}

	/**
	 * find auto complete when user press tab
	 */
	private fun findAutoComplete(array: Array<String>): Array<String> {
		return if(array.isEmpty()) return emptyArray()
		else if(array.size == 1) {
			val cmd = array[0]
			val commands = Commands.availableCommands()
			val out = ArrayList<String>()
			for(c in commands) {
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

	/**
	 * add input to history
	 */
	private fun addToHistory(line: String) {
		if(commandHistory.isEmpty()) commandHistory.add(line)
		else {
			commandHistory.remove(line)
			commandHistory.add(0, line)
		}
		if(commandHistory.size > 100) commandHistory.removeAt(100)
		currentHistoryIndex = -1
	}

	/**
	 * on history change, when user press up or down
	 */
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

	/**
	 * get alias for command
	 */
	private fun getAliases(): List<Pair<String, String>> {
		return currentEnv["ALIASES"]!!.split(";").map {
			val split = it.split("=", limit = 2)
			split[0] to split[1]
		}
	}


	private var isFirstShowWelcome = true
	override fun onRestore() {
		// init terminal ui element and event
		terminalOutput = document.getElementById("terminal-output") as HTMLDivElement
		terminalInput = document.getElementById("terminal-input") as HTMLInputElement
		terminalInput.onkeydown = { event ->
			if(event.keyCode == 13) { // enter
				event.preventDefault()
				val value = terminalInput.value
				terminalInput.value = ""
				onCommand(value)
			}
			else if(event.keyCode == 9) { // tab
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
			Unit
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
		if(isFirstShowWelcome) {
			isFirstShowWelcome = false
			postInit()
		}
		terminalStorage.forEach { terminalOutput.append(it) }
		terminalOutput.lastElementChild?.scrollToView()
		terminalStorage.clear()
		terminalInput.focus()
	}

	override fun onSuspend() {
		terminalStorage.addAll(terminalOutput.children.asList())
		terminalOutput.clear()
	}

	override fun onReceiveMessage(msg: String, extra: Map<String, String>?) {
		when(msg) {
			"clear" -> clearTerminal()
			"run" -> {
				val cmd = extra?.get("cmd") ?: return
				MainScope().launch {
					onCommand(cmd)
				}
			}
		}
	}

}