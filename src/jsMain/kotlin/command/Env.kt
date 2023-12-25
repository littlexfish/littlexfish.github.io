package command

import fs.FS
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

private val INPUT_BEGIN_CHANGE_ENVS = listOf("INPUT_BEGIN", "PWD", "USER", "VERSION", "ENGINE_VERSION")

class Env(val baseEnv: Env? = null) {

	@Suppress("unused")
	private val root: Env = baseEnv?.root ?: this
	private val env = HashMap<String, String>()

	operator fun set(key: String, value: String) {
		env[key] = value
		if(key in INPUT_BEGIN_CHANGE_ENVS) {
			(document.getElementById("terminal-input-prefix") as? HTMLElement)?.innerText = getCommandInputPrefix()
		}
	}

	operator fun get(key: String): String? {
		return env[key] ?: getEnvFromBase(key)
	}

	fun remove(key: String): String? {
		return env.remove(key)
	}

	private fun getEnvFromBase(key: String): String? {
		return baseEnv?.get(key)
	}

	fun getAllEnv(): Map<String, String> {
		val out = HashMap<String, String>()
		if(baseEnv != null) {
			out.putAll(baseEnv.getAllEnv())
		}
		out.putAll(env)
		return out
	}

	fun reset() {
		env.clear()
	}

	/**
	 * %p: current path
	 * %P: current path (full)
	 * %u: current user
	 * %v: current version
	 * %e: current engine version
	 */
	fun getCommandInputPrefix(): String {
		val pattern = get("INPUT_BEGIN") ?: return ""
		return pattern
			.replace("%p", FS.replaceHomeDirectory(get("PWD") ?: "%p"))
			.replace("%P", get("PWD") ?: "%P")
			.replace("%u", get("USER") ?: "%u")
			.replace("%v", get("VERSION") ?: "%v")
			.replace("%e", get("ENGINE_VERSION") ?: "%e")
	}

	fun defaultCommandInputPrefix() = "%p> "

}