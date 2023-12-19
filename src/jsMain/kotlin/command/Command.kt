package command

import command.cmds.env.Alias
import command.cmds.env.Set
import command.cmds.env.UnAlias
import command.cmds.env.Unset
import command.cmds.fs.*
import command.cmds.global.*
import command.cmds.env.Env as EnvCmd
import io.TerminalTunnel

abstract class Command {

	companion object {
		fun parseArgs(args: Array<String>, needValue: List<String> = emptyList()): Argument {
			return Argument(args, needValue)
		}
	}

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
	abstract suspend fun execute(args: Array<String>): Int

	open fun onExecuteError(it: Throwable) {
		it.printStackTrace()
	}

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
		commands["pwd"] = Pwd()
		commands["ls"] = Ls()
		commands["touch"] = Touch()
		commands["cd"] = Cd()
		commands["rm"] = Rm()
		commands["mkdir"] = Mkdir()
		commands["rmdir"] = Rmdir()
		commands["edit"] = Edit()

//		commands["chmod"] = Chmod()
//		commands["cat"] = Cat()
//		commands["mv"] = Mv()
//		commands["cp"] = Cp()
	}

	fun getCommand(cmd: String): Command? = commands[cmd]

	fun availableCommands(): List<String> = commands.keys.toList()

}
