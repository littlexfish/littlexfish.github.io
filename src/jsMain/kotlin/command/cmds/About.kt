package command.cmds

import Translation
import command.Command
import io.*
import kotlinx.browser.document
import org.w3c.dom.HTMLLinkElement

class About : Command() {

	override fun execute(args: Array<String>): Int {
		tunnel.pipeOutTextLn(tr("0"))
		tunnel.pipeOutNewLine()
		tunnel.pipeOutTextLn(tr("1"))
		tunnel.pipeOutTextLn(tr("2"))
		tunnel.pipeOutTextLn(tr("3"))
		tunnel.pipeOutNewLine()
		val tr4 = tr("4")
		val issue = tr4.split("\\{\\s*issue\\s*\\}".toRegex(), 2)
		tunnel.pipeOutText(issue[0])
		tunnel.pipeOutLink("https://github.com/littlexfish/littlexfish.github.io/issues/new") { innerText = tr("4_1") }
		tunnel.pipeOutTextLn(issue[1])
		tunnel.pipeOutNewLine()
		val tr5 = tr("5")
		val github = tr5.split("\\{\\s*github\\s*\\}".toRegex(), 2)
		tunnel.pipeOutText(github[0])
		tunnel.pipeOutLink("https://github.com/littlexfish/littlexfish.github.io") { innerText = tr("5_1") }
		tunnel.pipeOutTextLn(github[1])
		tunnel.pipeOutNewLine()
		tunnel.pipeOutTextLn(tr("6"))
		tunnel.pipeOutText(tr("7"))
		return 0
	}

	private fun tr(key: String): String {
		return Translation["command.about.$key"]
	}

}