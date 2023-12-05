package command

import kotlinx.browser.document
import org.w3c.dom.HTMLElement

class Env(val baseEnv: Env? = null) {

	private val root: Env = baseEnv?.root ?: this
	private val env = HashMap<String, String>()

	operator fun set(key: String, value: String) {
		env[key] = value
		if(key == "INPUT_BEGIN") {
			(document.getElementById("terminal-input-prefix") as? HTMLElement)?.innerText = value
		}
	}

	operator fun get(key: String): String? {
		return env[key] ?: getEnvFromBase(key)
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

}