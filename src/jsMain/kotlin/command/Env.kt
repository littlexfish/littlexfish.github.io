package command

import kotlinx.browser.document
import org.w3c.dom.HTMLElement

class Env(val baseEnv: Env? = null) {

	private val root: Env = baseEnv?.root ?: this
	private val env = HashMap<String, String>()

	operator fun set(key: String, value: String) {
		env[key] = value
		if(key == "INPUT_BEGIN") {
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

	fun getFromRoot(key: String): String? {
		return root[key]
	}

	fun reset() {
		env.clear()
	}

	/**
	 * %p: current path
	 * %u: current user
	 * %v: current version
	 * %e: current engine version
	 */
	fun getCommandInputPrefix(): String {
		val pattern = get("INPUT_BEGIN") ?: return ""
		return pattern
			.replace("%p", get("PWD") ?: "%p")
			.replace("%u", get("USER") ?: "%u")
			.replace("%v", get("VERSION") ?: "%v")
			.replace("%e", get("ENGINE_VERSION") ?: "%e")
	}

	fun defaultCommandInputPrefix() = "%p> "

}