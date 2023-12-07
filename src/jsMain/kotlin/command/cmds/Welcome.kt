package command.cmds

import Translation
import command.Command
import io.*
import io.pipeOutText

class Welcome : Command() {

	override fun execute(args: Array<String>): Int {
		val osIcon = """ ___      _______    _______  _______ 
|   |    |       |  |       ||       |
|   |    |    ___|  |   _   ||  _____|
|   |    |   |___   |  | |  || |_____ 
|   |___ |    ___|  |  |_|  ||_____  |
|       ||   |      |       | _____| |
|_______||___|      |_______||_______|
"""
		tunnel.pipeOutPre(osIcon) { color = "green"	}
		tunnel.pipeOutNewLine()
		tunnel.pipeOutTextLn(Translation["command.welcome.0"])
		tunnel.pipeOutText(Translation["command.welcome.1"])
		return 0
	}

}