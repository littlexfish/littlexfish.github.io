package command.cmds

import Builder
import command.Command
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
			tunnel.outputToTerminal(div)
			return 0
		}
		else {
			var hasInvalid = false
			for(arg in args) {
				if(!arg.contains("=")) {
					tunnel.outputToTerminal("Invalid setter '$arg'")
					hasInvalid = true
					continue
				}
				val spl = arg.split("=", limit = 2)
				val key = spl[0]
				val value = spl[1]
				env.baseEnv?.set(key, value)
				tunnel.outputToTerminal(getEnvMapElement(key, value))
			}
			return if(hasInvalid) 1 else 0
		}
	}

	private fun getEnvMapElement(k: String, v: String): Element =
		Builder {
			append { text = "$k="  }
			append { text = "\"$v\"";color="LightGreen" }
		}.build()

	override fun getHelp(): String = "Prints all environment variables\n" +
			"Using `env [key]=[value]` to set environment variable"

}