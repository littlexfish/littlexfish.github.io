package command

import command.cmds.*
import command.cmds.Env as EnvCmd
import io.TerminalTunnel

abstract class Command {

	protected lateinit var tunnel: TerminalTunnel
	protected lateinit var env: Env

	fun init(t: TerminalTunnel, e: Env) {
		tunnel = t
		env = e
	}

	/**
	 * execute the command
	 * @param args the arguments
	 * @return the exit code
	 */
	abstract fun execute(args: Array<String>): Int

	open fun getHelp(): String? = null

}

object Commands {

	private val commands = HashMap<String, Command>()

	init {
		commands["help"] = Help()
		commands["welcome"] = Welcome()
		commands["clear"] = Clear()
		commands["lscmd"] = LsCmd()
		commands["env"] = EnvCmd()
		commands["info"] = Info()
		commands["echo"] = Echo()
		commands["alias"] = Alias()
		commands["set"] = Set()
		commands["unset"] = Unset()
		commands["unalias"] = UnAlias()
		commands["about"] = About()
	}

	fun getCommand(cmd: String): Command? = commands[cmd]

	fun availableCommands(): List<String> = commands.keys.toList()

}
