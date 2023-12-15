package command.cmds.global

import command.Command
import createElement
import io.pipeOutTag

class Info : Command() {

	override suspend fun execute(args: Array<String>): Int {
		tunnel.pipeOut(createElement("span") {
			append(createElement("span") { innerText="OS Name: ";style.color = "LightGreen" })
			append(env["OS"])
		})
		tunnel.pipeOutTag("br")
		tunnel.pipeOut(createElement("span") {
			append(createElement("span") { innerText="OS Version: ";style.color = "LightGreen" })
			append(env["VERSION"])
		})
		tunnel.pipeOutTag("br")
		tunnel.pipeOut(createElement("span") {
			append(createElement("span") { innerText="Create By: ";style.color = "LightGreen" })
			append(env["CREATOR"])
		})
		tunnel.pipeOutTag("br")
		tunnel.pipeOut(createElement("span") {
			append(createElement("span") { innerText="OS Engine: ";style.color = "LightGreen" })
			append(env["ENGINE"])
		})
		tunnel.pipeOutTag("br")
		tunnel.pipeOut(createElement("span") {
			append(createElement("span") { innerText="OS Engine Version: ";style.color = "LightGreen" })
			append(env["ENGINE_VERSION"])
		})
		tunnel.pipeOutTag("br")
		val engLib = env["ENGINE_LIB"]!!
		val engLibs = engLib.split(";")
		tunnel.pipeOut(createElement("span") {
			innerText="OS Engine Libraries:";style.color = "LightGreen"
		})
		tunnel.pipeOutTag("br")
		for(lib in engLibs) {
			val spl = lib.split(":")
			tunnel.pipeOut(createElement("span") { innerHTML = "&nbsp;&nbsp;\"${spl[0]}\":${spl[1]}" })
			tunnel.pipeOutTag("br")
		}
		return 0
	}

}