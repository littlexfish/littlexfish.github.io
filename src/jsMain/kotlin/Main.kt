import command.Commands
import command.Env
import io.TerminalTunnel
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.dom.clear
import kotlinx.dom.createElement
import kotlinx.html.TagConsumer
import kotlinx.html.dom.append
import org.w3c.dom.*

lateinit var terminalOutput: HTMLDivElement
lateinit var terminalInput: HTMLInputElement

var currentEnv = Env()

fun main() {
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
	}
	terminalInput.onfocus = { _ ->
		document.getElementById("terminal-input-outline")?.classList?.add("focus")
	}
	terminalInput.onblur = { _ ->
		document.getElementById("terminal-input-outline")?.classList?.remove("focus")
	}
	showWelcomeMessage()
}

fun init() {
	currentEnv["OS"] = "LF OS"
	currentEnv["PWD"] = "/"
	currentEnv["INPUT_BEGIN"] = "> "
}

fun showWelcomeMessage() {
	Commands.getCommand("welcome", newTerminalTunnel(), currentEnv)?.execute(arrayOf())
}

fun onCommand() {
	val input = terminalInput.value
	terminalInput.value = ""
	if(input.isBlank()) return

	addOutput(Builder { color = "steelblue";text = currentEnv["INPUT_BEGIN"] + input }.build())
	val split = splitCommand(input).let(::commandArrayToSplit)
	val cmd = split.first
	val args = split.second
	val env = Env(currentEnv)
	val command = Commands.getCommand(cmd, newTerminalTunnel(), env)
	if(command == null) {
		addOutput(Builder("command not found: $cmd").apply { color = "red" }.build())
		return
	}
	else {
		env["CMD"] = cmd
		command.execute(args)
	}
}

private fun splitCommand(line: String): Array<String> {
	val out = ArrayList<String>()
	val spSplit = line.split(" ")
	var quoteType: String? = null
	for(s in spSplit) {
		if(quoteType == null) {
			if(s.startsWith("\"")) {
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

private fun commandArrayToSplit(commandSplit: Array<String>): Pair<String, Array<String>> {
	return Pair(commandSplit[0], commandSplit.sliceArray(1..< commandSplit.size))
}

fun addOutput(text: String) {
	val span = document.createElement("span") as HTMLSpanElement
	span.innerText = text
	addOutput(span)
}

fun addOutput(element: Element) {
	val div = document.createElement("div") as HTMLDivElement
	div.append(element)
	terminalOutput.append(div)
	div.scrollIntoView()
}

fun newTerminalTunnel(): TerminalTunnel {
	return TerminalTunnel(::addOutput)
}

fun clearTerminal() {
	terminalOutput.clear()
}

fun findAutoComplete(array: Array<String>): Array<String> {
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

// TODO: add pipe support