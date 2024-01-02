package command.cmds.global

import Translation
import command.Command
import createElement
import fs.SettKeys
import fs.Settings
import io.pipeOutTag

class Info : Command() {

	override suspend fun execute(args: Array<String>): Int {
		tunnel.pipeOut(createElement("span") {
			append(createElement("span") { innerText="OS Name: ";style.color = Settings.getSettings(SettKeys.Theme.COLOR_3) })
			append(env["OS"])
		})
		tunnel.pipeOutTag("br")
		tunnel.pipeOut(createElement("span") {
			append(createElement("span") { innerText="OS Version: ";style.color = Settings.getSettings(SettKeys.Theme.COLOR_3) })
			append(env["VERSION"])
		})
		tunnel.pipeOutTag("br")
		tunnel.pipeOut(createElement("span") {
			append(createElement("span") { innerText="Create By: ";style.color = Settings.getSettings(SettKeys.Theme.COLOR_3) })
			append(env["CREATOR"])
		})
		tunnel.pipeOutTag("br")
		tunnel.pipeOut(createElement("span") {
			append(createElement("span") { innerText="OS Engine: ";style.color = Settings.getSettings(SettKeys.Theme.COLOR_3) })
			append(env["ENGINE"])
		})
		tunnel.pipeOutTag("br")
		tunnel.pipeOut(createElement("span") {
			append(createElement("span") { innerText="OS Engine Version: ";style.color = Settings.getSettings(SettKeys.Theme.COLOR_3) })
			append(env["ENGINE_VERSION"])
		})
		tunnel.pipeOutTag("br")
		val engLib = env["ENGINE_LIB"]!!
		val engLibs = engLib.split(";")
		tunnel.pipeOut(createElement("span") {
			innerText="OS Engine Libraries:";style.color = Settings.getSettings(SettKeys.Theme.COLOR_3)
		})
		tunnel.pipeOutTag("br")
		for(lib in engLibs) {
			val spl = lib.split(":")
			tunnel.pipeOut(createElement("span") { innerHTML = "&nbsp;&nbsp;\"${spl[0]}\":${spl[1]}" })
			tunnel.pipeOutTag("br")
		}
		return 0
	}

	override fun getHelp(): String = Translation["command.info.help"]

}