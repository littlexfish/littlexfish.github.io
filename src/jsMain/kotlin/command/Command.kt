package command

import command.cmds.debug.*
import command.cmds.env.Alias
import command.cmds.env.Set
import command.cmds.env.UnAlias
import command.cmds.env.Unset
import command.cmds.fs.*
import command.cmds.global.*
import command.cmds.env.Env as EnvCmd
import io.TerminalTunnel
import io.pipeOutErrorTextTr
import util.Optional

abstract class Command(vararg type: CommandType) {

	companion object {
		fun parseArgs(args: Array<String>, needValue: List<String> = emptyList()): Argument {
			return Argument(args, needValue)
		}
		fun pipeNeedArgs(tunnel: TerminalTunnel, num: Int) {
			if(num < 0) {
				throw IllegalArgumentException("num must >= 0")
			}
			if(num == 1) {
				tunnel.pipeOutErrorTextTr("command_arg.1")
			}
			else {
				tunnel.pipeOutErrorTextTr("command_arg.2+", "num" to num)
			}
		}
	}

	internal val types = setOf(*type)
	protected lateinit var tunnel: TerminalTunnel
	protected lateinit var env: Env

	fun init(t: TerminalTunnel, e: Env) {
		tunnel = t
		env = e
	}

	fun hasAllTypeOf(types: List<CommandType>): Boolean {
		return this.types.all { it in types }
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
	private val enabledTypes = HashSet<CommandType>()

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
		commands["cat"] = Cat()
		commands["open"] = Open()
		commands["chmod"] = Chmod()
		commands["mv"] = Mv()
		commands["cp"] = Cp()
		commands["terminal"] = Terminal()
		commands["exit"] = Exit()
		commands["grep"] = Grep()
		commands["debug:rs"] = ResetSettings()

	}

	fun registerType(type: CommandType) {
		enabledTypes.add(type)
	}

	fun getCommand(cmd: String): Optional<Command?> {
		val c = commands[cmd]
		return if(c == null) {
			Optional.empty()
		}
		else {
			Optional.ofNullable(if(c.hasAllTypeOf(enabledTypes.toList())) c else null)
		}
	}

	fun findModuleExcept(command: String): List<CommandType> {
		return enabledTypes.filter {
			val c = commands[command]
			if(c != null) it !in c.types else false
		}
	}

	fun availableCommands(): List<String> = commands.keys.toList().filter {
		commands[it]!!.hasAllTypeOf(enabledTypes.toList())
	}.sorted()

}
