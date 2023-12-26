package command.cmds.global

import Translation
import command.Command
import command.Commands
import kotlinx.browser.document
import kotlinx.html.*
import kotlinx.html.dom.create

class LsCmd : Command() {

	override suspend fun execute(args: Array<String>): Int {
		val pArg = parseArgs(args, listOf("s"))
		val lineSize = pArg.get("s")?.toIntOrNull() ?: 5
		val cmd = Commands.availableCommands()
		tunnel.pipeOut(document.create.div { tableCommand(lineSize, cmd)() })
		return 0
	}

	private fun tableCommand(column: Int, cmd: List<String>): DIV.() -> Unit = {
		table {
			style = "border-collapse: collapse;"
			tbody {
				for(i in cmd.indices step column) {
					tr {
						for(j in 0..<column) {
							td {
								style = "padding-right: 40px;"
								if(i + j < cmd.size) {
									+cmd[i + j]
								}
							}
						}
					}
				}
			}
		}
	}

	override fun getHelp(): String = Translation["command.lscmd.help"]

}