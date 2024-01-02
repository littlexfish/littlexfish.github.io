package command.cmds.global

import Translation
import command.Command
import fs.SettKeys
import fs.Settings
import io.*
import io.pipeOutText

class Welcome : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val osIcon = """ ___      _______    _______  _______ 
|   |    |       |  |       ||       |
|   |    |    ___|  |   _   ||  _____|
|   |    |   |___   |  | |  || |_____ 
|   |___ |    ___|  |  |_|  ||_____  |
|       ||   |      |       | _____| |
|_______||___|      |_______||_______|
"""
		tunnel.pipeOutPre(osIcon) {
			color = Settings.getSettings(SettKeys.Theme.COLOR_3_DARK)
		}
		tunnel.pipeOutNewLine()
		tunnel.pipeOutTextLn(Translation["command.welcome.0"])
		tunnel.pipeOutText(Translation["command.welcome.1"])
		return 0
	}

	override fun getHelp(): String = Translation["command.welcome.help"]

}