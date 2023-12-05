package command.cmds

import command.Command
import io.pipeOutPre
import io.pipeOutTag
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
		tunnel.pipeOutTag("br")
		tunnel.pipeOutText("Welcome to the LF OS")
		tunnel.pipeOutTag("br")
		tunnel.pipeOutText("Type 'help' to see the help page")
		return 0
	}

}