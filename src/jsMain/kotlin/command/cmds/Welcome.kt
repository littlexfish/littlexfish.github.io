package command.cmds

import command.Command
import kotlinx.browser.document
import org.w3c.dom.HTMLPreElement

class Welcome : Command() {

	override fun execute(args: Array<String>): Int {
		val osIcon = document.createElement("pre") as HTMLPreElement
		osIcon.style.color = "green"
		osIcon.innerText = """ ___      _______    _______  _______ 
|   |    |       |  |       ||       |
|   |    |    ___|  |   _   ||  _____|
|   |    |   |___   |  | |  || |_____ 
|   |___ |    ___|  |  |_|  ||_____  |
|       ||   |      |       | _____| |
|_______||___|      |_______||_______|
"""
		tunnel.outputToTerminal(osIcon)
		tunnel.outputToTerminal("Welcome to the LF OS")
		tunnel.outputToTerminal("Type 'help' to see the help page")
		tunnel.outputToTerminal()
		return 0
	}

}