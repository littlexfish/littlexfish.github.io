package command.cmds

import command.Command
import io.*
import kotlinx.browser.document
import org.w3c.dom.HTMLLinkElement

class About : Command() {

	override fun execute(args: Array<String>): Int {
		tunnel.pipeOutTextLn("Hi,")
		tunnel.pipeOutNewLine()
		tunnel.pipeOutTextLn("This is the LF OS created by myself.")
		tunnel.pipeOutTextLn("Here contains some information about me.")
		tunnel.pipeOutTextLn("Please enjoy on the terminal.")
		tunnel.pipeOutNewLine()
		tunnel.pipeOutText("If you found any bugs, please open an ")
		tunnel.pipeOutLink("https://github.com/littlexfish/littlexfish.github.io/issues/new") { innerText = "issue" }
		tunnel.pipeOutTextLn(".")
		tunnel.pipeOutNewLine()
		tunnel.pipeOutText("The source code is available on ")
		tunnel.pipeOutLink("https://github.com/littlexfish/littlexfish.github.io") { innerText = "Github" }
		tunnel.pipeOutTextLn(".")
		tunnel.pipeOutNewLine()
		tunnel.pipeOutTextLn("Best regards,")
		tunnel.pipeOutText("LF")
		return 0
	}

}