package command.cmds

import command.Command
import createElement
import io.pipeOutText
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement

class Env : Command() {

	override fun execute(args: Array<String>): Int {
		if(args.isEmpty()) {
			val envMap = env.getAllEnv()
			val div = document.createElement("div") as HTMLDivElement
			for((k, v) in envMap.entries) {
				val span = document.createElement("span") as HTMLSpanElement
				span.append(getEnvMapElement(k, v))
				div.appendChild(span)
				div.append(document.createElement("br"))
			}
			tunnel.pipeOut(div)
			return 0
		}
		else {
			var hasInvalid = false
			for(arg in args) {
				if(!arg.contains("=")) {
					tunnel.pipeOutText("Invalid setter '$arg'") { style.color = "red" }
					hasInvalid = true
					continue
				}
				val spl = arg.split("=", limit = 2)
				val key = spl[0]
				val value = spl[1]
				env.baseEnv?.set(key, value)
				tunnel.pipeOut(getEnvMapElement(key, value))
			}
			return if(hasInvalid) 1 else 0
		}
	}

	private fun getEnvMapElement(k: String, v: String): Element = createElement("span") {
		append("$k=")
		append(createElement("span") { innerText = "\"$v\"";style.color = "LightGreen" })
	}

	override fun getHelp(): String = "prints all environment variables\n" +
			"Using `env [key]=[value]` to set environment variable"

}