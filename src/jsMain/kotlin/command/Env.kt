package command

class Env(val baseEnv: Env? = null) {

	private val env = HashMap<String, String>()

	operator fun set(key: String, value: String) {
		env[key] = value
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

}