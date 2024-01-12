package command

import Application
import fs.FS
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

private val INPUT_BEGIN_CHANGE_ENVS = listOf("INPUT_BEGIN", "PWD", "USER", "VERSION", "ENGINE_VERSION")

class Env(val baseEnv: Env? = null) {

	private companion object {
		const val OS_NAME = "LF OS"
		const val VERSION = "beta-0.3.0"
		const val ENGINE = "Kotlin/JS"
		const val ENGINE_VERSION = "1.9.21"
		const val ENGINE_LIB = "kotlinx-html-js:0.8.0;stdlib-js:1.9.21;kotlinx-coroutines-core:1.8.0-RC;font-awesome:6.5.1;highlight.js:11.9.0"
		const val CREATOR = "LF"
	}

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
		return env[key] ?: getEnvFromBase(key) ?: getEnvFromDefine(key)
	}

	private fun getEnvFromDefine(key: String): String? = when(key) {
		"APP_NAME" -> Application.getCurrentApp()?.name
		"OS" -> OS_NAME
		"VERSION" -> VERSION
		"ENGINE" -> ENGINE
		"ENGINE_VERSION" -> ENGINE_VERSION
		"ENGINE_LIB" -> ENGINE_LIB
		"CREATOR" -> CREATOR
		else -> null
	}

	private fun getAllEnvDefine(): Map<String, String> = mapOf(
		"OS" to OS_NAME,
		"VERSION" to VERSION,
		"ENGINE" to ENGINE,
		"ENGINE_VERSION" to ENGINE_VERSION,
		"ENGINE_LIB" to ENGINE_LIB,
		"CREATOR" to CREATOR
	)

	fun remove(key: String): String? {
		return env.remove(key)
	}

	private fun getEnvFromBase(key: String): String? {
		return baseEnv?.get(key)
	}

	fun getAllEnv(): Map<String, String> {
		val out = HashMap<String, String>()
		out.putAll(getAllEnvDefine())
		pAllEnv(out)
		return out
	}

	private fun pAllEnv(c: HashMap<String, String>) {
		baseEnv?.pAllEnv(c)
		c.putAll(env)
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