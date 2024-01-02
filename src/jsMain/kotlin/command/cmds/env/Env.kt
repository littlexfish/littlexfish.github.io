package command.cmds.env

import Translation
import command.Command
import createElement
import fs.SettKeys
import fs.Settings
import io.pipeOutErrorTextTr
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement

class Env : Command() {

	override suspend fun execute(args: Array<String>): Int {
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
			if(args.contains("-r") || args.contains("--reset")) {
				env.baseEnv?.reset()
				return 0
			}
			var hasInvalid = false
			for(arg in args) {
				if(!arg.contains("=")) {
					tunnel.pipeOutErrorTextTr("command.env.invalid_setter", "setter" to arg)
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
		append(createElement("span") {
			innerText = "\"$v\"";
			style.color = Settings.getSettings(SettKeys.Theme.COLOR_3)
		})
	}

	override fun getHelp(): String = Translation["command.env.help"]

}